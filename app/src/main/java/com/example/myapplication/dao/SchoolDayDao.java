package com.example.myapplication.dao;

import com.example.myapplication.model.Schedule;
import com.example.myapplication.model.SchoolDay;
import com.example.myapplication.model.WeekDayEnum;

import java.util.List;

/**
 * Created by iChrome on 29.12.2015.
 */
public class SchoolDayDao {
    private DBHelper dbHelper;

    public SchoolDayDao(DBHelper helper){
        setDbHelper(helper);
    }

    public void saveSchoolWeekToDataBase(List<SchoolDay> week){
        for(SchoolDay schoolDay : week){
            for(Schedule schedule : schoolDay.getSchedules()){
                WeekDayEnum weekDay = WeekDayEnum.getDayByName(schoolDay.getDayName());
                if(weekDay != null) {
                    schedule.setWeekDay((long) weekDay.getOrder());
                }
                getDbHelper().addScheduleToDataBase(schedule);
            }
        }
    }

/*    public List<SchoolDay> getScheduleForStudentGroup(String studentGroupName){

    }*/

    public DBHelper getDbHelper() {
        return dbHelper;
    }

    public void setDbHelper(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }
}
