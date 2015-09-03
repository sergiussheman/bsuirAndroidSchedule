package com.example.myapplication.DataProvider;

import android.util.Log;

import com.example.myapplication.Model.Employee;
import com.example.myapplication.Model.StudentGroup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LoadSchedule {
    private static final String STUDENT_GROUP_SCHEDULE_BY_ID_REST = "http://www.bsuir.by/schedule/rest/schedule/android/";
    private static final String EXAM_SCHEDULE = "http://www.bsuir.by/schedule/rest/examSchedule/android/";
    private static final String ACTUAL_APPLICATION_VERSION_URL = "http://www.bsuir.by/schedule/rest/android/actualAndroidVersion";
    private static final String EMPLOYEE_LIST_REST = "http://www.bsuir.by/schedule/rest/employee";
    private static final String SCHEDULE_EMPLOYEE_REST = "http://www.bsuir.by/schedule/rest/employee/android/";
    private static final String STUDENT_GROUP_REST = "http://www.bsuir.by/schedule/rest/studentGroup/";
    private static final String TAG = "Load";

    public static String loadScheduleForStudentGroupById(StudentGroup sg, File fileDir){
        try{
            URL url = new URL(STUDENT_GROUP_SCHEDULE_BY_ID_REST + sg.getStudentGroupId().toString());
            loadSchedule(url, fileDir, sg.getStudentGroupName() + sg.getStudentGroupId());

            url = new URL(EXAM_SCHEDULE + sg.getStudentGroupId().toString());
            loadSchedule(url, fileDir, sg.getStudentGroupName() + sg.getStudentGroupId() + "exam");

            return null;
        } catch (SocketTimeoutException e) {
            return "Ошибка подключения. Сервер не отвечает.";
        } catch (IOException e) {
            Log.v("logs", e.toString());
            return "Группа " + sg.getStudentGroupName() + " не найдена. Проверьте соединение с интернетом." + e.toString();
        }
    }

    public static String loadScheduleForEmployee(String employeeName, File filesDir){
        try {
            //employeeName contains last name and id
            //get all digits from passed employeeName
            //it will be employeeId. Construct URL with this id
            String employeeId = employeeName.replaceAll("\\D+","");
            URL url = new URL(SCHEDULE_EMPLOYEE_REST + employeeId);
            loadSchedule(url, filesDir, employeeName);
            return null;
        } catch (SocketTimeoutException e) {
            return "Ошибка подключения. Сервер не отвечает.";
        } catch (IOException e) {
            Log.v("logs", e.toString());
            return "Расписание для " + employeeName + " не найдено." + e.toString();
        }
    }

    private static void loadSchedule(URL url, File fileDir, String fileName) throws IOException{
        HttpURLConnection urlConnection = (HttpURLConnection) url
                .openConnection();
        urlConnection.setConnectTimeout(5000);
        urlConnection.connect();

        InputStream inputStream = urlConnection.getInputStream();
        File file = new File(fileDir, fileName + ".xml");
        FileOutputStream fileOutput = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int bufferLength;
        while ((bufferLength = inputStream.read(buffer)) > 0) {
            fileOutput.write(buffer, 0, bufferLength);
        }
        inputStream.close();
        fileOutput.close();
        urlConnection.disconnect();
        Log.v(TAG, "Расписание успешно загружено!");
    }

    public static List<StudentGroup> loadAvailableStudentGroups(){
        BufferedReader reader = null;
        try{
            URL url = new URL(STUDENT_GROUP_REST);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.connect();

            StringBuilder result = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null){
                result.append(line);
            }
            return XmlDataProvider.parseListStudentGroupXml(result.toString());
        } catch (Exception e){
            Log.v(TAG, e.toString());
        } finally {
            try{
                if(reader != null){
                    reader.close();
                }
            } catch (IOException e){
                Log.v(TAG, e.toString());
            }
        }
        return new ArrayList<>();
    }

    public static List<Employee> loadListEmployee() {
        BufferedReader reader = null;
        try {
            URL url = new URL(EMPLOYEE_LIST_REST);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.connect();

            StringBuilder result = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            return XmlDataProvider.parseListEmployeeXml(result.toString());
        } catch (Exception e){
            Log.v(TAG, e.toString());
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }

            } catch (IOException e){
                Log.v(TAG, e.toString());
            }
        }
        return new ArrayList<>();
    }

    public static String loadActualApplicationVersion(){
        BufferedReader reader = null;
        try{
            URL url = new URL(ACTUAL_APPLICATION_VERSION_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.connect();

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = reader.readLine();
            if (line != null && !line.isEmpty()){
                return line;
            }
        } catch (Exception e){
            Log.v(TAG, e.toString());
        } finally {
            try {
                if(reader != null){
                    reader.close();
                }
            } catch (IOException e){
                Log.v(TAG, e.toString());
            }
        }
        return null;
    }
}
