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
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Utils.DateUtil;
import com.example.myapplication.Utils.FileUtil;
import com.example.myapplication.DataProvider.LoadSchedule;
import com.example.myapplication.Model.AvailableFragments;

import java.io.File;
import java.util.List;


public class DownloadScheduleForGroup extends Fragment {
    private static final String TAG = "downScheForGroupTAG";
    private static final Integer COUNT_DIGITS_IN_STUDENT_GROUP = 6;

    private OnFragmentInteractionListener mListener;
    private List<String> downloadedSchedulesForGroup;
    private boolean isDownloadingNewSchedule;
    private TableLayout tableLayoutForDownloadedSchedules;

    public static DownloadScheduleForGroup newInstance() {
        DownloadScheduleForGroup fragment = new DownloadScheduleForGroup();
        return fragment;
    }

    public DownloadScheduleForGroup() {
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
        final View view = inflater.inflate(R.layout.fragment_download_schedule_for_group, container, false);

        Button button = (Button) view.findViewById(R.id.buttonForDownloadSchedule);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setIsDownloadingNewSchedule(true);
                EditText editText = (EditText) view.findViewById(R.id.editTextForEnterGroup);
                String studentGroup = editText.getText().toString();
                if (!isAppropriateStudentGroup(studentGroup)) {
                    Toast.makeText(getActivity(), R.string.not_appropriate_student_group, Toast.LENGTH_LONG).show();
                } else {
                    downloadOrUpdateSchedule(studentGroup);
                }
            }
        });

        TableLayout tableLayout = (TableLayout) view.findViewById(R.id.tableLayoutForGroup);
        setTableLayoutForDownloadedSchedules(tableLayout);
        setDownloadedSchedulesForGroup(FileUtil.getAllDownloadedSchedules(getActivity(), true));
        populateTableLayout(tableLayout, getDownloadedSchedulesForGroup());
        return view;
    }

    private void downloadOrUpdateSchedule(String studentGroup){
        ConnectivityManager connectMan = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectMan.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            DownloadFilesTask downloadFilesTask = new DownloadFilesTask();
            downloadFilesTask.fileDir = getActivity().getFilesDir();
            downloadFilesTask.execute(studentGroup);
            updateDefaultGroup(studentGroup);
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.no_connection_to_network), Toast.LENGTH_LONG).show();
        }
    }

    public void populateTableLayout(final TableLayout tableLayout, final List<String> schedulesForGroup){
        Integer currentRowNumber = 0;
        tableLayout.removeAllViews();
        tableLayout.setPadding(5, 0, 5, 0);
        TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);

        TableRow headerRow = new TableRow(getActivity());
        TextView groupHeaderTextView = new TextView(getActivity());
        groupHeaderTextView.setText(getResources().getString(R.string.group_name));
        groupHeaderTextView.setGravity(Gravity.START);
        groupHeaderTextView.setLayoutParams(params);
        groupHeaderTextView.setTypeface(null, Typeface.BOLD);
        headerRow.addView(groupHeaderTextView);

        TextView lastUpdateHeader = new TextView(getActivity());
        lastUpdateHeader.setText(getResources().getString(R.string.last_updated));
        lastUpdateHeader.setTypeface(null, Typeface.BOLD);
        lastUpdateHeader.setPadding(5, 0, 5, 0);
        headerRow.addView(lastUpdateHeader);

        TextView deleteHeader = new TextView(getActivity());
        deleteHeader.setText(getResources().getString(R.string.delete));
        deleteHeader.setTypeface(null, Typeface.BOLD);
        deleteHeader.setGravity(Gravity.CENTER);
        deleteHeader.setPadding(5, 0, 5, 0);
        headerRow.addView(deleteHeader);

        TextView refreshHeader = new TextView(getActivity());
        refreshHeader.setText(getResources().getString(R.string.refresh));
        refreshHeader.setTypeface(null, Typeface.BOLD);
        refreshHeader.setGravity(Gravity.CENTER);
        refreshHeader.setPadding(5,0,5,0);
        headerRow.addView(refreshHeader);

        headerRow.setPadding(0,0,0,18);
        tableLayout.addView(headerRow);

        for(String currentGroupSchedule : schedulesForGroup) {
            TableRow rowForGroupSchedule = new TableRow(getActivity());
            TextView textViewForGroupName = new TextView(getActivity());
            textViewForGroupName.setGravity(Gravity.START);
            textViewForGroupName.setText(currentGroupSchedule);
            textViewForGroupName.setLayoutParams(params);
            rowForGroupSchedule.addView(textViewForGroupName);

            TextView lastUpdatedTextView = new TextView(getActivity());
            lastUpdatedTextView.setText(getLastUpdateFromPreference(currentGroupSchedule));
            lastUpdatedTextView.setPadding(5, 0, 5, 0);
            rowForGroupSchedule.addView(lastUpdatedTextView);

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
                                    String fileNameForDelete = schedulesForGroup.get(rowNumber);

                                    File file = new File(getActivity().getFilesDir(), fileNameForDelete);
                                    if (!file.delete()) {
                                        Toast.makeText(getActivity(), R.string.error_while_deleting_file, Toast.LENGTH_LONG).show();
                                    }
                                    deleteDefaultGroupIfNeed(fileNameForDelete);
                                    setDownloadedSchedulesForGroup(FileUtil.getAllDownloadedSchedules(getActivity(), true));
                                    populateTableLayout(tableLayout, getDownloadedSchedulesForGroup());
                                }
                            })
                            .setNegativeButton(android.R.string.no, null).show();
                }
            });
            rowForGroupSchedule.addView(deleteImageView);

            ImageView refreshImageView = new ImageView(getActivity());
            refreshImageView.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_action_refresh));
            refreshImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TableRow selectedRow = (TableRow) v.getParent();
                    Integer rowNumber = (Integer) selectedRow.getTag();
                    String fileNameForRefresh = schedulesForGroup.get(rowNumber);
                    fileNameForRefresh = fileNameForRefresh.substring(0, fileNameForRefresh.length() - 4);
                    setIsDownloadingNewSchedule(false);
                    downloadOrUpdateSchedule(fileNameForRefresh);
                }
            });
            rowForGroupSchedule.addView(refreshImageView);

            rowForGroupSchedule.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TableRow selectedTableRow = (TableRow) v;
                    Integer rowNumber = (Integer) selectedTableRow.getTag();
                    String selectedGroup = schedulesForGroup.get(rowNumber);
                    updateDefaultGroup(selectedGroup);
                    mListener.onChangeFragment(AvailableFragments.ShowSchedules);
                }
            });

            rowForGroupSchedule.setTag(currentRowNumber);
            currentRowNumber++;
            tableLayout.addView(rowForGroupSchedule);
        }
    }

    private void deleteDefaultGroupIfNeed(String groupName){
        String settingFileName = getActivity().getString(R.string.setting_file_name);
        final SharedPreferences preferences = getActivity().getSharedPreferences(settingFileName, 0);
        final SharedPreferences.Editor editor = preferences.edit();
        String groupFiledInSettings = getActivity().getString(R.string.default_group_field_in_settings);
        String defaultGroupName = preferences.getString(groupFiledInSettings, "none");
        groupName = groupName.substring(0, groupName.length() - 4);
        if(groupName.equalsIgnoreCase(defaultGroupName)){
            editor.remove(groupFiledInSettings);
        }
        editor.remove(groupName);
        editor.apply();
    }

    private void updateDefaultGroup(String studentGroup){
        if(".xml".equalsIgnoreCase(studentGroup.substring(studentGroup.length() - 4))){
            studentGroup = studentGroup.substring(0, studentGroup.length() - 4);
        }
        String settingFileName = getActivity().getString(R.string.setting_file_name);
        final SharedPreferences preferences = this.getActivity().getSharedPreferences(settingFileName, 0);
        final SharedPreferences.Editor editor = preferences.edit();
        String employeeFieldInSettings = getActivity().getString(R.string.default_employee_field_in_settings);
        String groupFieldInSettings = getActivity().getString(R.string.default_group_field_in_settings);
        editor.putString(groupFieldInSettings, studentGroup);
        editor.putString(employeeFieldInSettings, "none");
        editor.putString(studentGroup, DateUtil.getCurrentDateAsString());
        editor.apply();
    }

    private String getLastUpdateFromPreference(String schedulesName){
        schedulesName = schedulesName.substring(0, schedulesName.length() - 4);
        String settingFileName = getActivity().getString(R.string.setting_file_name);
        final SharedPreferences preferences = getActivity().getSharedPreferences(settingFileName, 0);
        return preferences.getString(schedulesName, "-");
    }

    private boolean isAppropriateStudentGroup(String studentGroup){
        Integer countDigits = studentGroup.length();
        return countDigits.equals(COUNT_DIGITS_IN_STUDENT_GROUP);
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

    private class DownloadFilesTask extends AsyncTask<String, String, String> {
        private File fileDir;

        protected String doInBackground(String... urls) {
            return LoadSchedule.loadScheduleForStudentGroup(urls[0], fileDir);
        }

        protected void onPostExecute(String result) {
            if(result != null) {
                Log.v(TAG, result);
            } else {
                if(isDownloadingNewSchedule()) {
                    mListener.onChangeFragment(AvailableFragments.ShowSchedules);
                } else{
                    populateTableLayout(getTableLayoutForDownloadedSchedules(), getDownloadedSchedulesForGroup());
                }
            }
        }
    }

    public List<String> getDownloadedSchedulesForGroup() {
        return downloadedSchedulesForGroup;
    }

    public void setDownloadedSchedulesForGroup(List<String> downloadedSchedulesForGroup) {
        this.downloadedSchedulesForGroup = downloadedSchedulesForGroup;
    }

    public TableLayout getTableLayoutForDownloadedSchedules() {
        return tableLayoutForDownloadedSchedules;
    }

    public void setTableLayoutForDownloadedSchedules(TableLayout tableLayoutForDownloadedSchedules) {
        this.tableLayoutForDownloadedSchedules = tableLayoutForDownloadedSchedules;
    }

    public boolean isDownloadingNewSchedule() {
        return isDownloadingNewSchedule;
    }

    public void setIsDownloadingNewSchedule(boolean isDownloadingNewSchedule) {
        this.isDownloadingNewSchedule = isDownloadingNewSchedule;
    }
}
