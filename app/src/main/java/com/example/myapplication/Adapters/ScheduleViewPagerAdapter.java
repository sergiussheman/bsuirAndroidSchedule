package com.example.myapplication.Adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.myapplication.Model.SchoolDay;
import com.example.myapplication.Model.SubGroupEnum;
import com.example.myapplication.Model.WeekNumberEnum;
import com.example.myapplication.ScheduleFragmentForGroup;

import java.util.List;

/**
 * Created by iChrome on 01.09.2015.
 */
public class ScheduleViewPagerAdapter extends FragmentStatePagerAdapter {
    private Integer selectedDayPosition;
    private WeekNumberEnum selectedWeekNumber;
    private SubGroupEnum selectedSubGroupNumber;
    private Context context;

    private List<SchoolDay> weekSchedules;

    public ScheduleViewPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int i) {
        ScheduleFragmentForGroup fragment = ScheduleFragmentForGroup.newInstance(getWeekSchedules(), i);
        return fragment;
    }

    @Override
    public int getCount() {
        // For this contrived example, we have a 100-object collection.
        return 7;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "OBJECT " + (position + 1);
    }

    public Integer getSelectedDayPosition() {
        return selectedDayPosition;
    }

    public void setSelectedDayPosition(Integer selectedDayPosition) {
        this.selectedDayPosition = selectedDayPosition;
    }

    public WeekNumberEnum getSelectedWeekNumber() {
        return selectedWeekNumber;
    }

    public void setSelectedWeekNumber(WeekNumberEnum selectedWeekNumber) {
        this.selectedWeekNumber = selectedWeekNumber;
    }

    public SubGroupEnum getSelectedSubGroupNumber() {
        return selectedSubGroupNumber;
    }

    public void setSelectedSubGroupNumber(SubGroupEnum selectedSubGroupNumber) {
        this.selectedSubGroupNumber = selectedSubGroupNumber;
    }

    public List<SchoolDay> getWeekSchedules() {
        return weekSchedules;
    }

    public void setWeekSchedules(List<SchoolDay> weekSchedules) {
        this.weekSchedules = weekSchedules;
    }
}
