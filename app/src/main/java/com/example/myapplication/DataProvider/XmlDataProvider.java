package com.example.myapplication.DataProvider;

import android.util.Log;

import com.example.myapplication.Model.Employee;
import com.example.myapplication.Model.Schedule;
import com.example.myapplication.Model.SchoolDay;
import com.example.myapplication.Model.StudentGroup;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by iChrome on 05.08.2015.
 */
public class XmlDataProvider {
    private static final String TAG = "xmlLog";
    private static final String EMPLOYEE_TAG = "employee";

    public static List<Employee> parseListEmployeeXml(String content){
        try {
            List<Employee> resultList = new ArrayList<>();
            Employee currentReadingEmployee = null;
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(content));

            int eventType = parser.getEventType();
            String currentTag = null;

            while(eventType != XmlPullParser.END_DOCUMENT){
                switch (eventType){
                    case XmlPullParser.START_DOCUMENT:
                        Log.v(TAG, "Start document");
                        break;

                    case XmlPullParser.START_TAG:
                        currentTag = parser.getName();
                        if(currentTag.equals(EMPLOYEE_TAG) && parser.getDepth() == 2){
                            if(currentReadingEmployee != null){
                                resultList.add(currentReadingEmployee);
                            }
                            currentReadingEmployee = new Employee();
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        currentTag = parser.getName();
                        break;

                    case XmlPullParser.TEXT:
                        assert currentTag != null;
                        if(currentTag.equalsIgnoreCase("academicDepartment")){
                            assert currentReadingEmployee != null;
                            currentReadingEmployee.setDepartment(parser.getText());
                        } else if(currentTag.equalsIgnoreCase("firstName")){
                            assert currentReadingEmployee != null;
                            currentReadingEmployee.setFirstName(parser.getText());
                        } else if(currentTag.equalsIgnoreCase("id")){
                            assert currentReadingEmployee != null;
                            currentReadingEmployee.setId(Long.parseLong(parser.getText()));
                        } else if(currentTag.equalsIgnoreCase("lastName")){
                            assert currentReadingEmployee != null;
                            currentReadingEmployee.setLastName(parser.getText());
                        } else if(currentTag.equalsIgnoreCase("middleName")){
                            assert currentReadingEmployee != null;
                            currentReadingEmployee.setMiddleName(parser.getText());
                        }
                        break;
                    default:
                        break;
                }
                eventType = parser.next();
            }
            return resultList;
        } catch(XmlPullParserException e){
            Log.v(TAG, "Ошибка при парсинге xml");
        } catch (IOException e){
            Log.v(TAG, "IO exception");
        }
        return new ArrayList<>();
    }

    public static List<StudentGroup> parseListStudentGroupXml(String content){
        try {
            List<StudentGroup> resultList = new ArrayList<>();
            StudentGroup currentReadingStudentGroup = null;
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(content));

            int eventType = parser.getEventType();
            String currentTag = null;

            while(eventType != XmlPullParser.END_DOCUMENT){
                switch (eventType){
                    case XmlPullParser.START_DOCUMENT:
                        Log.v(TAG, "Start document");
                        break;

                    case XmlPullParser.START_TAG:
                        currentTag = parser.getName();
                        if(currentTag.equals("studentGroup") && parser.getDepth() == 2){
                            if(currentReadingStudentGroup != null){
                                resultList.add(currentReadingStudentGroup);
                            }
                            currentReadingStudentGroup = new StudentGroup();
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        currentTag = parser.getName();
                        break;

                    case XmlPullParser.TEXT:
                        assert currentTag != null;
                        if(currentTag.equalsIgnoreCase("name")){
                            assert currentReadingStudentGroup != null;
                            currentReadingStudentGroup.setStudentGroupName(parser.getText());
                        } else if(currentTag.equalsIgnoreCase("id")) {
                            assert currentReadingStudentGroup != null;
                            currentReadingStudentGroup.setStudentGroupId(Long.parseLong(parser.getText()));
                        }
                        break;
                    default:
                        break;
                }
                eventType = parser.next();
            }
            return resultList;
        } catch(XmlPullParserException e){
            Log.v(TAG, "Ошибка при парсинге xml");
        } catch (IOException e){
            Log.v(TAG, "IO exception");
        }
        return new ArrayList<>();
    }


