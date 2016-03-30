package com.example.myapplication.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.example.myapplication.model.Employee;
import com.example.myapplication.model.Schedule;
import com.example.myapplication.model.SchoolDay;
import com.example.myapplication.model.WeekDayEnum;
import com.example.myapplication.utils.DateUtil;
import com.example.myapplication.utils.EmployeeUtil;
import com.example.myapplication.utils.FileUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by iChrome on 29.12.2015.
 */
public class SchoolDayDao {
    private DBHelper dbHelper;
    static final Integer WEEK_WORKDAY_NUM = 6;

    public SchoolDayDao(DBHelper helper){
        setDbHelper(helper);
    }

    public void saveSchoolWeekToDataBase(List<SchoolDay> week){
        for(SchoolDay schoolDay : week){
            for(Schedule schedule : schoolDay.getSchedules()){
                WeekDayEnum weekDay = WeekDayEnum.getDayByName(schoolDay.getDayName());

                //if weekDay == date
                //weekDay == date if schedule is for exam
                if (schoolDay.getDayName().contains(".")) {
                    schedule.setDate(schoolDay.getDayName());
                }

                if(weekDay != null) {

                    schedule.setWeekDay((long) weekDay.getOrder());
                }
                getDbHelper().addScheduleToDataBase(schedule);
            }
        }
    }

    public void updateScheduleForGroup(String rowId, Schedule newRecord) {
        Log.d("Update schedule", "ROW NUM = " + rowId);
        String weekNums = "";
        for (String str: newRecord.getWeekNumbers()) {
            weekNums += str;
            weekNums += ", ";
        }
        weekNums = weekNums.substring(0, weekNums.length() - 2);
        ContentValues values = new ContentValues();
        values.put(DBColumns.LESSON_TIME_ID_COLUMN, newRecord.getLessonTime());
        values.put(DBColumns.LESSON_TYPE_COLUMN, newRecord.getLessonType());
        values.put(DBColumns.WEEK_DAY_COLUMN, newRecord.getLessonType());
        values.put(DBColumns.WEEK_NUMBER_COLUMN, weekNums);
        values.put(DBColumns.SUBJECT_ID_COLUMN, "true");
        values.put(DBColumns.SUBGROUP_COLUMN, newRecord.getSubGroup());

        String selection = BaseColumns._ID + " = " + rowId;


        int count = getDbHelper().getReadableDatabase().update(
                "schedule",
                values,
                selection,
                null);

    }

    //удаляет расписание для группы иди преподавателя
    //возвращает количество удаленных записей
    //если нужно удалить расписание для обновления то не удаляем записи в которых колонка IS_MANUAL == true
    public Integer deleteSchedule(String fileName, boolean isForRefresh) {
        if (FileUtil.isDigit(fileName.charAt(0))) {
            return deleteGroupSchedule(fileName, isForRefresh);
        } else {
            return deleteTeacherSchedule(fileName, isForRefresh);
        }
    }

    private Integer deleteGroupSchedule(String fileName, boolean isForRefresh) {
        List<Cursor> tableCursor = new ArrayList<>();
        String studentGroupName = fileName.substring(0, 6);
        String query = "select * from schedule where id_student_group = "
                + getGroupId(studentGroupName);
        tableCursor = getDbHelper().getData(query);
        Integer deletedRowNum = 0;

        try {
            tableCursor.get(0).moveToFirst();
        } catch (NullPointerException ex) {
            Log.d("DB", "schedule table doesn't contain this group!");
            return -1;
        }

        do {
            if (!isAvailableTeacherId(tableCursor.get(0).getString(0))) {
                if (isForRefresh) {
                    try {
                        if (!tableCursor.get(0).getString(tableCursor.get(0).getColumnIndex(DBColumns.IS_MANUAL))
                                .equalsIgnoreCase("true")) {
                            deleteScheduleTableRow(tableCursor.get(0).getString(0));
                            deletedRowNum++;
                        }
                    } catch (NullPointerException ex) {
                        deleteScheduleTableRow(tableCursor.get(0).getString(0));
                        deletedRowNum++;
                    }
                } else {
                    deleteScheduleTableRow(tableCursor.get(0).getString(0));
                    deletedRowNum++;
                }
            }
        } while (tableCursor.get(0).moveToNext());

        //resetScheduleTableId();

        return deletedRowNum;
    }

    private Integer resetScheduleTableId() {
        List<Cursor> scheduleCursor = new ArrayList<>();
        String query = "select * from schedule";
        scheduleCursor = getDbHelper().getData(query);
        Integer newId = 1;

        try {
            scheduleCursor.get(0).moveToFirst();
        } catch (NullPointerException ex) {
            Log.d("DB", "schedule table doesn't contain this group!");
            return - 1;
        }

        do {
            ContentValues values = new ContentValues();
            values.put(BaseColumns._ID, "" + newId);

            String selection = BaseColumns._ID + " = " + scheduleCursor.get(0).getString(0);

            int count = getDbHelper().getReadableDatabase().update(
                    "schedule",
                    values,
                    selection,
                    null);

            newId++;

        } while(scheduleCursor.get(0).moveToNext());

        return 0;
    }

