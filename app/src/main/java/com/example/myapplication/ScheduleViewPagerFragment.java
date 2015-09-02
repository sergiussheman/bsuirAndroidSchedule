package com.example.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.example.myapplication.Adapters.ScheduleViewPagerAdapter;
import com.example.myapplication.Model.SchoolDay;
import com.example.myapplication.Model.SubGroupEnum;
import com.example.myapplication.Model.WeekNumberEnum;

import java.util.List;

public class ScheduleViewPagerFragment extends Fragment {
    private ViewPager scheduleViewPager;
    private ScheduleViewPagerAdapter adapter;
    private List<SchoolDay> allWeekSchedules;
    private OnFragmentInteractionListener mListener;

    public static ScheduleViewPagerFragment newInstance(String param1, String param2) {
        ScheduleViewPagerFragment fragment = new ScheduleViewPagerFragment();
        return fragment;
    }

    public ScheduleViewPagerFragment() {
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
        View view = inflater.inflate(R.layout.fragment_schedule_view_pager, container, false);
        scheduleViewPager = (ViewPager) view.findViewById(R.id.scheduleViewPager);
        adapter = new ScheduleViewPagerAdapter(getActivity().getSupportFragmentManager());
        adapter.setWeekSchedules(getAllWeekSchedules());
        adapter.setSelectedWeekNumber(WeekNumberEnum.ALL);
        adapter.setSelectedSubGroupNumber(SubGroupEnum.ENTIRE_GROUP);
        scheduleViewPager.setAdapter(adapter);
        scheduleViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mListener.onChangeDay(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        return view;
    }

    public void updateFiltersForViewPager(Integer dayPosition, WeekNumberEnum weekNumber, SubGroupEnum subGroup){
        adapter = new ScheduleViewPagerAdapter(getActivity().getSupportFragmentManager());
        adapter.setWeekSchedules(getAllWeekSchedules());
        adapter.setSelectedDayPosition(dayPosition);
        adapter.setSelectedWeekNumber(weekNumber);
        adapter.setSelectedSubGroupNumber(subGroup);
        scheduleViewPager.setAdapter(adapter);
        scheduleViewPager.setCurrentItem(dayPosition);
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

    public List<SchoolDay> getAllWeekSchedules() {
        return allWeekSchedules;
    }

    public void setAllWeekSchedules(List<SchoolDay> allWeekSchedules) {
        this.allWeekSchedules = allWeekSchedules;
    }
}
