package com.example.myapplication.DataProvider;

import android.content.Context;
import android.content.SharedPreferences;

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
                    if(schedulesForGroups && fileName.length() == 10){
                        result.add(fileName);
                    } else if(!schedulesForGroups && fileName.length() > 10){
                        result.add(fileName);
                    }
                }
            }
        }
        return result;
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
}