    private boolean isAvailableTeacherId(String id) {
        List<Cursor> teacherTableCursor = new ArrayList<>();
        String query = "select * from employee where " +
                DBColumns.EMP_SCHEDULE_AVAILABLE + " = true";
        teacherTableCursor = getDbHelper().getData(query);

        try {
            teacherTableCursor.get(0).moveToFirst();
        } catch (NullPointerException ex) {
            Log.d("DB ", "All teachers is unavailable!");
            return false;
        }

        do {
            if (teacherTableCursor.get(0).getString(0).equals(id)) {
                return true;
            }
        } while(teacherTableCursor.get(0).moveToNext());

        return false;
    }

    private boolean isAvailableGroupId(String id) {
        List<Cursor> groupTableCursor = new ArrayList<>();
        String query = "select * from student_group where " +
                BaseColumns._ID + " = " + id;
        groupTableCursor = getDbHelper().getData(query);

        try {
            groupTableCursor.get(0).moveToFirst();
        } catch (NullPointerException ex) {
            Log.d("DB ", "All groups is unavailable!");
            return false;
        }


        if (groupTableCursor.get(0).getString(
                groupTableCursor.get(0).getColumnIndex(
                        DBColumns.GR_SCHEDULE_AVAILABLE)) != null) {
            if (groupTableCursor.get(0).getString(
                    groupTableCursor.get(0).getColumnIndex(
                            DBColumns.GR_SCHEDULE_AVAILABLE)).equals("true")) {
                Log.d("Data Base", groupTableCursor.get(0).getString(groupTableCursor.get(0).getColumnIndex(
                                DBColumns.GR_SCHEDULE_AVAILABLE)));
                return true;
            }
            else return false;
        }
        else return false;
    }

    private Integer deleteTeacherSchedule(String fileName,  boolean isForRefresh) {
        List<Cursor> tableCursor = new ArrayList<>();
        String lastName = EmployeeUtil.getEmployeeLastNameFromFile(fileName);
        String query = "select * from schedule";
        tableCursor = getDbHelper().getData(query);
        Integer deletedRowNum = 0;
        List<String> scheduleId = new ArrayList<>();

        try {
            tableCursor.get(0).moveToFirst();
        } catch (NullPointerException ex) {
            Log.d("DB", "schedule table doesn't contain this group!");
            return -1;
        }

        List<Cursor> cursor = new ArrayList<>();
        query = "select se_id_schedule from schedule_employee where se_id_employee = "
                + getEmployeeId(lastName);

        Log.d("Emp id = ", getEmployeeId(lastName));

        cursor = getDbHelper().getData(query);

        cursor.get(0).moveToFirst();
        do {
            scheduleId.add(cursor.get(0).getString(0));
        } while (cursor.get(0).moveToNext());

        do {
            if (isTeacherId(scheduleId, tableCursor.get(0).getString(0))) {
                if (!isAvailableGroupId(tableCursor.get(0).getString(
                        tableCursor.get(0).getColumnIndex(DBColumns.STUDENT_GROUP_ID_COLUMN)))) {
                    if (isForRefresh) {
                        try {
                            if (!tableCursor.get(0).getString(tableCursor.get(0).getColumnIndex(DBColumns.IS_MANUAL))
                                    .equalsIgnoreCase("true")) {
                                deleteScheduleTableRow(tableCursor.get(0).getString(0));
                                deletedRowNum++;
                            }
                        } catch (NullPointerException ex) {
                            deleteScheduleTableRow(tableCursor.get(0).getString(0));
                            deletedRowNum++;
                        }
                    } else {
                        deleteScheduleTableRow(tableCursor.get(0).getString(0));
                        deletedRowNum++;
                    }
                }
            }
        } while (tableCursor.get(0).moveToNext());

        //resetScheduleTableId();

        return deletedRowNum;
    }

    //удаляет все строки с заданным id
    //метод возвращает кол-во удаленных строк
    public Integer deleteScheduleTableRow(String id) {
        return getDbHelper().getReadableDatabase().delete("schedule", "_id = " + id, null);
    }

    //расписание взависимости от названия
    public List<SchoolDay> getSchedule(String fileName, boolean isForExam) {
        Log.d("Current date", DateUtil.getCurrentDateAsString());
        if (FileUtil.isDigit(fileName.charAt(0))) {
            if (isForExam) {
                return getExamScheduleForStudentGroup(fileName);
            } else {
                return getScheduleForStudentGroup(fileName);
            }
        } else {
            return getScheduleForTeacher(fileName, isForExam);
        }
    }

