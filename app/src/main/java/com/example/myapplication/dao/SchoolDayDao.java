package com.example.myapplication.dao;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.myapplication.model.Employee;
import com.example.myapplication.model.Schedule;
import com.example.myapplication.model.SchoolDay;
import com.example.myapplication.model.WeekDayEnum;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

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

    public List<SchoolDay> getScheduleForTeacher(String  teacherName) {
        List<SchoolDay> weekSchedule = new ArrayList<>();
        int dayNum = 0;
        String[] scheduleId = null;
        String studentGroupName = teacherName.substring(0, 6);

        List<Cursor> scheduleTableCursor = new ArrayList<>();
        String scheduleTableQuery = "select * from schedule where id_student_group = "
                + getGroupId(studentGroupName);

        scheduleTableCursor = getDbHelper().getData(scheduleTableQuery);

        scheduleTableCursor.get(0).moveToLast();
        dayNum = scheduleTableCursor.get(0).getInt(scheduleTableCursor.get(0).
                getColumnIndex(DBColumns.WEEK_DAY_COLUMN));

        for (int i = 0; i < dayNum; i++) {
            weekSchedule.add(fillSchoolDayForTeacher(scheduleId, (long) (i + 1)));
        }

        for (SchoolDay sd: weekSchedule) {
            Log.d("SCHEDULE DAY = ", sd.getDayName());
            for (Schedule schedule: sd.getSchedules()) {
                Log.d("schedule = ", schedule.getLessonTime() + " " +
                        schedule.getLessonType() + " " +
                        schedule.getStudentGroup() + " " +
                        schedule.getSubject() + " " +
                        schedule.getSubGroup() + " " +
                        schedule.getWeekNumbers() + " " +
                        schedule.getWeekDay() + " ");
            }
        }


        return weekSchedule;
    }

    public List<SchoolDay> getScheduleForStudentGroup(String groupFileName){
        List<SchoolDay> weekSchedule = new ArrayList<>();
        int dayNum = 0;
        String studentGroupName = groupFileName.substring(0, 6);

        List<Cursor> scheduleTableCursor = new ArrayList<>();
        String scheduleTableQuery = "select * from schedule where id_student_group = "
                + getGroupId(studentGroupName);

        scheduleTableCursor = getDbHelper().getData(scheduleTableQuery);

        scheduleTableCursor.get(0).moveToLast();
        dayNum = scheduleTableCursor.get(0).getInt(scheduleTableCursor.get(0).
                getColumnIndex(DBColumns.WEEK_DAY_COLUMN));

        for (int i = 0; i < dayNum; i++) {
            weekSchedule.add(fillSchoolDayForGroup(studentGroupName, (long) (i + 1)));
        }

        for (SchoolDay sd: weekSchedule) {
            Log.d("SCHEDULE DAY = ", sd.getDayName());
            for (Schedule schedule: sd.getSchedules()) {
                Log.d("schedule = ", schedule.getLessonTime() + " " +
                        schedule.getLessonType() + " " +
                        schedule.getStudentGroup() + " " +
                        schedule.getSubject() + " " +
                        schedule.getSubGroup() + " " +
                        schedule.getWeekNumbers() + " " +
                        schedule.getWeekDay() + " ");
            }
        }


        return weekSchedule;
    }

    private SchoolDay fillSchoolDayForTeacher(String[] scheduleId, Long weekDay) {


        //TODO: need to create method that will be fill <SchoolDay> for teacher

        return null;
    }

    private SchoolDay fillSchoolDayForGroup(String studentGroupName, Long weekDay) {

        List<SchoolDay> weekSchedule = new ArrayList<>();

        SchoolDay currentSchoolDay = new SchoolDay();
        Schedule currentSchedule = new Schedule();
        List<Schedule> currentScheduleList = new ArrayList<>();

        List<Cursor> scheduleTableCursor = new ArrayList<>();
        String scheduleTableQuery = "select * from schedule where id_student_group = "
                + getGroupId(studentGroupName) + " and " + "week_day = " + weekDay;
        scheduleTableCursor = getDbHelper().getData(scheduleTableQuery);

        String lessonTime = null;
        String lessonType = null;
        String subject = null;
        String subGroup = null;
        String[] weekNumbers = null;
        List<Employee> employees = new ArrayList<>();
        List<String> auds = new ArrayList<>();

        if (scheduleTableCursor.isEmpty()) {
            Log.d("cursor", " is empty");
        }
        else {
            scheduleTableCursor.get(0).moveToFirst();
        }
        int rowNum = 0;

        //fill SchoolDay
        do {
            currentSchedule = new Schedule();
            if ((lessonTime = getLessonTimeById(getLessonTimeIdFromScheduleTable(rowNum, scheduleTableCursor))) != null) {
                currentSchedule.setLessonTime(lessonTime);
            }
            else currentSchedule.setLessonTime(null);

            if ((lessonType = getLessonTypeFromScheduleTable(rowNum, scheduleTableCursor)) != null) {
                currentSchedule.setLessonType(lessonType);
            }
            else currentSchedule.setLessonType(null);

            if ((subject = getSubjectById(getSubjectIdFromScheduleTable(rowNum, scheduleTableCursor))) != null) {
                currentSchedule.setSubject(subject);
            }
            else currentSchedule.setSubject(null);

            if ((subGroup = getSubGroupFromScheduleTable(rowNum, scheduleTableCursor)) != null)  {
                currentSchedule.setSubGroup(subGroup);
            }
            else currentSchedule.setSubGroup("");

            currentSchedule.setStudentGroup(studentGroupName);

            if ((weekDay = getWeekDayFromScheduleTable(rowNum, scheduleTableCursor)) != null) {
                currentSchedule.setWeekDay(weekDay);
            }
            else currentSchedule.setWeekDay(null);

            if ((weekNumbers = getWeekNumsFromScheduleTable(rowNum, scheduleTableCursor)) != null) {
                currentSchedule.setWeekNumbers(Arrays.asList(weekNumbers));
            }
            else currentSchedule.setWeekNumbers(null);

            Log.d("day num = ", weekDay + "");
            if ((employees = getEmployeeForScheduleTableRow(rowNum, scheduleTableCursor)) != null) {
                currentSchedule.setEmployeeList(employees);
            }
            else currentSchedule.setEmployeeList(null);

            if ((auds = getAudsFroScheduleTableRow(rowNum, scheduleTableCursor)) != null) {
                currentSchedule.setAuditories(auds);
            }
            else currentSchedule.setAuditories(Arrays.asList(""));

            currentScheduleList.add(currentSchedule);
            rowNum++;
        } while (scheduleTableCursor.get(0).moveToNext());

        currentSchoolDay.setSchedules(currentScheduleList);

        switch (weekDay.toString()) {
            case "1": currentSchoolDay.setDayName("Понедельник"); break;
            case "2": currentSchoolDay.setDayName("Вторник"); break;
            case "3": currentSchoolDay.setDayName("Среда"); break;
            case "4": currentSchoolDay.setDayName("Четверг"); break;
            case "5": currentSchoolDay.setDayName("Пятница"); break;
            case "6": currentSchoolDay.setDayName("Суббота"); break;
            default: currentSchoolDay.setDayName("Ошибка");break;
        }


        return currentSchoolDay;
    }

    private String getGroupId(String studentGroupName) {
        String id = null;
        List<Cursor> groupNameCursor = new ArrayList<>();
        String getGroupIdQuery = "select * from student_group where student_group_name = "
                + studentGroupName;
        groupNameCursor = getDbHelper().getData(getGroupIdQuery);
        Log.d("STUDENT GROUP NAME = ", studentGroupName);
        try {
            id = groupNameCursor.get(0).getString(0);
        } catch (NullPointerException ex) {
            id = "2";
        }
        Log.d("selected group id = ", id);
        return id;
    }

    private String[] getWeekNumsFromScheduleTable(Integer rowNum, List<Cursor> c) {
        List<Cursor> cursor = c;
        String[] weekNums = null;

        cursor.get(0).moveToFirst();
        cursor.get(0).moveToPosition(rowNum);
        weekNums = cursor.get(0).getString(cursor.get(0).getColumnIndex(DBColumns.WEEK_NUMBER_COLUMN)).split("\\, ");
        return weekNums;
    }

    private Long getWeekDayFromScheduleTable(Integer rowNum, List<Cursor> c) {
        List<Cursor> cursor = c;

        cursor.get(0).moveToFirst();
        cursor.get(0).moveToPosition(rowNum);
        return cursor.get(0).getLong(cursor.get(0).getColumnIndex(DBColumns.WEEK_DAY_COLUMN));
    }

    private String getSubGroupFromScheduleTable(Integer rowNum, List<Cursor> c) {
        List<Cursor> cursor = c;

        cursor.get(0).moveToFirst();
        cursor.get(0).moveToPosition(rowNum);
        return cursor.get(0).getString(cursor.get(0).getColumnIndex(DBColumns.SUBGROUP_COLUMN));
    }

    private String getLessonTypeFromScheduleTable(Integer rowNum, List<Cursor> c) {
        List<Cursor> cursor = c;

        cursor.get(0).moveToFirst();
        cursor.get(0).moveToPosition(rowNum);
        return cursor.get(0).getString(cursor.get(0).getColumnIndex(DBColumns.LESSON_TYPE_COLUMN));
    }

    private String getSubjectIdFromScheduleTable(Integer rowNum, List<Cursor> c) {
        List<Cursor> subjCursor = c;

        subjCursor.get(0).moveToFirst();
        subjCursor.get(0).moveToPosition(rowNum);
        return subjCursor.get(0).getString(subjCursor.get(0).getColumnIndex(DBColumns.SUBJECT_ID_COLUMN));
    }

    private String getLessonTimeIdFromScheduleTable(Integer rowNum, List<Cursor> c) {
        List<Cursor> cursor = c;

        cursor.get(0).moveToFirst();
        cursor.get(0).moveToPosition(rowNum);
        return cursor.get(0).getString(cursor.get(0).getColumnIndex(DBColumns.LESSON_TIME_ID_COLUMN));
    }

    private String getGroupNamebyId(String id) {
        List<Cursor> groupNameCursor;
        String getGroupNameQuery = "select * from student_group where _id = " + id;

        groupNameCursor = getDbHelper().getData(getGroupNameQuery);
        return groupNameCursor.get(0).getString(groupNameCursor.get(0).getColumnCount() - 1);
    }

    private Employee getEmployeeById(String id) {
        try {
            List<Cursor> getEmployeeCursor = new ArrayList<>();
            Employee employee = new Employee();
            String getEmployeeQuery = "select * from employee where _id = " + id;

            getEmployeeCursor = getDbHelper().getData(getEmployeeQuery);
            for (int i = 0; i < getEmployeeCursor.get(0).getColumnCount(); i++) {
                switch (getEmployeeCursor.get(0).getColumnName(i)) {
                    case DBColumns.FIRST_NAME_COLUMN:
                        if (getEmployeeCursor.get(0).getString(i) != null) {
                            employee.setFirstName(getEmployeeCursor.get(0).getString(i));
                        } else employee.setFirstName(null);
                        break;
                    case DBColumns.LAST_NAME_COLUMN:
                        if (getEmployeeCursor.get(0).getString(i) != null) {
                            employee.setLastName(getEmployeeCursor.get(0).getString(i));
                        } else employee.setLastName(null);
                        break;
                    case DBColumns.MIDDLE_NAME_COLUMN:
                        if (getEmployeeCursor.get(0).getString(i) != null) {
                            employee.setMiddleName(getEmployeeCursor.get(0).getString(i));
                        } else employee.setMiddleName(null);
                        break;
                    case DBColumns.DEPARTMENT_COLUMN:
                        if (getEmployeeCursor.get(0).getString(i) != null) {
                            employee.setDepartment(getEmployeeCursor.get(0).getString(i));
                        } else employee.setDepartment(null);
                        break;
                    case BaseColumns._ID:
                        if (getEmployeeCursor.get(0).getString(i) != null) {
                            employee.setId(getEmployeeCursor.get(0).getLong(i));
                        } else employee.setId(null);
                        break;
                    default:
                        return employee;

                }
            }
            Log.d("employee string = ", employee.getId() + employee.getDepartment() +
                    employee.getFirstName() + employee.getLastName() + employee.getMiddleName());

            return employee;
        } catch (NullPointerException ex) {
            return null;
        }
    }

    private List<Employee> getEmployeeForScheduleTableRow(Integer rowNum, List<Cursor> c) {
        Log.d("rowNum = ", rowNum + "");
        List<Employee> employee = new ArrayList<>();
        List<Cursor> cursor = c;
        String scheduleId = null;

        c.get(0).moveToFirst();
        c.get(0).moveToPosition(rowNum);

        scheduleId = c.get(0).getString(0);


        List<Cursor> empCursor = new ArrayList<>();
        String query = "select * from schedule_employee where se_id_schedule = " + scheduleId;
        empCursor = getDbHelper().getData(query);
        try {
            employee.add(getEmployeeById(empCursor.get(0).getString(empCursor.get(0).getColumnIndex(
                    DBColumns.SE_EMPLOYEE_ID_COLUMN))));
        } catch (NullPointerException ex) {
            return new ArrayList<>();
        }

        return employee;
    }

    private String getSubjectById(String id) {
        List<Cursor> getSubjCursor;
        String getSubjQuery = "select * from subject where _id = " + id;

        getSubjCursor = getDbHelper().getData(getSubjQuery);
        return getSubjCursor.get(0).getString(getSubjCursor.get(0).getColumnCount() - 1);
    }

    private String getLessonTimeById(String id) {
        List<Cursor> getTimeCursor;
        String getEmployeeQuery = "select * from lesson_time where _id = " + id;

        getTimeCursor = getDbHelper().getData(getEmployeeQuery);
        return getTimeCursor.get(0).getString(getTimeCursor.get(0).getColumnCount() - 1);
    }

    private String getAudById(String id) {
        List<Cursor> cursor;
        String query = "select * from auditory where _id = " + id;

        cursor = getDbHelper().getData(query);
        return cursor.get(0).getString(cursor.get(0).getColumnIndex(DBColumns.AUDITORY_NAME_COLUMN));
    }

    private List<String> getAudsFroScheduleTableRow(Integer rowNum, List<Cursor> c) {
        List<String> auds = new ArrayList<>();
        String scheduleId = null;

        c.get(0).moveToFirst();
        c.get(0).moveToPosition(rowNum);

        scheduleId = c.get(0).getString(0);

        List<Cursor> audCursor = new ArrayList<>();
        String query = "select * from schedule_auditory where sa_id_schedule = " + scheduleId;
        audCursor = getDbHelper().getData(query);
        try {
            auds.add(getAudById(audCursor.get(0).getString(audCursor.get(0).getColumnIndex(
                    DBColumns.SA_AUDITORY_ID_COLUMN))));
        } catch (NullPointerException ex) {
            List<String> empty = new ArrayList<>();
            return empty;
        }

        return auds;
    }


    public DBHelper getDbHelper() {
        return dbHelper;
    }

    public void setDbHelper(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }
}
