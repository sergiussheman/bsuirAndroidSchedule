package com.example.myapplication.DataProvider;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by iChrome on 20.08.2015.
 */
public class FileUtil {
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
}
