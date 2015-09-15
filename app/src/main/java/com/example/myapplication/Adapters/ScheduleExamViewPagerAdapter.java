package com.example.myapplication.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.myapplication.ExamScheduleFragment;
import com.example.myapplication.Model.SchoolDay;
import com.example.myapplication.Model.SubGroupEnum;
import com.example.myapplication.Model.WeekNumberEnum;

import java.util.List;

/**
 * Created by iChrome on 01.09.2015.
 */
public class ScheduleExamViewPagerAdapter extends FragmentStatePagerAdapter {
    private Integer selectedDayPosition;
    private WeekNumberEnum selectedWeekNumber;
    private SubGroupEnum selectedSubGroupNumber;

    private List<SchoolDay> allSchedules;

    public ScheduleExamViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        Integer calculatedDayPosition;
        switch(i){
            case 0:
                if(selectedDayPosition == 0){
                    calculatedDayPosition = allSchedules.size() - 1;
                } else {
                    calculatedDayPosition = selectedDayPosition - 1;
                }
                break;
            case 2:
                if(selectedDayPosition == allSchedules.size() - 1){
                    calculatedDayPosition = 0;
                } else {
                    calculatedDayPosition = selectedDayPosition + 1;
                }
                break;
            default:
                calculatedDayPosition = selectedDayPosition;
        }
        ExamScheduleFragment fragment = ExamScheduleFragment.newInstance(getAllSchedules(), calculatedDayPosition);
        return fragment;
    }

    @Override
    public int getCount() {
        return allSchedules.size();
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

    public List<SchoolDay> getAllSchedules() {
        return allSchedules;
    }

    public void setAllSchedules(List<SchoolDay> allSchedules) {
        this.allSchedules = allSchedules;
    }
}
