package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.example.myapplication.DataProvider.LoadSchedule;
import com.example.myapplication.Model.AvailableFragments;
import com.example.myapplication.Model.Employee;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class DownloadScheduleForEmployee extends Fragment {
    private static final String TAG = "employeeDownloadTAG";
    private List<Employee> availableEmployeeList;
    private  View currentView;

    private OnFragmentInteractionListener mListener;

    public static DownloadScheduleForEmployee newInstance() {
        DownloadScheduleForEmployee fragment = new DownloadScheduleForEmployee();
        return fragment;
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
            Toast.makeText(getActivity(), R.string.connection_timeout, Toast.LENGTH_LONG).show();
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
                        String parameterForDownloadSchedule = selectedEmployee.getLastName() + selectedEmployee.getId();
                        DownloadFilesTask task = new DownloadFilesTask();
                        task.filesDir = getActivity().getFilesDir();
                        task.execute(parameterForDownloadSchedule);
                        updateDefaultEmployee(selectedEmployee);
                    } else{
                        Toast.makeText(getActivity(), getResources().getString(R.string.should_select_employee), Toast.LENGTH_LONG).show();
                    }
                } else{
                    Toast.makeText(getActivity(), getResources().getString(R.string.no_connection_to_network), Toast.LENGTH_LONG).show();
                }
            }
        });

        return currentView;
    }

    private void updateDefaultEmployee(Employee employee){
        String settingFileName = getActivity().getString(R.string.setting_file_name);
        final SharedPreferences preferences = getActivity().getSharedPreferences(settingFileName, 0);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putString("defaultGroup", "none");
        String employeeForPreferences = employee.getLastName() + employee.getId();
        editor.putString("defaultEmployee", employeeForPreferences);
        editor.apply();
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
            if(result == null) {
                String[] employeesAsArray = convertEmployeeToArray(availableEmployeeList);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, employeesAsArray);
                AutoCompleteTextView textView = (AutoCompleteTextView) currentView.findViewById(R.id.autoCompleteForEmployee);
                textView.setAdapter(adapter);
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
                Log.v(TAG, result);
            } else {
                mListener.onChangeFragment(AvailableFragments.ShowSchedules);
            }
        }
    }
}
