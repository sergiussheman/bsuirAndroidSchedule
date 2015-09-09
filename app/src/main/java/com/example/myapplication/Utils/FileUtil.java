package com.example.myapplication.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by iChrome on 20.08.2015.
 */
public class FileUtil {
    private static final String DEFAULT_GROUP = "defaultGroup";
    private static final String DEFAULT_EMPLOYEE = "defaultEmployee";

    private FileUtil(){
    }

    public static List<String> getAllDownloadedSchedules(Context context, boolean schedulesForGroups){
        List<String> result = new ArrayList<>();
        for (File f : context.getFilesDir().listFiles()) {
            if(f.isFile()){
                String fileName = f.getName();
                if(".xml".equalsIgnoreCase(fileName.substring(fileName.length() - 4))){
                    if(!fileName.contains("exam")) {
                        if (schedulesForGroups && isDigit(fileName.charAt(0))) {
                            result.add(fileName);
                        } else if (!schedulesForGroups && !isDigit(fileName.charAt(0))) {
                            result.add(fileName);
                        }
                    }
                }
            }
        }
        return result;
    }

    private static boolean isDigit(char symbol){
        char[] digits = new char[] {'1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};
        for(char tempSymbol : digits){
            if(tempSymbol == symbol){
                return true;
            }
        }
        return false;
    }

    public static String getDefaultSchedule(Context context){
        String settingFileName = context.getString(R.string.setting_file_name);
        SharedPreferences preferences = context.getSharedPreferences(settingFileName, 0);
        String defaultGroup = preferences.getString(DEFAULT_GROUP, "none");
        if(!defaultGroup.equalsIgnoreCase("none")){
            return defaultGroup + ".xml";
        } else{
            String defaultEmployee = preferences.getString(DEFAULT_EMPLOYEE, "none");
            if(!defaultEmployee.equalsIgnoreCase("none")){
                return defaultEmployee + ".xml";
            }
        }
        return null;
    }

    public static Integer getDefaultSubgroup(Context context){
        String settingFileName = context.getString(R.string.setting_file_name);
        final SharedPreferences preferences = context.getSharedPreferences(settingFileName, 0);
        Integer defaultSubgroup = preferences.getInt(context.getString(R.string.default_subgroup), 0);
        if(defaultSubgroup != 0){
            return defaultSubgroup;
        } else {
            return null;
        }
    }


    public static boolean isDefaultStudentGroup(Context context){
        String settingFileName = context.getString(R.string.setting_file_name);
        SharedPreferences preferences = context.getSharedPreferences(settingFileName, 0);
        String defaultGroup = preferences.getString(context.getResources().getString(R.string.default_group_field_in_settings), "none");
        if("none".equals(defaultGroup)){
            return false;
        } else{
            return true;
        }
    }

    public static Boolean isLastUsingDailySchedule(Context context){
        String settingFileName = context.getString(R.string.setting_file_name);
        final SharedPreferences preferences = context.getSharedPreferences(settingFileName, 0);
        String lastUsingSchedule = preferences.getString(MainActivity.LAST_USING_SCHEDULE, "none");
        switch (lastUsingSchedule){
            case MainActivity.LAST_USING_DAILY_SCHEDULE_TAG:
                return true;
            case MainActivity.LAST_USING_EXAM_SCHEDULE_TAG:
                return false;
            default:
                return null;
        }
    }
}
