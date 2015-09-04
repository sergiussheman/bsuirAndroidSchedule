package com.example.myapplication.Widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.myapplication.DataProvider.XmlDataProvider;
import com.example.myapplication.Model.Schedule;
import com.example.myapplication.Model.SchoolDay;
import com.example.myapplication.R;
import com.example.myapplication.Utils.FileUtil;

import java.util.Calendar;
import java.util.List;

/**
 * Created by iChrome on 04.09.2015.
 */
public class ScheduleWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory{
    private Context savedContext;
    private int appWidgetId;
    private List<Schedule> items;

    public ListRemoteViewsFactory(Context context, Intent intent){
        savedContext = context;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate(){
        String defaultSchedule = FileUtil.getDefaultSchedule(savedContext);
        List<SchoolDay> weekSchedules = XmlDataProvider.parseScheduleXml(savedContext.getFilesDir(), defaultSchedule);
        int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        if(currentDay == Calendar.SUNDAY){
            currentDay = 8;
        }
        items = weekSchedules.get(currentDay - 2).getSchedules();
    }

    @Override
    public void onDestroy(){
        items.clear();
    }

    @Override
    public int getCount(){
        return items.size();
    }

    @Override
    public RemoteViews getViewAt(int position){
        Schedule currentSchedule = items.get(position);
        RemoteViews result = new RemoteViews(savedContext.getPackageName(), R.layout.schedule_widget_item_layout);
        String lessonTime = currentSchedule.getLessonTime();
        String[] times = lessonTime.split("-");
        result.setTextViewText(R.id.scheduleWidgetStartTime, times[0]);
        result.setTextViewText(R.id.scheduleWidgetEndTime, times[1]);

        result.setTextViewText(R.id.scheduleWidgetSubjectName, currentSchedule.getSubject());
        result.setTextViewText(R.id.scheduleWidgetAuditory, convertListString(currentSchedule.getAuditories(), ""));
        return result;
    }

    private String convertListString(List<String> values, String addition){
        StringBuilder result = new StringBuilder();
        for(String value : values){
            result.append(value);
            result.append(", ");
        }
        if(result.length() > 2) {
            result.delete(result.length() - 2, result.length());
            result.append(addition);
        }
        return result.toString();
    }

    @Override
    public RemoteViews getLoadingView(){
        return null;
    }

    @Override
    public int getViewTypeCount(){
        return 1;
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public boolean hasStableIds(){
        return true;
    }

    @Override
    public void onDataSetChanged(){

    }
}
