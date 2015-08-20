package com.example.myapplication.Utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by iChrome on 20.08.2015.
 */
public class DateUtil {

    private DateUtil(){
    }

    public static String getCurrentDateAsString(){
        DateFormat df = DateFormat.getDateInstance();
        Date today = Calendar.getInstance().getTime();

        return df.format(today);
    }
}
