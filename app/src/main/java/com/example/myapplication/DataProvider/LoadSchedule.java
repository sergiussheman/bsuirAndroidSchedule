package com.example.myapplication.DataProvider;

import android.util.Log;

import com.example.myapplication.Model.Employee;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.List;

public class LoadSchedule {
    private static final String bsuir = "http://bsuir.by/schedule/rest/schedule/";
    private static final String EMPLOYEE_LIST_REST = "http://www.bsuir.by/schedule/rest/employee";
    private static final String SCHEDULE_EMPLOYEE_REST = "http://www.bsuir.by/schedule/rest/employee/";
    private static final String TAG = "Load";

    public static String loadScheduleForStudentGroup(String group, File filesDir) {
        try {
            URL url = new URL(bsuir + group);
            loadSchedule(url, filesDir, group);
            return null;
        } catch (SocketTimeoutException e) {
            return "Ошибка подключения. Сервер не отвечает.";
        } catch (IOException e) {
            Log.v("logs", e.toString());
            return "Группа " + group + " не найдена. Проверьте соединение с интернетом." + e.toString();
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
        int bufferLength = 0;
        while ((bufferLength = inputStream.read(buffer)) > 0) {
            fileOutput.write(buffer, 0, bufferLength);
        }
        inputStream.close();
        fileOutput.close();
        urlConnection.disconnect();
        Log.v(TAG, "Расписание успешно загружено!");
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
        return null;
    }
}
