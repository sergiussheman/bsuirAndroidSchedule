package com.example.myapplication.Widget;

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
    public static final String DEFAULT_SUBGROUP = "defaultSubgroup";
    private String[] weekDays = new String[]{"Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье"};
    private List<SchoolDay> weekSchedules;
    private Integer dayOffset;
    private Context savedContext;
    private List<Schedule> items;
    private Integer defaultSubGroup;
    private boolean isLastUsingDailySchedule;

    public ListRemoteViewsFactory(Context context, Intent intent){
        savedContext = context;
        dayOffset = intent.getIntExtra(OFFSET_EXTRA_NAME, 0);
        defaultSubGroup = intent.getIntExtra(DEFAULT_SUBGROUP, 0);
    }

    @Override
    public void onCreate(){
        updateScheduleList();
    }

    public void updateParameters(Context context, Intent intent){
        savedContext = context;
        dayOffset = intent.getIntExtra(OFFSET_EXTRA_NAME, 0);
        defaultSubGroup = intent.getIntExtra(DEFAULT_SUBGROUP, 0);
    }

    public void updateScheduleList(){
        String defaultSchedule = FileUtil.getDefaultSchedule(savedContext);
        if(defaultSchedule != null) {
            isLastUsingDailySchedule = FileUtil.isLastUsingDailySchedule(savedContext);
            if (!isLastUsingDailySchedule) {
                defaultSchedule = defaultSchedule.replace(".xml", "exam.xml");
            }
            weekSchedules = XmlDataProvider.parseScheduleXml(savedContext.getFilesDir(), defaultSchedule);
            updateCurrentDaySchedules();
        }
    }

    private void updateCurrentDaySchedules(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, dayOffset);
        List<Schedule> schedules;
        if(isLastUsingDailySchedule) {
            int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
            if (currentDay == Calendar.SUNDAY) {
                currentDay = 8;
            }
            schedules = getSchedulesByDayOfWeek(currentDay - 2, weekSchedules);
        } else{
            schedules = getSchedulesByDate(weekSchedules, dayOffset);
        }
        Integer currentWeekNumber = DateUtil.getWeek(calendar.getTime());
        boolean scheduleForGroup = FileUtil.isDefaultStudentGroup(savedContext);
        items = new ArrayList<>();
        if (currentWeekNumber != null) {
            String weekNumberAsString = currentWeekNumber.toString();
            for (Schedule schedule : schedules) {
                boolean matchSubgroupNumber = false;
                boolean matchWeekNumber = false;
                if(isLastUsingDailySchedule) {
                    for (String weekNumber : schedule.getWeekNumbers()) {
                        if (weekNumberAsString.equalsIgnoreCase(weekNumber)) {
                            matchWeekNumber = true;
                        }
                    }
                } else{
                    matchWeekNumber = true;
                }

                if (scheduleForGroup) {
                    if (defaultSubGroup == 0) {
                        matchSubgroupNumber = true;
                    } else if (schedule.getSubGroup().isEmpty()) {
                        matchSubgroupNumber = true;
                    } else {
                        if (defaultSubGroup.toString().equalsIgnoreCase(schedule.getSubGroup())) {
                            matchSubgroupNumber = true;
                        }
                    }
                } else {
                    matchSubgroupNumber = true;
                }

                if (matchSubgroupNumber && matchWeekNumber) {
                    items.add(schedule);
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

    private List<Schedule> getSchedulesByDate(List<SchoolDay> dateSchedules, Integer dayOffset){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, dayOffset);
        String currentDateAsString = calendar.get(Calendar.DATE) + ".";
        //add 1 because month is zero-based
        Integer monthOrder = calendar.get(Calendar.MONTH) + 1;
        if(monthOrder < 10){
            currentDateAsString += "0" + monthOrder + ".";
        } else {
            currentDateAsString += monthOrder + ".";
        }
        currentDateAsString += calendar.get(Calendar.YEAR);
        for(SchoolDay schoolDay : dateSchedules){
            if(schoolDay.getDayName().equalsIgnoreCase(currentDateAsString)){
                return schoolDay.getSchedules();
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
        updateCurrentDaySchedules();
        Schedule currentSchedule;
        RemoteViews result = new RemoteViews(savedContext.getPackageName(), R.layout.schedule_widget_item_layout);
        if(items.size() > position) {
            currentSchedule = items.get(position);

            String lessonTime = currentSchedule.getLessonTime();
            String[] times = lessonTime.split("-");
            if (times.length == 2) {
                result.setTextViewText(R.id.scheduleWidgetStartTime, times[0]);
                result.setTextViewText(R.id.scheduleWidgetEndTime, times[1]);
            }
            updateLessonTypeViewBackground(currentSchedule, result);

            String subject = currentSchedule.getSubject();
            if(!currentSchedule.getNote().isEmpty()){
                subject += " " + currentSchedule.getNote();
            }
            result.setTextViewText(R.id.scheduleWidgetSubjectName, subject);
            result.setTextViewText(R.id.scheduleWidgetAuditory, convertListString(currentSchedule.getAuditories(), ""));
        }
        return result;
    }

    private void updateLessonTypeViewBackground(Schedule currentSchedule, RemoteViews remoteViews){
        switch(currentSchedule.getLessonType()){
            case "ПЗ":
                remoteViews.setInt(R.id.lessonTypeViewInWidget, "setBackgroundResource", R.color.yellow);
                break;
            case "УПз":
                remoteViews.setInt(R.id.lessonTypeViewInWidget, "setBackgroundResource", R.color.yellow);
                break;
            case "ЛК":
                remoteViews.setInt(R.id.lessonTypeViewInWidget, "setBackgroundResource", R.color.green);
                break;
            case "УЛк":
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
        updateScheduleList();
    }
}
