package com.example.myapplication.Widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import com.example.myapplication.R;
import com.example.myapplication.Utils.DateUtil;
import com.example.myapplication.Utils.FileUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of App Widget functionality.
 */
public class ScheduleWidgetProvider extends AppWidgetProvider {
    private static final String NEXT_DAY = "nextDay";
    private static final String PREVIOUS_DAY = "previousDay";
    private static final String GO_TO_TODAY = "goToToday";
    private static final String PATTERN = "^[а-яА-ЯёЁ]+";
    private static Integer offset = 0;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.listViewWidget);
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
        if(NEXT_DAY.equals(intent.getAction())){
            offset++;
            updateWidget(context);
        } else if (PREVIOUS_DAY.equals(intent.getAction())) {
            offset--;
            updateWidget(context);
        } else if(GO_TO_TODAY.equals(intent.getAction())){
            offset = 0;
            updateWidget(context);
        }
    }

    public void updateWidget(Context context){
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisAppWidget = new ComponentName(context.getPackageName(), ScheduleWidgetProvider.class.getName());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
        onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        Intent intent = new Intent(context, ScheduleWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.putExtra(ListRemoteViewsFactory.OFFSET_EXTRA_NAME, offset);
        intent.putExtra(ListRemoteViewsFactory.DEFAULT_SUBGROUP, FileUtil.getDefaultSubgroup(context));

        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        // Construct the RemoteViews object
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.schedule_widget);
        rv.setRemoteAdapter(appWidgetId, R.id.listViewWidget, intent);
        rv.setEmptyView(R.id.listViewWidget, R.id.empty_view_widget);
        rv.setTextViewText(R.id.scheduleWidgetTitle, getFirstTitle(context));
        rv.setTextViewText(R.id.secondWidgetTitle, getSecondTitle());
        rv.setTextViewText(R.id.secondWidgetSubTitle, getSecondSubTitle(context));

        rv.setOnClickPendingIntent(R.id.previousWidgetButton, getPendingSelfIntent(context, PREVIOUS_DAY));
        rv.setOnClickPendingIntent(R.id.nextWidgetButton, getPendingSelfIntent(context, NEXT_DAY));
        rv.setOnClickPendingIntent(R.id.todayWidgetButton, getPendingSelfIntent(context, GO_TO_TODAY));
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, rv);

    }

    private static String getFirstTitle(Context context){
        String result = "";
        String defaultSchedule = FileUtil.getDefaultSchedule(context);
        if(defaultSchedule != null) {
            if (FileUtil.isDefaultStudentGroup(context)) {
                result = defaultSchedule.substring(0, 6);
                Integer defaultSubGroup = FileUtil.getDefaultSubgroup(context);
                if(defaultSubGroup != null) {
                    result += " - " + defaultSubGroup + " подгр.";
                }
            } else {
                Pattern pattern = Pattern.compile(PATTERN);
                Matcher matcher = pattern.matcher(defaultSchedule);
                if (matcher.find()) {
                    result = matcher.group(0);
                    String initials = result.substring(result.length() - 2, result.length());
                    if(initials.length() == 2) {
                        result = result.substring(0, result.length() - 2) + " " + initials.charAt(0) + ". " + initials.charAt(1) + ".";
                    }
                } else {
                    result = "not found";
                }
            }
        }
        return result;
    }

    private static String getSecondTitle(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, offset);
        DateFormat df = new SimpleDateFormat("dd/MM");
        String result = df.format(calendar.getTime());


        Integer currentWeekNumber = DateUtil.getWeek(calendar.getTime());
        if(currentWeekNumber != null){
            result += " (" + currentWeekNumber + " нед)";
        }
        return result;
    }

    private static String getSecondSubTitle(Context context){
        String result = "";
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, offset);
        int indexDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if(indexDayOfWeek == 1){
            indexDayOfWeek = 8;
        }
        String[] dayOfWeekAbbrev = context.getResources().getStringArray(R.array.day_of_week);
        result += dayOfWeekAbbrev[indexDayOfWeek - 1];
        return  result;
    }

    private PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }
}

