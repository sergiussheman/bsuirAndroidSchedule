package com.example.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
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
    private static final int PAGE_LEFT = 0;
    private static final int PAGE_MIDDLE = 1;
    private static final int PAGE_RIGHT = 2;
    private Integer currentMiddleIndex;
    private Integer currentSelectedIndex;
    private WeekNumberEnum selectedWeekNumber = WeekNumberEnum.ALL;
    private SubGroupEnum selectedSubGroup = SubGroupEnum.ENTIRE_GROUP;

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
        scheduleViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentSelectedIndex = position;
                if (currentSelectedIndex == PAGE_LEFT) {
                    if(currentMiddleIndex == 0){
                        currentMiddleIndex = 6;
                    } else {
                        currentMiddleIndex--;
                    }
                    // user swiped to right direction
                } else if (currentSelectedIndex == PAGE_RIGHT) {

                    if(currentMiddleIndex == 6){
                        currentMiddleIndex = 0;
                    } else{
                        currentMiddleIndex++;
                    }

                }
                mListener.onChangeDay(currentMiddleIndex);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE && currentSelectedIndex != null) {
                    updateFiltersForViewPager(currentMiddleIndex, selectedWeekNumber, selectedSubGroup);
                }
            }
        });

        updateFiltersForViewPager(getCurrentMiddleIndex(), getSelectedWeekNumber(), getSelectedSubGroup());
        return view;
    }

    public Void updateFiltersForViewPager(Integer dayPosition, WeekNumberEnum weekNumber, SubGroupEnum subGroup) {
        selectedWeekNumber = weekNumber;
        selectedSubGroup = subGroup;
        adapter = new ScheduleViewPagerAdapter(getActivity().getSupportFragmentManager());
        adapter.setWeekSchedules(getAllWeekSchedules());
        currentMiddleIndex = dayPosition;
        adapter.setSelectedDayPosition(dayPosition);
        adapter.setSelectedWeekNumber(weekNumber);
        adapter.setSelectedSubGroupNumber(subGroup);
        scheduleViewPager.setAdapter(adapter);
        scheduleViewPager.setCurrentItem(PAGE_MIDDLE);
        return null;
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

    public Integer getCurrentMiddleIndex() {
        return currentMiddleIndex;
    }

    public void setCurrentMiddleIndex(Integer currentMiddleIndex) {
        this.currentMiddleIndex = currentMiddleIndex;
    }

    public SubGroupEnum getSelectedSubGroup() {
        return selectedSubGroup;
    }

    public void setSelectedSubGroup(SubGroupEnum selectedSubGroup) {
        this.selectedSubGroup = selectedSubGroup;
    }

    public WeekNumberEnum getSelectedWeekNumber() {
        return selectedWeekNumber;
    }

    public void setSelectedWeekNumber(WeekNumberEnum selectedWeekNumber) {
        this.selectedWeekNumber = selectedWeekNumber;
    }
}