    public static List<SchoolDay> parseScheduleXml(File directory, String fileName){
        List<SchoolDay> weekSchedule = new ArrayList<>();
        try {
            File file = new File(directory, fileName);
            FileReader reader = new FileReader(file);

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(reader);

            int eventType = parser.getEventType();
            String currentTag = null;
            SchoolDay currentSchoolDay = null;
            Schedule currentSchedule = null;
            Employee currentEmployee = new Employee();
            List<Schedule> currentScheduleList = new ArrayList<>();

            while(eventType != XmlPullParser.END_DOCUMENT){
                switch (eventType){
                    case XmlPullParser.START_DOCUMENT:
                        Log.v(TAG, "start xml document");
                        break;
                    case XmlPullParser.START_TAG:
                        currentTag = parser.getName();
                        if((currentTag.equals("schedule")) && (parser.getDepth() == 3))
                        {
                            if(currentSchedule != null) {
                                currentScheduleList.add(currentSchedule);
                            }
                            currentSchedule = new Schedule();
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        currentTag = parser.getName();
                        break;
                    case XmlPullParser.TEXT:
                        if(currentTag.equals("auditory")){
                            assert currentSchedule != null;
                            currentSchedule.getAuditories().add(parser.getText());
                        } else if(currentTag.equals("firstName")){
                            currentEmployee.setFirstName(parser.getText());
                        } else if(currentTag.equals("lastName")){
                            currentEmployee.setLastName(parser.getText());
                        } else if(currentTag.equals("middleName")){
                            currentEmployee.setMiddleName(parser.getText());
                            currentSchedule.getEmployeeList().add(currentEmployee);
                            currentEmployee = new Employee();
                        } else if("studentGroup".equalsIgnoreCase(currentTag)){
                            currentSchedule.setStudentGroup(parser.getText());
                        } else if(currentTag.equals("lessonTime")){
                            currentSchedule.setLessonTime(parser.getText());
                        } else if(currentTag.equals("lessonType")){
                            currentSchedule.setLessonType(parser.getText());
                        } else if(currentTag.equals("note")){
                            currentSchedule.setNote(parser.getText());
                        } else if(currentTag.equals("numSubgroup")){
                            if(!parser.getText().equals("0"))
                                currentSchedule.setSubGroup(parser.getText());
                            else currentSchedule.setSubGroup("");;
                        } else if(currentTag.equals("subject")){
                            currentSchedule.setSubject(parser.getText());
                        } else if(currentTag.equals("weekNumber")){
                            String weekNum = parser.getText();
                            if(!weekNum.equals("0"))
                                currentSchedule.getWeekNumbers().add(weekNum);
                        } else if(currentTag.equals("weekDay")){
                            if(currentSchedule != null) {
                                currentScheduleList.add(currentSchedule);
                            }
                            currentSchoolDay = new SchoolDay();
                            currentSchoolDay.setDayName(parser.getText());
                            currentSchoolDay.setSchedules(currentScheduleList);
                            weekSchedule.add(currentSchoolDay);

                            currentSchedule = null;
                            currentScheduleList = new ArrayList<>();
                        }
                        break;
                    default:
                        break;
                }
                eventType = parser.next();
            }
        } catch (FileNotFoundException e){
            Log.v(TAG, "Файл не найден: " + fileName);
        } catch(XmlPullParserException e){
            Log.v(TAG, "Ошибка при парсинге xml");
        } catch (IOException e){
            Log.v(TAG, "Ошибка при считывании данных");
        }
        return weekSchedule;
    }
}