    //расписание для преподавателей
    private List<SchoolDay> getScheduleForTeacher(String  fileName, boolean isForExam) {
        List<SchoolDay> weekSchedule = new ArrayList<>();
        int dayNum = 0;
        List<String> scheduleId = new ArrayList<>();
        String lastName = EmployeeUtil.getEmployeeLastNameFromFile(fileName);

        List<Cursor> cursor = new ArrayList<>();
        String query = "select se_id_schedule from schedule_employee where se_id_employee = "
                + getEmployeeId(lastName);

        Log.d("Emp id = ", getEmployeeId(lastName));

        cursor = getDbHelper().getData(query);

        cursor.get(0).moveToFirst();
        do {
            scheduleId.add(cursor.get(0).getString(0));
        } while (cursor.get(0).moveToNext());

        String[] buf = scheduleId.toArray(new String[scheduleId.size()]);
        Log.d("sch id = ", buf.length + "");

        if (isForExam) {
            return fillExamScheduleForTeacher(scheduleId, lastName);
        } else {
            for (int i = 0; i < WEEK_WORKDAY_NUM; i++) {
                SchoolDay tmp;
                if ((tmp = fillSchoolDayForTeacher(scheduleId, (long) (i + 1))) != null) {
                    weekSchedule.add(tmp);
                }
            }
        }

            for (SchoolDay sd : weekSchedule) {
                Log.d("SCHEDULE DAY = ", sd.getDayName());
                for (Schedule schedule : sd.getSchedules()) {
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

    private List<SchoolDay> getExamScheduleForStudentGroup(String fileName) {

        List<SchoolDay> weekSchedule = new ArrayList<>();

        SchoolDay currentSchoolDay = new SchoolDay();
        Schedule currentSchedule = new Schedule();
        List<Schedule> currentScheduleList = new ArrayList<>();
        String studentGroupName = fileName.substring(0, 6);
        List<SchoolDay> sdList = new ArrayList<>();


        List<Cursor> scheduleTableCursor = new ArrayList<>();
        String scheduleTableQuery = "select * from schedule where id_student_group = "
                + getGroupId(studentGroupName) + " and " + "week_day IS NULL";
        scheduleTableCursor = getDbHelper().getData(scheduleTableQuery);

        String lessonTime = null;
        String lessonType = null;
        String subject = null;
        String subGroup = null;
        String groupName = null;
        String[] weekNumbers = null;
        String note = null;
        List<Employee> employees = new ArrayList<>();
        List<String> auds = new ArrayList<>();
        String isHidden;
        String date;
        List<String> dateList = new ArrayList<>();

        if (scheduleTableCursor.isEmpty()) {
            Log.d("cursor", " is empty");
        }
        else {
            try {
                scheduleTableCursor.get(0).moveToFirst();
            } catch (NullPointerException ex) {
                return new ArrayList<>();
            }
        }

        int rowNum = 0;
        //fill SchoolDay
        do {
            currentSchedule = new Schedule();
            currentScheduleList = new ArrayList<>();
            currentSchoolDay = new SchoolDay();


            //currentSchedule.setWeekDay(null);
            if ((date = scheduleTableCursor.get(0).getString(scheduleTableCursor.get(0)
                    .getColumnIndex(DBColumns.DATE_COLUMN))) != null) {
                currentSchedule.setDate(date);
            }

            // if schedule for this date was processed continue
            if (dateList.contains(date)) {
                rowNum++;
                continue;
            }

            dateList.add(date);

            List<Cursor> dateCursor = new ArrayList<>();
            dateCursor = getDbHelper().getData("select * from schedule where date = '" + date + "'");

            dateCursor.get(0).moveToFirst();

            int dateRowNum = 0;
            do {
                currentSchedule = new Schedule();
                if ((isHidden = dateCursor.get(0).getString(dateCursor.get(0)
                        .getColumnIndex(DBColumns.IS_HIDDEN))) != null) {
                    if (isHidden.equals("true")) {
                        currentSchedule.setHidden(true);
                    } else currentSchedule.setHidden(false);
                } else currentSchedule.setHidden(false);


                if ((lessonTime = getLessonTimeById(getLessonTimeIdFromScheduleTable(dateRowNum, dateCursor))) != null) {
                    currentSchedule.setLessonTime(lessonTime);
                } else currentSchedule.setLessonTime(null);

                if ((lessonType = getLessonTypeFromScheduleTable(dateRowNum, dateCursor)) != null) {
                    currentSchedule.setLessonType(lessonType);
                } else currentSchedule.setLessonType(null);

                if ((subject = getSubjectById(getSubjectIdFromScheduleTable(dateRowNum, dateCursor))) != null) {
                    currentSchedule.setSubject(subject);
                } else currentSchedule.setSubject(null);

                if ((subGroup = getSubGroupFromScheduleTable(dateRowNum, dateCursor)) != null) {
                    if (!subGroup.equals("0")) {
                        currentSchedule.setSubGroup(subGroup);
                    } else currentSchedule.setSubGroup("");
                } else currentSchedule.setSubGroup("");

                if ((groupName = getGroupNamebyId(getGroupIdFromScheduleTable(dateRowNum, dateCursor))) != null) {
                    currentSchedule.setStudentGroup(groupName);
                } else currentSchedule.setStudentGroup("");

                if ((employees = getEmployeeForScheduleTableRow(dateRowNum, dateCursor)) != null) {
                    currentSchedule.setEmployeeList(employees);
                } else currentSchedule.setEmployeeList(null);

                if ((auds = getAudsForScheduleTableRow(dateRowNum, dateCursor)) != null) {
                    currentSchedule.setAuditories(auds);
                } else currentSchedule.setAuditories(Arrays.asList(""));

                if ((note = getNoteForScheduleTableRow(dateRowNum, dateCursor)) != null) {
                    currentSchedule.setNote(note);
                } else currentSchedule.setNote("");

                currentSchedule.setScheduleTableRowId(dateCursor.get(0).getString(
                        dateCursor.get(0).getColumnIndex(BaseColumns._ID)));

                currentScheduleList.add(currentSchedule);
                dateRowNum++;
            } while (dateCursor.get(0).moveToNext());


            currentSchoolDay.setSchedules(currentScheduleList);
            currentSchoolDay.setDayName(date);

            sdList.add(currentSchoolDay);
            rowNum++;
        } while (scheduleTableCursor.get(0).moveToNext());

       // currentScheduleList = sortScheduleListByTime(currentScheduleList);


        return sdList;
    }

    private List<SchoolDay> fillExamScheduleForTeacher(List<String> scheduleId, String fileName) {

        List<SchoolDay> weekSchedule = new ArrayList<>();

        SchoolDay currentSchoolDay = new SchoolDay();
        Schedule currentSchedule = new Schedule();
        List<Schedule> currentScheduleList = new ArrayList<>();
        String studentGroupName = fileName.substring(0, 6);
        List<SchoolDay> sdList = new ArrayList<>();


        List<Cursor> scheduleTableCursor = new ArrayList<>();
        String scheduleTableQuery = "select * from schedule where week_day IS NULL";
        scheduleTableCursor = getDbHelper().getData(scheduleTableQuery);

        String lessonTime = null;
        String lessonType = null;
        String subject = null;
        String subGroup = null;
        String groupName = null;
        String[] weekNumbers = null;
        String note = null;
        List<Employee> employees = new ArrayList<>();
        List<String> auds = new ArrayList<>();
        String isHidden;
        String date;
        List<String> dateList = new ArrayList<>();

        if (scheduleTableCursor.isEmpty()) {
            Log.d("cursor", " is empty");
        }
        else {
            try {
                scheduleTableCursor.get(0).moveToFirst();
            } catch (NullPointerException ex) {
                return new ArrayList<>();
            }
        }

        int rowNum = 0;
        //fill SchoolDay
        do {
            if (isTeacherId(scheduleId, scheduleTableCursor.get(0).getString(0))) {
                currentSchedule = new Schedule();
                currentScheduleList = new ArrayList<>();
                currentSchoolDay = new SchoolDay();


                //currentSchedule.setWeekDay(null);
                if ((date = scheduleTableCursor.get(0).getString(scheduleTableCursor.get(0)
                        .getColumnIndex(DBColumns.DATE_COLUMN))) != null) {
                    currentSchedule.setDate(date);
                }

                // if schedule for this date was processed continue
                if (dateList.contains(date)) {
                    rowNum++;
                    continue;
                }

                dateList.add(date);

                List<Cursor> dateCursor = new ArrayList<>();
                dateCursor = getDbHelper().getData("select * from schedule where date = '" + date + "'");

                dateCursor.get(0).moveToFirst();

                int dateRowNum = 0;
                do {
                    currentSchedule = new Schedule();
                    if ((isHidden = dateCursor.get(0).getString(dateCursor.get(0)
                            .getColumnIndex(DBColumns.IS_HIDDEN))) != null) {
                        if (isHidden.equals("true")) {
                            currentSchedule.setHidden(true);
                        } else currentSchedule.setHidden(false);
                    } else currentSchedule.setHidden(false);


                    if ((lessonTime = getLessonTimeById(getLessonTimeIdFromScheduleTable(dateRowNum, dateCursor))) != null) {
                        currentSchedule.setLessonTime(lessonTime);
                    } else currentSchedule.setLessonTime(null);

                    if ((lessonType = getLessonTypeFromScheduleTable(dateRowNum, dateCursor)) != null) {
                        currentSchedule.setLessonType(lessonType);
                    } else currentSchedule.setLessonType(null);

                    if ((subject = getSubjectById(getSubjectIdFromScheduleTable(dateRowNum, dateCursor))) != null) {
                        currentSchedule.setSubject(subject);
                    } else currentSchedule.setSubject(null);

                    if ((subGroup = getSubGroupFromScheduleTable(dateRowNum, dateCursor)) != null) {
                        if (!subGroup.equals("0")) {
                            currentSchedule.setSubGroup(subGroup);
                        } else currentSchedule.setSubGroup("");
                    } else currentSchedule.setSubGroup("");

                    if ((groupName = getGroupNamebyId(getGroupIdFromScheduleTable(dateRowNum, dateCursor))) != null) {
                        currentSchedule.setStudentGroup(groupName);
                    } else currentSchedule.setStudentGroup("");

                    if ((employees = getEmployeeForScheduleTableRow(dateRowNum, dateCursor)) != null) {
                        currentSchedule.setEmployeeList(employees);
                    } else currentSchedule.setEmployeeList(null);

                    if ((auds = getAudsForScheduleTableRow(dateRowNum, dateCursor)) != null) {
                        currentSchedule.setAuditories(auds);
                    } else currentSchedule.setAuditories(Arrays.asList(""));

                    if ((note = getNoteForScheduleTableRow(dateRowNum, dateCursor)) != null) {
                        currentSchedule.setNote(note);
                    } else currentSchedule.setNote("");

                    currentSchedule.setScheduleTableRowId(dateCursor.get(0).getString(
                            dateCursor.get(0).getColumnIndex(BaseColumns._ID)));

                    currentScheduleList.add(currentSchedule);
                    dateRowNum++;
                } while (dateCursor.get(0).moveToNext());


                currentSchoolDay.setSchedules(currentScheduleList);
                currentSchoolDay.setDayName(date);

                sdList.add(currentSchoolDay);
            }
            rowNum++;
        } while (scheduleTableCursor.get(0).moveToNext());

        // currentScheduleList = sortScheduleListByTime(currentScheduleList);


        return sdList;
    }

    //расписание для группы
    private List<SchoolDay> getScheduleForStudentGroup(String groupFileName){
        List<SchoolDay> weekSchedule = new ArrayList<>();
        int dayNum = 0;
        Log.d("file name = ", groupFileName);
        String studentGroupName = groupFileName.substring(0, 6);

        List<Cursor> scheduleTableCursor = new ArrayList<>();
        String scheduleTableQuery = "select * from schedule where id_student_group = "
                + getGroupId(studentGroupName);

        scheduleTableCursor = getDbHelper().getData(scheduleTableQuery);

        scheduleTableCursor.get(0).moveToLast();

        for (int i = 0; i < WEEK_WORKDAY_NUM; i++) {
            SchoolDay tmp;
            if ((tmp = fillSchoolDayForGroup(studentGroupName, (long) (i + 1))) != null) {
                weekSchedule.add(tmp);
            }
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

    //распимание для преподавателя на один день
    private SchoolDay fillSchoolDayForTeacher(List<String> scheduleId, Long weekDay) {
        List<SchoolDay> weekSchedule = new ArrayList<>();

        SchoolDay currentSchoolDay = new SchoolDay();
        Schedule currentSchedule = new Schedule();
        List<Schedule> currentScheduleList = new ArrayList<>();

        List<Cursor> scheduleTableCursor = new ArrayList<>();
        String scheduleTableQuery = "select * from schedule where " + "week_day = " + weekDay
                + " order by id_lesson_time";
        scheduleTableCursor = getDbHelper().getData(scheduleTableQuery);

        String lessonTime = null;
        String lessonType = null;
        String subject = null;
        String subGroup = null;
        String groupName = null;
        String[] weekNumbers = null;
        List<Employee> employees = new ArrayList<>();
        List<String> auds = new ArrayList<>();
        String isHidden;

        try {
            if (scheduleTableCursor.isEmpty()) {
                Log.d("cursor", " is empty");
            } else {
                scheduleTableCursor.get(0).moveToFirst();
            }
        } catch (NullPointerException ex) {
            return null;
        }

        int rowNum = 0;

        //fill SchoolDay
        do {
            if (isTeacherId(scheduleId, scheduleTableCursor.get(0).getString(0))) {
                currentSchedule = new Schedule();

                if ((isHidden = scheduleTableCursor.get(0).getString(scheduleTableCursor.get(0)
                        .getColumnIndex(DBColumns.IS_HIDDEN))) != null) {
                    if (isHidden.equals("true")) {
                        currentSchedule.setHidden(true);
                    }
                    else currentSchedule.setHidden(false);
                }
                else currentSchedule.setHidden(false);

                if ((lessonTime = getLessonTimeById(getLessonTimeIdFromScheduleTable(rowNum, scheduleTableCursor))) != null) {
                    currentSchedule.setLessonTime(lessonTime);
                } else currentSchedule.setLessonTime(null);

                if ((lessonType = getLessonTypeFromScheduleTable(rowNum, scheduleTableCursor)) != null) {
                    currentSchedule.setLessonType(lessonType);
                } else currentSchedule.setLessonType(null);

                if ((subject = getSubjectById(getSubjectIdFromScheduleTable(rowNum, scheduleTableCursor))) != null) {
                    currentSchedule.setSubject(subject);
                } else currentSchedule.setSubject(null);

                if ((groupName = getGroupNamebyId(getGroupIdFromScheduleTable(rowNum, scheduleTableCursor))) != null) {
                    currentSchedule.setStudentGroup(groupName);
                } else currentSchedule.setStudentGroup("");

                if ((subGroup = getSubGroupFromScheduleTable(rowNum, scheduleTableCursor)) != null ) {
                    if (!subGroup.equals("0")) {
                        currentSchedule.setSubGroup(subGroup);
                    } else currentSchedule.setSubGroup("");
                } else currentSchedule.setSubGroup("");

                if ((weekDay = getWeekDayFromScheduleTable(rowNum, scheduleTableCursor)) != null) {
                    currentSchedule.setWeekDay(weekDay);
                } else currentSchedule.setWeekDay(null);

                if ((weekNumbers = getWeekNumsFromScheduleTable(rowNum, scheduleTableCursor)) != null) {
                    currentSchedule.setWeekNumbers(Arrays.asList(weekNumbers));
                } else currentSchedule.setWeekNumbers(null);

                Log.d("day num = ", weekDay + "");
                if ((employees = getEmployeeForScheduleTableRow(rowNum, scheduleTableCursor)) != null) {
                    currentSchedule.setEmployeeList(employees);
                } else currentSchedule.setEmployeeList(null);

                if ((auds = getAudsForScheduleTableRow(rowNum, scheduleTableCursor)) != null) {
                    currentSchedule.setAuditories(auds);
                } else currentSchedule.setAuditories(Arrays.asList(""));

                currentSchedule.setScheduleTableRowId(scheduleTableCursor.get(0).getString(
                        scheduleTableCursor.get(0).getColumnIndex(BaseColumns._ID)));

                currentScheduleList.add(currentSchedule);
            }
            rowNum++;
        } while (scheduleTableCursor.get(0).moveToNext());

        for (Schedule s: currentScheduleList) {
            Log.d("full list ", s.getLessonTime() + s.getLessonType() +
                    s.getSubject() + s.getWeekNumbers());
        }

        for (Schedule s: currentScheduleList) {
            Log.d("Before del repetition", s.getLessonTime() + " " +
            s.getSubGroup() + " " + s.getLessonType() + " " + s.getSubject() + " " +
            s.getWeekNumbers());
        }

        //delete doubles
        /*
        int i;
        while ((i = getFirstRepetition(currentScheduleList)) > -1) {
            currentScheduleList.remove(i);
        }
        */

        for (Schedule s: currentScheduleList) {
            Log.d("After del repetition", s.getLessonTime() + " " +
                    s.getSubGroup() + " " + s.getLessonType() + " " + s.getSubject() + " " +
                    s.getWeekNumbers());
        }

        currentScheduleList = sortScheduleListByTime(currentScheduleList);

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

    public Integer getFirstRepetition(List<Schedule> currentScheduleList) {
        Integer rIndex = -1;
        boolean flag = false;
        int n = 0;
        int m = 0;
        for (Schedule i: currentScheduleList) {
            for (Schedule j: currentScheduleList) {
                if (n != m) {
                    if (i.getLessonTime().equals(j.getLessonTime()) && i.getSubGroup().equals(j.getSubGroup())
                            && i.getNote().equals(j.getNote())) {
                        if (isEqualWeekNums(i.getWeekNumbers().toArray(new String[i.getWeekNumbers().size()]),
                                j.getWeekNumbers().toArray(new String[j.getWeekNumbers().size()]))) {
                            rIndex = m;
                            flag = true;
                            break;
                        }
                    }
                }
                m++;
            }
            if (flag) break;
            m = 0;
            n++;
        }

        if (rIndex >= 0) {
            return rIndex;
        } else {
            return -1;
        }
    }

    private boolean isEqualWeekNums(String[] w1, String[] w2) {
        boolean flag = false;
        if (w1.length != w2.length) {
            return false;
        }

        for (int i = 0; i < w1.length; i++) {
            if (w1[i].equals(w2[i])) {
                flag = true;
            } else {
                return false;
            }
        }

        return flag;
    }

    //проверяет принадлежит ли запись в таблице преподавателю
    private boolean isTeacherId(List<String> scheduleId, String value) {
        for(String str: scheduleId) {
            if (str.equals(value)) return true;
        }
        return false;
    }

    //заполняет расписание на один день для группы
    private SchoolDay fillSchoolDayForGroup(String studentGroupName, Long weekDay) {

        List<SchoolDay> weekSchedule = new ArrayList<>();

        SchoolDay currentSchoolDay = new SchoolDay();
        Schedule currentSchedule = new Schedule();
        List<Schedule> currentScheduleList = new ArrayList<>();

        List<Cursor> scheduleTableCursor = new ArrayList<>();
        String scheduleTableQuery = "select * from schedule where id_student_group = "
                + getGroupId(studentGroupName) + " and " + "week_day = " + weekDay + " order by id_lesson_time";
        scheduleTableCursor = getDbHelper().getData(scheduleTableQuery);

        String lessonTime = null;
        String lessonType = null;
        String subject = null;
        String subGroup = null;
        String groupName = null;
        String[] weekNumbers = null;
        String note = null;
        List<Employee> employees = new ArrayList<>();
        List<String> auds = new ArrayList<>();
        String isHidden;
        String date;

        if (scheduleTableCursor.isEmpty()) {
            Log.d("cursor", " is empty");
        }
        else {
            try {
                scheduleTableCursor.get(0).moveToFirst();
            } catch (NullPointerException ex) {
                return null;
            }
        }
        int rowNum = 0;

        //fill SchoolDay
        do {
            currentSchedule = new Schedule();

            if ((isHidden = scheduleTableCursor.get(0).getString(scheduleTableCursor.get(0)
                    .getColumnIndex(DBColumns.IS_HIDDEN))) != null) {
                if (isHidden.equals("true")) {
                    currentSchedule.setHidden(true);
                }
                else currentSchedule.setHidden(false);
            }
            else currentSchedule.setHidden(false);


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
                if (!subGroup.equals("0")) {
                    currentSchedule.setSubGroup(subGroup);
                }
                else currentSchedule.setSubGroup("");
            }
            else currentSchedule.setSubGroup("");

            if ((groupName = getGroupNamebyId(getGroupIdFromScheduleTable(rowNum, scheduleTableCursor))) != null) {
                currentSchedule.setStudentGroup(groupName);
            } else currentSchedule.setStudentGroup("");

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

            if ((auds = getAudsForScheduleTableRow(rowNum, scheduleTableCursor)) != null) {
                currentSchedule.setAuditories(auds);
            }
            else currentSchedule.setAuditories(Arrays.asList(""));

            if ((note = getNoteForScheduleTableRow(rowNum, scheduleTableCursor)) != null) {
                currentSchedule.setNote(note);
            } else currentSchedule.setNote("");

            currentSchedule.setScheduleTableRowId(scheduleTableCursor.get(0).getString(
                    scheduleTableCursor.get(0).getColumnIndex(BaseColumns._ID)));

            if ((date = scheduleTableCursor.get(0).getString(scheduleTableCursor.get(0)
                    .getColumnIndex(DBColumns.DATE_COLUMN))) != null) {
                currentSchedule.setDate(date);
            } else currentSchedule.setDate("");


            currentScheduleList.add(currentSchedule);
            rowNum++;
        } while (scheduleTableCursor.get(0).moveToNext());

        currentScheduleList = sortScheduleListByTime(currentScheduleList);

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

    //сортирует расписание по времени от меньшего к большему
    public List<Schedule> sortScheduleListByTime(List<Schedule> schedules) {
        for (int i = 0; i < schedules.size(); i++) {
            for (int j = i; j < schedules.size(); j++) {
                if (!isTimeLess(schedules.get(i).getLessonTime(), schedules.get(j).getLessonTime())) {
                    Schedule buf;
                    buf = schedules.get(i);
                    schedules.set(i, schedules.get(j));
                    schedules.set(j, buf);
                }
            }
        }

        return schedules;
    }

    private boolean isTimeLess(String firstTime, String secondTime) {
        if (firstTime.length() <= 0 || secondTime.length() <= 0) {
            return true;
        }
        try {
            try {
                Integer first = Integer.valueOf(firstTime.substring(0, 2));
                Integer second = Integer.valueOf(secondTime.substring(0, 2));
                if (first < second) return true;
                else return false;
            } catch (NumberFormatException ex) {
                Integer first = Integer.valueOf(firstTime.substring(0, 1));
                Integer second = Integer.valueOf(secondTime.substring(0, 1));
                if (first < second) return true;
                else return false;
            }

        } catch (NullPointerException ex) {
            return true;
        }

    }

    //получить id группы по навзанию группы
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

    //получает номера недель из строки в таблицы расписании
    private String[] getWeekNumsFromScheduleTable(Integer rowNum, List<Cursor> c) {
        List<Cursor> cursor = c;
        String[] weekNums = {"1", "2", "3", "4"};

        try {
            cursor.get(0).moveToFirst();
            cursor.get(0).moveToPosition(rowNum);
            weekNums = cursor.get(0).getString(cursor.get(0).getColumnIndex(DBColumns.WEEK_NUMBER_COLUMN)).split("\\, ");
            return weekNums;
        } catch (NullPointerException ex) {
            return  weekNums;
        }
    }

    //получае день недели из строки в таблицы расписании
    private Long getWeekDayFromScheduleTable(Integer rowNum, List<Cursor> c) {
        List<Cursor> cursor = c;

        cursor.get(0).moveToFirst();
        cursor.get(0).moveToPosition(rowNum);
        return cursor.get(0).getLong(cursor.get(0).getColumnIndex(DBColumns.WEEK_DAY_COLUMN));
    }

    //получает подгруппу из строки в таблице расписании
    private String getSubGroupFromScheduleTable(Integer rowNum, List<Cursor> c) {
        List<Cursor> cursor = c;

        cursor.get(0).moveToFirst();
        cursor.get(0).moveToPosition(rowNum);
        return cursor.get(0).getString(cursor.get(0).getColumnIndex(DBColumns.SUBGROUP_COLUMN));
    }

    private String getNoteForScheduleTableRow(Integer rowNum, List<Cursor> c) {
        List<Cursor> cursor = c;

        cursor.get(0).moveToFirst();
        cursor.get(0).moveToPosition(rowNum);

        String rowId = cursor.get(0).getString(cursor.get(0).getColumnIndex(BaseColumns._ID));
        String query = "select " + DBColumns.NOTE_TEXT_COLUMN + " from note"  + " where " +
                DBColumns.NOTE_SCHEDULE_ID_COLUMN + " = " + rowId;

        List<Cursor> noteCursor = getDbHelper().getData(query);

        try {
            Log.d("Date Base", " Row with id " + rowId + " have note = " + noteCursor.get(0).getString(0));
           return noteCursor.get(0).getString(0);
        } catch (NullPointerException ex) {
            Log.d("Data Base", " Row with id " + rowId + " haven't got note." );
            return null;
        }

    }

    //тип занятия
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

    private String getGroupIdFromScheduleTable(Integer rowNum, List<Cursor> c) {
        List<Cursor> cursor = c;

        cursor.get(0).moveToFirst();
        cursor.get(0).moveToPosition(rowNum);
        return cursor.get(0).getString(cursor.get(0).getColumnIndex(DBColumns.STUDENT_GROUP_ID_COLUMN));

    }

    private String getGroupNamebyId(String id) {
        List<Cursor> groupNameCursor;
        String getGroupNameQuery = "select * from student_group where _id = " + id;

        groupNameCursor = getDbHelper().getData(getGroupNameQuery);
        return groupNameCursor.get(0).getString(groupNameCursor.get(0).getColumnIndex(DBColumns.STUDENT_GROUP_NAME_COLUMN));
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
            empCursor.get(0).moveToFirst();
            do {
                employee.add(getEmployeeById(empCursor.get(0).getString(empCursor.get(0).getColumnIndex(
                        DBColumns.SE_EMPLOYEE_ID_COLUMN))));
            } while (empCursor.get(0).moveToNext());
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

    private List<String> getAudsForScheduleTableRow(Integer rowNum, List<Cursor> c) {
        List<String> auds = new ArrayList<>();
        String scheduleId = null;

        c.get(0).moveToFirst();
        c.get(0).moveToPosition(rowNum);

        scheduleId = c.get(0).getString(0);

        List<Cursor> audCursor = new ArrayList<>();
        String query = "select * from schedule_auditory where sa_id_schedule = " + scheduleId;
        audCursor = getDbHelper().getData(query);
        try {
            audCursor.get(0).moveToFirst();
            do {
                auds.add(getAudById(audCursor.get(0).getString(audCursor.get(0).getColumnIndex(
                        DBColumns.SA_AUDITORY_ID_COLUMN))));
            } while(audCursor.get(0).moveToNext());
        } catch (NullPointerException ex) {
            List<String> empty = new ArrayList<>();
            return empty;
        }

        return auds;
    }

    private String getEmployeeId(String lastName) {
        List<Cursor> cursor = new ArrayList<>();
        List<String> names = new ArrayList<>();


        String query = "select last_name from employee";

        // get employee id from table
        // sqlite not match unicode symbols
        cursor = getDbHelper().getData(query);
        cursor.get(0).moveToFirst();
        int id = 0;
        do {
            if (cursor.get(0).getString(0).equals(lastName)) {
                return String.valueOf(id + 1);
            }
            id++;
        } while (cursor.get(0).moveToNext());

       return null;
    }

    //сделать расписание группы доступным для просмотра
    private void setGroupAsAvailable(String fileName) {
        String groupName = fileName.substring(0, 6);

        ContentValues values = new ContentValues();
        values.put(DBColumns.GR_SCHEDULE_AVAILABLE, "true");

        String selection = DBColumns.STUDENT_GROUP_NAME_COLUMN + " = " + groupName;


        int count = getDbHelper().getReadableDatabase().update(
                "student_group",
                values,
                selection,
                null);
    }

    //делает группу недоступной для просмотра
    private void setGroupAsUnavailable(String fileName) {
        String groupName = fileName.substring(0, 6);

        ContentValues values = new ContentValues();
        values.put(DBColumns.GR_SCHEDULE_AVAILABLE, "");

        String selection = DBColumns.STUDENT_GROUP_NAME_COLUMN + " = " + groupName;


        int count = getDbHelper().getReadableDatabase().update(
                "student_group",
                values,
                selection,
                null);
    }

    //сделать расписание преподавателя доступным для просмотра
    private void setTeacherAsAvailable(String fileName) {
        String lastName = EmployeeUtil.getEmployeeLastNameFromFile(fileName);
        int id = 0;

        ContentValues values = new ContentValues();
        values.put(DBColumns.EMP_SCHEDULE_AVAILABLE, "true");

        String selection = BaseColumns._ID + " = " + getEmployeeId(lastName);;


        int count = getDbHelper().getReadableDatabase().update(
                "employee",
                values,
                selection,
                null);
    }

    private void setTeacherAsUnavailable(String fileName) {
        String lastName = EmployeeUtil.getEmployeeLastNameFromFile(fileName);
        int id = 0;

        ContentValues values = new ContentValues();
        values.put(DBColumns.EMP_SCHEDULE_AVAILABLE, "");

        String selection = BaseColumns._ID + " = " + getEmployeeId(lastName);;


        int count = getDbHelper().getReadableDatabase().update(
                "employee",
                values,
                selection,
                null);
    }

    public void hideScheduleRow(String rowId) {

        ContentValues values = new ContentValues();
        values.put(DBColumns.IS_HIDDEN, "true");

        String selection = BaseColumns._ID + " = " + rowId;


        int count = getDbHelper().getReadableDatabase().update(
                "schedule",
                values,
                selection,
                null);
    }

    public void setAsManual(String rowId) {
        ContentValues values = new ContentValues();
        values.put(DBColumns.IS_MANUAL, "true");

        String selection = BaseColumns._ID + " = " + rowId;


        int count = getDbHelper().getReadableDatabase().update(
                "schedule",
                values,
                selection,
                null);
    }

    public void showScheduleRow(String rowId) {
        ContentValues values = new ContentValues();
        values.put(DBColumns.IS_HIDDEN, "false");

        String selection = BaseColumns._ID + " = " + rowId;


        int count = getDbHelper().getReadableDatabase().update(
                "schedule",
                values,
                selection,
                null);
    }

    //доступные расписания групп
    public List<String> getAvailableGroups() {
        List<Cursor> cursors = new ArrayList<>();
        List<String> groups = new ArrayList<>();

        String query = "select * from student_group";

        try {
            cursors = getDbHelper().getData(query);
            cursors.get(0).moveToFirst();


            do {
                // 2 = GR_SCHEDULE_AVAILABLE column
                if (cursors.get(0).getString(2) != null){
                    if (cursors.get(0).getString(2).equals("true")) {
                        //1 = GROUP_NAME column
                        groups.add(cursors.get(0).getString(1));
                        Log.d("available group = ", groups.get(0));
                    }
                }

            } while (cursors.get(0).moveToNext());


            return groups;
        } catch (NullPointerException ex) {
            Log.d("DB ", "No one group have been found!");
            return groups;
        }
    }

    //доступные расписания преподавателей
    public List<String> getAvailableTeachers() {
        List<Cursor> cursors = new ArrayList<>();
        List<String> teachers = new ArrayList<>();

        String query = "select * from employee";

        try {
            cursors = getDbHelper().getData(query);
            cursors.get(0).moveToFirst();


            do {
                //5 = EMP_SCHEDULE_AVAILABLE column
                if (cursors.get(0).getString(5) != null) {
                    if (cursors.get(0).getString(5).equals("true")) {
                        //2, 1, 3 = LAST_NAME, FIRST_NAME, MIDDLE_NAME columns
                        teachers.add(cursors.get(0).getString(2) +
                                cursors.get(0).getString(1).substring(0, 1) +
                                cursors.get(0).getString(3).substring(0, 1) );
                        Log.d("available teacher = ", teachers.get(0));
                    }
                }

            } while (cursors.get(0).moveToNext());


            return teachers;
        } catch (NullPointerException ex) {
            Log.d("DB ", "No one teacher have been found!");
            return teachers;
        }
    }

    //сделать расписание доступным
    public void setAsAvailable(String fileName) {
        if (FileUtil.isDigit(fileName.charAt(0))) {
            setGroupAsAvailable(fileName);
        } else {
            setTeacherAsAvailable(fileName);
        }
    }

    public void setAsUnavailable(String fileName) {
        if (FileUtil.isDigit(fileName.charAt(0))) {
            setGroupAsUnavailable(fileName);
        } else {
            setTeacherAsUnavailable(fileName);
        }
    }


    public DBHelper getDbHelper() {
        return dbHelper;
    }

    public void setDbHelper(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }
}
