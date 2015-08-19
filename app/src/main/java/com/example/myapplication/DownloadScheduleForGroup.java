package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myapplication.DataProvider.LoadSchedule;
import com.example.myapplication.Model.AvailableFragments;

import java.io.File;


public class DownloadScheduleForGroup extends Fragment {
    private static final String TAG = "downScheForGroupTAG";
    private static final Integer COUNT_DIGITS_IN_STUDENT_GROUP = 6;

    private OnFragmentInteractionListener mListener;

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
                EditText editText = (EditText) view.findViewById(R.id.editTextForEnterGroup);
                String studentGroup = editText.getText().toString();
                if (!isAppropriateStudentGroup(studentGroup)) {
                    Toast.makeText(getActivity(), R.string.not_appropriate_student_group, Toast.LENGTH_LONG).show();
                } else {
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

            }
        });
        return view;
    }

    private void updateDefaultGroup(String studentGroup){
        String settingFileName = getActivity().getString(R.string.setting_file_name);
        final SharedPreferences preferences = this.getActivity().getSharedPreferences(settingFileName, 0);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putString("defaultGroup", studentGroup);
        editor.putString("defaultEmployee", "none");
        editor.apply();
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
                //Toast.makeText(ScheduleForGroup.this, result, Toast.LENGTH_SHORT).show();
                Log.v(TAG, result);
            } else {
                mListener.onChangeFragment(AvailableFragments.ShowSchedules);
            }
        }
    }

}
