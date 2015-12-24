package com.example.myapplication.dao;

import android.provider.BaseColumns;

/**
 * Created by iChrome on 24.12.2015.
 */
public class DBColumns {
    //employee table
    public static final String FIRST_NAME_COLUMN = "FIRST_NAME";
    public static final String LAST_NAME_COLUMN = "LAST_NAME";
    public static final String MIDDLE_NAME_COLUMN = "MIDDLE_NAME_COLUMN";
    public static final String DEPARTMENT_COLUMN = "DEPARTMENT";

    //subject
    public static final String NAME_COLUMN = "NAME";

    //lessonTime
    public static final String START_TIME_COLUMN = "START_TIME";
    public static final String END_TIME_COLUMN = "END_TIME";

    //schedule
    public static final String SUBJECT_ID_COLUMN = "ID_SUBJECT";
    public static final String LESSON_TIME_ID_COLUMN = "ID_LESSON_TIME";
    public static final String AUDITORY_COLUMN = "AUDITORY";
    public static final String SUBGROUP_COLUMN = "SUBGROUP";
    public static final String WEEK_NUMBER_COLUMN = "WEEK_NUMBER";
    public static final String WEEK_DAY_COLUMN = "WEEK_DAY";
    public static final String DATE_COLUMN = "DATE";
    public static final String LESSON_TYPE_COLUMN = "LESSON_TYPE";
    public static final String NOTE_COLUMN = "NODE";

    //schedule_employee
    public static final String SCHEDULE_ID_COLUMN = "ID_SCHEDULE";
    public static final String EMPLOYEE_ID_COLUMN = "ID_EMPLOYEE";
}
