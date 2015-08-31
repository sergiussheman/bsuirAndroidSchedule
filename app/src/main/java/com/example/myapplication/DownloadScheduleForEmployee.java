package com.example.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Utils.DateUtil;
import com.example.myapplication.Utils.FileUtil;
import com.example.myapplication.DataProvider.LoadSchedule;
import com.example.myapplication.Model.AvailableFragments;
import com.example.myapplication.Model.Employee;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DownloadScheduleForEmployee extends Fragment {
    private static final String TAG = "employeeDownloadTAG";
    private static final String AVAILABLE_EMPLOYEES_PARAM = "availableEmployees";
    private static final String DOWNLOADED_SCHEDULES_PARAM = "downloadScheduls";
    private static final String PATTERN = "^[а-яА-ЯёЁ]+";


    private List<Employee> availableEmployeeList;
    private List<String> downloadedSchedules;
    private  View currentView;
    private boolean isDownloadingNewSchedule;
    private TableLayout tableLayoutForDownloadedSchedules;

    private OnFragmentInteractionListener mListener;

    //TODO: create bundle with list of employees
    public static DownloadScheduleForEmployee newInstance(List<Employee> availableEmployees, List<String> listDownloadedSchedules) {
        return new DownloadScheduleForEmployee();
    }

    public DownloadScheduleForEmployee() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        currentView = inflater.inflate(R.layout.fragment_download_schedule_for_employee, container, false);

        try {
            ConnectivityManager connectMan = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectMan.getActiveNetworkInfo();
            if(networkInfo != null && networkInfo.isConnected()) {
                new DownloadEmployeeXML().execute();
            }
        } catch (Exception e){
            Toast.makeText(getActivity(), R.string.can_not_load_list_of_employees, Toast.LENGTH_LONG).show();
        }

        Button downloadButton = (Button) currentView.findViewById(R.id.buttonForDownloadEmployeeSchedule);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager connectMan = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectMan.getActiveNetworkInfo();
                if(networkInfo != null && networkInfo.isConnected()) {
                    AutoCompleteTextView textView = (AutoCompleteTextView) currentView.findViewById(R.id.autoCompleteForEmployee);
                    String selectedEmployeeName = textView.getText().toString();
                    Employee selectedEmployee = getEmployeeByName(selectedEmployeeName);
                    if(selectedEmployee != null) {
                        String parameterForDownloadSchedule = getFileNameForEmployeeSchedule(selectedEmployee);
                        setIsDownloadingNewSchedule(true);
                        downloadOrUpdateScheduleForEmployee(parameterForDownloadSchedule);
                        updateDefaultEmployee(selectedEmployee, true);

                    } else{
                        Toast.makeText(getActivity(), getResources().getString(R.string.should_select_employee), Toast.LENGTH_LONG).show();
                    }
                } else{
                    Toast.makeText(getActivity(), getResources().getString(R.string.no_connection_to_network), Toast.LENGTH_LONG).show();
                }
            }
        });

        setTableLayoutForDownloadedSchedules((TableLayout) currentView.findViewById(R.id.tableLayoutForEmployee));
        setDownloadedSchedules(FileUtil.getAllDownloadedSchedules(getActivity(), false));
        populateTableLayout(getTableLayoutForDownloadedSchedules(), downloadedSchedules);

        return currentView;
    }

    public void downloadOrUpdateScheduleForEmployee(String employeeNameForDownload){
        DownloadFilesTask task = new DownloadFilesTask();
        task.filesDir = getActivity().getFilesDir();
        task.execute(employeeNameForDownload);
    }

    public void populateTableLayout(final TableLayout tableLayout, final List<String> schedulesForEmployee){
        Integer currentRowNumber = 0;
        tableLayout.removeAllViews();
        tableLayout.setPadding(5, 0, 5, 0);
        TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);

        TableRow headerRow = new TableRow(getActivity());
        TextView employeeHeaderTextView = new TextView(getActivity());
        employeeHeaderTextView.setText(getResources().getString(R.string.employee_shortcut));
        employeeHeaderTextView.setGravity(Gravity.START);
        employeeHeaderTextView.setLayoutParams(params);
        employeeHeaderTextView.setTypeface(null, Typeface.BOLD);
        headerRow.addView(employeeHeaderTextView);

        TextView lastUpdateHeader = new TextView(getActivity());
        lastUpdateHeader.setText(getResources().getString(R.string.last_updated));
        lastUpdateHeader.setTypeface(null, Typeface.BOLD);
        headerRow.addView(lastUpdateHeader);

        TextView deleteHeader = new TextView(getActivity());
        deleteHeader.setText(getResources().getString(R.string.delete));
        deleteHeader.setTypeface(null, Typeface.BOLD);
        deleteHeader.setGravity(Gravity.CENTER);
        deleteHeader.setPadding(5, 0, 5, 0);
        headerRow.addView(deleteHeader);

        TextView refreshHeader = new TextView(getActivity());
        refreshHeader.setText(R.string.refresh);
        refreshHeader.setTypeface(null, Typeface.BOLD);
        refreshHeader.setGravity(Gravity.CENTER);
        refreshHeader.setPadding(5,0,5,0);
        headerRow.addView(refreshHeader);

        headerRow.setPadding(0, 0, 0, 18);
        tableLayout.addView(headerRow);

        for(String currentEmployeeSchedule : schedulesForEmployee) {
            TableRow rowForEmployeeSchedule = new TableRow(getActivity());
            TextView textViewForEmployeeName = new TextView(getActivity());
            textViewForEmployeeName.setGravity(Gravity.START);
            textViewForEmployeeName.setText(getEmployeeNameFromString(currentEmployeeSchedule));
            textViewForEmployeeName.setLayoutParams(params);
            rowForEmployeeSchedule.addView(textViewForEmployeeName);

            TextView lastUpdatedTextView = new TextView(getActivity());
            lastUpdatedTextView.setText(getLastUpdateFromPreference(currentEmployeeSchedule));
            rowForEmployeeSchedule.addView(lastUpdatedTextView);


            ImageView deleteImageView = new ImageView(getActivity());
            deleteImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_remove));
            deleteImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(getResources().getString(R.string.delete))
                            .setMessage(getResources().getString(R.string.confirm_delete_message))
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    TableRow selectedRow = (TableRow) v.getParent();
                                    Integer rowNumber = (Integer) selectedRow.getTag();
                                    String fileNameForDelete = schedulesForEmployee.get(rowNumber);
                                    File file = new File(getActivity().getFilesDir(), fileNameForDelete);
                                    if (!file.delete()) {
                                        Toast.makeText(getActivity(), R.string.error_while_deleting_file, Toast.LENGTH_LONG).show();
                                    }
                                    deleteDefaultEmployeeIfNeed(fileNameForDelete);
                                    setDownloadedSchedules(FileUtil.getAllDownloadedSchedules(getActivity(), false));
                                    populateTableLayout(tableLayout, getDownloadedSchedules());
                                }
                            })
                            .setNegativeButton(android.R.string.no, null).show();
                }
            });
            rowForEmployeeSchedule.addView(deleteImageView);

            ImageView refreshImageView = new ImageView(getActivity());
            refreshImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_refresh));
            refreshImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ConnectivityManager connectMan = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connectMan.getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.isConnected()) {
                        TableRow selectedRow = (TableRow) v.getParent();
                        Integer rowNumber = (Integer) selectedRow.getTag();
                        String fileNameForRefresh = schedulesForEmployee.get(rowNumber);
                        fileNameForRefresh = fileNameForRefresh.substring(0, fileNameForRefresh.length() - 4);
                        setIsDownloadingNewSchedule(false);
                        downloadOrUpdateScheduleForEmployee(fileNameForRefresh);
                        updateDefaultEmployee(fileNameForRefresh, true);
                    } else {
                        Toast.makeText(getActivity(), getResources().getString(R.string.no_connection_to_network), Toast.LENGTH_LONG).show();
                    }
                }
            });
            rowForEmployeeSchedule.addView(refreshImageView);

            rowForEmployeeSchedule.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TableRow selectedTableRow = (TableRow) v;
                    Integer rowNumber = (Integer) selectedTableRow.getTag();
                    String selectedEmployee = schedulesForEmployee.get(rowNumber);
                    updateDefaultEmployee(selectedEmployee, false);
                    mListener.onChangeFragment(AvailableFragments.ShowSchedules);
                }
            });

            rowForEmployeeSchedule.setTag(currentRowNumber);
            currentRowNumber++;
            tableLayout.addView(rowForEmployeeSchedule);
        }
    }

    private void deleteDefaultEmployeeIfNeed(String employeeName){
        String settingFileName = getActivity().getString(R.string.setting_file_name);
        final SharedPreferences preferences = getActivity().getSharedPreferences(settingFileName, 0);
        final SharedPreferences.Editor editor = preferences.edit();
        String employeeFieldInSettings = getActivity().getString(R.string.default_employee_field_in_settings);
        String defaultEmployeeName = preferences.getString(employeeFieldInSettings, "none");
        employeeName = employeeName.substring(0, employeeName.length() - 4);
        if(employeeName.equalsIgnoreCase(defaultEmployeeName)){
            editor.remove(employeeFieldInSettings);

        }
        editor.remove(employeeName);
        editor.apply();
    }

    private void updateDefaultEmployee(Employee employee, boolean isDownloadedSchedule){
        String settingFileName = getActivity().getString(R.string.setting_file_name);
        final SharedPreferences preferences = getActivity().getSharedPreferences(settingFileName, 0);
        final SharedPreferences.Editor editor = preferences.edit();
        String employeeFieldInSettings = getActivity().getString(R.string.default_employee_field_in_settings);
        String groupFiledInSettings = getActivity().getString(R.string.default_group_field_in_settings);
        editor.putString(groupFiledInSettings, "none");
        String employeeForPreferences = getFileNameForEmployeeSchedule(employee);
        editor.putString(employeeFieldInSettings, employeeForPreferences);
        if(isDownloadedSchedule) {
            editor.putString(employeeForPreferences, DateUtil.getCurrentDateAsString());
        }
        editor.apply();
    }

    private void updateDefaultEmployee(String defaultEmployee, boolean isDownloadedSchedule){
        if(".xml".equalsIgnoreCase(defaultEmployee.substring(defaultEmployee.length() -4))) {
            defaultEmployee = defaultEmployee.substring(0, defaultEmployee.length() - 4);
        }
        String settingFileName = getActivity().getString(R.string.setting_file_name);
        final SharedPreferences preferences = getActivity().getSharedPreferences(settingFileName, 0);
        final SharedPreferences.Editor editor = preferences.edit();
        String employeeFieldInSettings = getActivity().getString(R.string.default_employee_field_in_settings);
        String currentDefaultEmployee = preferences.getString(employeeFieldInSettings, "none");
        if(!defaultEmployee.equalsIgnoreCase(currentDefaultEmployee)){
            editor.putString(employeeFieldInSettings, defaultEmployee);
            String groupFiledInSettings = getActivity().getString(R.string.default_group_field_in_settings);
            editor.putString(groupFiledInSettings, "none");
        }
        if(isDownloadedSchedule) {
            editor.putString(defaultEmployee, DateUtil.getCurrentDateAsString());
        }
        editor.apply();
    }

    private String getLastUpdateFromPreference(String schedulesName){
        schedulesName = schedulesName.substring(0, schedulesName.length() - 4);
        String settingFileName = getActivity().getString(R.string.setting_file_name);
        final SharedPreferences preferences = getActivity().getSharedPreferences(settingFileName, 0);
        return preferences.getString(schedulesName, "-");
    }

    private String getEmployeeNameFromString(String passedString){
        Pattern pattern = Pattern.compile(PATTERN);
        Matcher matcher = pattern.matcher(passedString);
        if(matcher.find()){
            return matcher.group(0);
        }
        return "Not found matches";
    }

    private String getFileNameForEmployeeSchedule(Employee employee){
        return employee.getLastName() + employee.getFirstName().charAt(0) + employee.getMiddleName().charAt(0) + employee.getId();
    }

    @Nullable
    private Employee getEmployeeByName(String selectedEmployee){
        for(Employee employee : getAvailableEmployeeList()){
            String employeeAsString = employeeToString(employee);
            if(employeeAsString.equalsIgnoreCase(selectedEmployee)){
                return employee;
            }
        }
        return null;
    }

    private String[] convertEmployeeToArray(List<Employee> employees){
        List<String> resultList = new ArrayList<>();
        for(Employee employee : employees){
            resultList.add(employeeToString(employee));
        }
        String[] resultArray = new String[resultList.size()];
        resultArray = resultList.toArray(resultArray);
        return resultArray;
    }

    private String employeeToString(Employee employee){
        String employeeFIO = employee.getLastName();
        if(employee.getFirstName() != null && employee.getFirstName().length() > 0){
            employeeFIO += " " + employee.getFirstName();
            if(employee.getMiddleName() != null && employee.getMiddleName().length() > 0){
                employeeFIO += " " + employee.getMiddleName();
            }
        }
        return employeeFIO;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public List<Employee> getAvailableEmployeeList() {
        return availableEmployeeList;
    }

    public void setAvailableEmployeeList(List<Employee> availableEmployeeList) {
        this.availableEmployeeList = availableEmployeeList;
    }

    private class DownloadEmployeeXML extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... parameters) {
            try {
                List<Employee> loadedEmployees = LoadSchedule.loadListEmployee();
                setAvailableEmployeeList(loadedEmployees);
                return null;
            } catch (Exception e){
                return e.toString();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if(result == null) {
                    String[] employeesAsArray = convertEmployeeToArray(availableEmployeeList);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, employeesAsArray);
                    AutoCompleteTextView textView = (AutoCompleteTextView) currentView.findViewById(R.id.autoCompleteForEmployee);
                    textView.setAdapter(adapter);
                }
            } catch (Exception e){
                Log.v(TAG, "Exception occurred" + e.toString());
            }
        }
    }

    private class DownloadFilesTask extends AsyncTask<String, String, String> {
        private File filesDir;

        protected String doInBackground(String... employeeName) {
            return LoadSchedule.loadScheduleForEmployee(employeeName[0], filesDir);
        }

        protected void onPostExecute(String result) {
            if(result != null) {
                //Toast.makeText(ScheduleForGroup.this, result, Toast.LENGTH_SHORT).show();
                Toast.makeText(getActivity(), getString(R.string.error_while_downloading_schedule), Toast.LENGTH_LONG).show();
            } else {
                if(isDownloadingNewSchedule()) {
                    mListener.onChangeFragment(AvailableFragments.ShowSchedules);
                } else{
                    populateTableLayout(getTableLayoutForDownloadedSchedules(), getDownloadedSchedules());
                }
            }
        }
    }

    public List<String> getDownloadedSchedules() {
        return downloadedSchedules;
    }

    public void setDownloadedSchedules(List<String> downloadedSchedules) {
        this.downloadedSchedules = downloadedSchedules;
    }

    public boolean isDownloadingNewSchedule() {
        return isDownloadingNewSchedule;
    }

    public void setIsDownloadingNewSchedule(boolean isDownloadingNewSchedule) {
        this.isDownloadingNewSchedule = isDownloadingNewSchedule;
    }

    public TableLayout getTableLayoutForDownloadedSchedules() {
        return tableLayoutForDownloadedSchedules;
    }

    public void setTableLayoutForDownloadedSchedules(TableLayout tableLayoutForDownloadedSchedules) {
        this.tableLayoutForDownloadedSchedules = tableLayoutForDownloadedSchedules;
    }
}
