package com.example.myapplication.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by iChrome on 24.12.2015.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final Integer DATABASE_VERSION = 0;
    private static final String DATABASE_NAME = "scheduleDB";
    private static final String SUBJECT_TABLE_NAME = "subject";
    private static final String EMPLOYEE_TABLE_NAME = "employee";
    private static final String LESSON_TIME_TABLE_NAME = "lesson_time";
    private static final String SCHEDULE_TABLE_NAME = "schedule";
    private static final String SCHEDULE_EMPLOYEE_TABLE_NAME = "schedule_employee";
    private static final String SCHEDULE_VIEW_NAME = "scheduleView";


    public DBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys=ON;");

        //creating subject table
        String sql = "CREATE TABLE " + SUBJECT_TABLE_NAME + " (" + BaseColumns._ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT, " + DBColumns.NAME_COLUMN + " TEXT);";
        db.execSQL(sql);

        //creating employee table
        sql = "CREATE TABLE " + EMPLOYEE_TABLE_NAME + " (" + BaseColumns._ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT, " + DBColumns.FIRST_NAME_COLUMN + " TEXT, " +
                DBColumns.LAST_NAME_COLUMN + " TEXT, " + DBColumns.MIDDLE_NAME_COLUMN + " TEXT, " +
                DBColumns.DEPARTMENT_COLUMN + " TEXT);";
        db.execSQL(sql);

        //creating lessonTime table
        sql = "CREATE TABLE " + LESSON_TIME_TABLE_NAME + " (" + BaseColumns._ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT, " + DBColumns.START_TIME_COLUMN + " TIME, " +
                DBColumns.END_TIME_COLUMN + " TIME);";
        db.execSQL(sql);

        //creating schedule table
        sql = "CREATE TABLE " + SCHEDULE_TABLE_NAME + " (" + BaseColumns._ID +
                "INTEGER PRIMARY KEY AUTOINCREMENT, " + DBColumns.LESSON_TYPE_COLUMN + " TEXT, " +
                DBColumns.SUBJECT_ID_COLUMN + " INTEGER, " + DBColumns.LESSON_TIME_ID_COLUMN + " INTEGER, " +
                DBColumns.AUDITORY_COLUMN + " TEXT, " + DBColumns.SUBGROUP_COLUMN + " INTEGER DEFAULT 0," +
                DBColumns.WEEK_NUMBER_COLUMN + " TEXT DEFAULT '0', " + DBColumns.WEEK_DAY_COLUMN + "INTEGER, " +
                DBColumns.DATE_COLUMN + " DATE, " + DBColumns.NOTE_COLUMN + " TEXT, " +
                "FOREIGN KEY (" + DBColumns.SUBJECT_ID_COLUMN + ") REFERENCES " + SUBJECT_TABLE_NAME + "(" + BaseColumns._ID + "), " +
                " FOREIGN KEY (" + DBColumns.LESSON_TIME_ID_COLUMN + ") REFERENCES " + LESSON_TIME_TABLE_NAME + "(" + BaseColumns._ID + "));";
        db.execSQL(sql);

        //creating schedule_employee table (manyToMany)
        sql = "CREATE TABLE " + SCHEDULE_EMPLOYEE_TABLE_NAME + " (" + DBColumns.EMPLOYEE_ID_COLUMN +
                "INTEGER PRIMARY KEY, " + DBColumns.SCHEDULE_ID_COLUMN + "INTEGER PRIMARY KEY, " +
                "FOREIGN KEY (" + DBColumns.EMPLOYEE_ID_COLUMN + ") REFERENCES " + EMPLOYEE_TABLE_NAME + "(" + BaseColumns._ID + "), " +
                "FOREIGN KEY (" + DBColumns.SCHEDULE_ID_COLUMN + ") REFERENCES " + SCHEDULE_TABLE_NAME + "(" + BaseColumns._ID + "));";
        db.execSQL(sql);

        //creating schedule view
        sql = "CREATE VIEW " + SCHEDULE_VIEW_NAME + " AS SELECT " + SCHEDULE_TABLE_NAME + "." + BaseColumns._ID + ", " +
                SUBJECT_TABLE_NAME + "." + DBColumns.NAME_COLUMN + ", " + LESSON_TIME_TABLE_NAME + "." + DBColumns.START_TIME_COLUMN + ", " +
                LESSON_TIME_TABLE_NAME + "." + DBColumns.END_TIME_COLUMN + ", " + SCHEDULE_TABLE_NAME + "." + DBColumns.AUDITORY_COLUMN + ", " +
                SCHEDULE_TABLE_NAME + "." + DBColumns.AUDITORY_COLUMN + ", " + SCHEDULE_TABLE_NAME + "." + DBColumns.SUBGROUP_COLUMN + ", " +
                SCHEDULE_TABLE_NAME + "." + DBColumns.WEEK_NUMBER_COLUMN + ", " + SCHEDULE_TABLE_NAME + "." + DBColumns.WEEK_DAY_COLUMN + ", " +
                SCHEDULE_TABLE_NAME + "." + DBColumns.DATE_COLUMN + ", " + SCHEDULE_TABLE_NAME + "." + DBColumns.LESSON_TYPE_COLUMN + ", " +
                SCHEDULE_TABLE_NAME + "." + DBColumns.NOTE_COLUMN + ", " +
                "GROUP_CONCAT(" + EMPLOYEE_TABLE_NAME + "." + DBColumns.LAST_NAME_COLUMN + ", ' ', " +
                "LEFT(" + EMPLOYEE_TABLE_NAME + "." + DBColumns.FIRST_NAME_COLUMN + ", 1), '.', " +
                "LEFT(" + EMPLOYEE_TABLE_NAME + "." + DBColumns.MIDDLE_NAME_COLUMN + ", 1), '.') " +
                "FROM  ((((" + SCHEDULE_TABLE_NAME + " LEFT JOIN " + LESSON_TIME_TABLE_NAME + " ON (" +
                SCHEDULE_TABLE_NAME + "." + DBColumns.LESSON_TIME_ID_COLUMN + " = " +
                LESSON_TIME_TABLE_NAME + "." + BaseColumns._ID + ")) " +
                "LEFT JOIN " + SUBJECT_TABLE_NAME + " ON (" + SUBJECT_TABLE_NAME + "." + BaseColumns._ID + " = " +
                SCHEDULE_TABLE_NAME + "." + DBColumns.SUBJECT_ID_COLUMN + ")) " +
                "LEFT JOIN " + SCHEDULE_EMPLOYEE_TABLE_NAME + " ON (" + SCHEDULE_TABLE_NAME + "." + BaseColumns._ID + " = " +
                SCHEDULE_EMPLOYEE_TABLE_NAME + "." + DBColumns.SCHEDULE_ID_COLUMN + ")) " +
                "LEFT JOIN " + EMPLOYEE_TABLE_NAME + " ON (" + SCHEDULE_EMPLOYEE_TABLE_NAME + "." + DBColumns.EMPLOYEE_ID_COLUMN + " = " +
                EMPLOYEE_TABLE_NAME + "." + BaseColumns._ID + "));";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion >= newVersion) {
            return;
        }

        recreateTables(db);
    }

    public void recreateTables(SQLiteDatabase db) {
        db.execSQL("DROP VIEW IF EXISTS " + SCHEDULE_VIEW_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SCHEDULE_EMPLOYEE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SCHEDULE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + EMPLOYEE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SUBJECT_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + LESSON_TIME_TABLE_NAME);
        onCreate(db);
    }
}
