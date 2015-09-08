package com.example.myapplication.Widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.myapplication.DataProvider.XmlDataProvider;
import com.example.myapplication.Model.Schedule;
import com.example.myapplication.Model.SchoolDay;
import com.example.myapplication.R;
import com.example.myapplication.Utils.DateUtil;
import com.example.myapplication.Utils.FileUtil;

import java.util.ArrayList;
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
    public static final String OFFSET_EXTRA_NAME = "scheduleWidgetOffset";
    private String[] weekDays = new String[]{"Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье"};
    private Integer dayOffset;
    private Context savedContext;
    private int appWidgetId;
    private List<Schedule> items;

    public ListRemoteViewsFactory(Context context, Intent intent){
        savedContext = context;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        dayOffset = intent.getIntExtra(OFFSET_EXTRA_NAME, 0);
    }

    @Override
    public void onCreate(){
        String defaultSchedule = FileUtil.getDefaultSchedule(savedContext);
        List<SchoolDay> weekSchedules = XmlDataProvider.parseScheduleXml(savedContext.getFilesDir(), defaultSchedule);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, dayOffset);

        int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
        if(currentDay == Calendar.SUNDAY){
            currentDay = 8;
        }
        List<Schedule> schedules = getSchedulesByDayOfWeek(currentDay - 2, weekSchedules);
        Integer currentWeekNumber = DateUtil.getWeek(calendar.getTime());
        items = new ArrayList<>();
        if(currentWeekNumber != null) {
            String weekNumberAsString = currentWeekNumber.toString();
            for (Schedule schedule : schedules) {
                for (String weekNumber : schedule.getWeekNumbers()) {
                    if (weekNumberAsString.equalsIgnoreCase(weekNumber)){
                        items.add(schedule);
                    }
                }
            }
        }
    }

    private List<Schedule> getSchedulesByDayOfWeek(Integer dayOfWeek, List<SchoolDay> weekSchedules){
        if(dayOfWeek >= 0 && dayOfWeek < weekDays.length) {
            String dayAsString = weekDays[dayOfWeek];
            for (SchoolDay schoolDay : weekSchedules) {
                if (schoolDay.getDayName().equalsIgnoreCase(dayAsString)) {
                    return schoolDay.getSchedules();
                }
            }
        }
        return new ArrayList<>();
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
        updateLessonTypeViewBackground(currentSchedule, result);

        result.setTextViewText(R.id.scheduleWidgetSubjectName, currentSchedule.getSubject());
        result.setTextViewText(R.id.scheduleWidgetAuditory, convertListString(currentSchedule.getAuditories(), ""));
        return result;
    }

    private void updateLessonTypeViewBackground(Schedule currentSchedule, RemoteViews remoteViews){
        switch(currentSchedule.getLessonType()){
            case "ПЗ":
                remoteViews.setInt(R.id.lessonTypeViewInWidget, "setBackgroundResource", R.color.yellow);
                break;
            case "ЛК":
                remoteViews.setInt(R.id.lessonTypeViewInWidget, "setBackgroundResource", R.color.green);
                break;
            case "ЛР":
                remoteViews.setInt(R.id.lessonTypeViewInWidget, "setBackgroundResource", R.color.red);
                break;
            default:
                remoteViews.setInt(R.id.lessonTypeViewInWidget, "setBackgroundResource", R.color.blue);
                break;
        }
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
