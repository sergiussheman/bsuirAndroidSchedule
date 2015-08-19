package com.example.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.myapplication.Model.AvailableFragments;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WhoAreYou#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WhoAreYou extends Fragment {

    private OnFragmentInteractionListener mListener;

    public static WhoAreYou newInstance() {
        WhoAreYou fragment = new WhoAreYou();
        return fragment;
    }

    public WhoAreYou() {
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
        View view = inflater.inflate(R.layout.fragment_who_are_you, null);

        ImageButton buttonForStudentSchedule = (ImageButton) view.findViewById(R.id.studentImageButton);
        ImageButton buttonForEmployeeSchedule = (ImageButton) view.findViewById(R.id.employeeImageButton);

        buttonForStudentSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onChangeFragment(AvailableFragments.DownloadScheduleForGroup);
            }
        });

        buttonForEmployeeSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onChangeFragment(AvailableFragments.DownloadScheduleForEmployee);
            }
        });
        return view;
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

}
