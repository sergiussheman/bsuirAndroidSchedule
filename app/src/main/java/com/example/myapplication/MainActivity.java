package com.example.myapplication;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.internal.view.menu.ActionMenuItemView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.myapplication.dataprovider.LoadSchedule;
import com.example.myapplication.dataprovider.XmlDataProvider;
import com.example.myapplication.model.AvailableFragments;
import com.example.myapplication.model.SchoolDay;
import com.example.myapplication.model.SubGroupEnum;
import com.example.myapplication.model.WeekNumberEnum;
import com.example.myapplication.utils.DateUtil;
import com.example.myapplication.utils.FileUtil;
import com.example.myapplication.widget.ScheduleWidgetProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Базовая активити
 */
public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, OnFragmentInteractionListener, ActionBar.OnNavigationListener {
    public static final String LAST_USING_SCHEDULE = "lastUsingSchedule";
    public static final String LAST_USING_DAILY_SCHEDULE_TAG = "usingDailyScheduleTag";
    public static final String LAST_USING_EXAM_SCHEDULE_TAG = "usingExamScheduleTag";
    private static final String MESSAGE_ABOUT_WIDGET_TAG = "messageAboutWidget";

    private static final String TAG = "mainActivityTAG";
    private static final Integer NOT_SHOW = 0;
    private static final Integer SHOW_WITHOUT_FILTERS = 1;
    private static final Integer SHOW_ALL = 2;
    private static final String URL_FOR_DOWNLOAD_APK = "http://www.bsuir.by/schedule/resources/android/BSUIR_Schedule.apk";
    private static final String ANDROID_APK_FILE_NAME = "/androidSchedule.apk";
    ProgressDialog mProgressDialog;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    private WhoAreYou whoAreYouFragment;
    private DownloadScheduleForGroup downloadScheduleForGroupFragment;
    private DownloadScheduleForEmployee downloadScheduleForEmployeeFragment;
    private ScheduleFragmentForGroup showScheduleFragmentForGroup;

    private WeekNumberEnum selectedWeekNumber = null;
    private SubGroupEnum selectedSubGroup = SubGroupEnum.ENTIRE_GROUP;
    private Integer selectedDayPosition = 0;

    private ScheduleViewPagerFragment scheduleViewPagerFragment;
    private ScheduleExamViewPagerFragment scheduleExamViewPagerFragment;
    private List<SchoolDay> examSchedules;

    private int showNavigationList;
    private boolean changedDayFromViewPager;


    /**
     * Метод вызывается при создании activity. Здесь мы настраиваем необходимые для работы данные, создаем View для отображения пользователю.
     * @param savedInstanceState Если activity была пересоздана после уничтожения, то в {@link #onSaveInstanceState} будут хранится
     *                           данные которые были сохранены перед уничтожением
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        selectedSubGroup = SubGroupEnum.ENTIRE_GROUP;
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));


        whoAreYouFragment = new WhoAreYou();
        downloadScheduleForGroupFragment = new DownloadScheduleForGroup();
        downloadScheduleForEmployeeFragment = new DownloadScheduleForEmployee();
        showScheduleFragmentForGroup = new ScheduleFragmentForGroup();
        scheduleExamViewPagerFragment = new ScheduleExamViewPagerFragment();
        scheduleViewPagerFragment = new ScheduleViewPagerFragment();

        String defaultSchedule = FileUtil.getDefaultSchedule(this);
        //Если дефолтного расписания нету, тогда открываем страницу для скачивания расписания
        if(defaultSchedule == null) {
            onChangeFragment(AvailableFragments.WHO_ARE_YOU);
        } else{
            //Открываем распсание занятий или расписание экзаменов  зависимости от того,
            //что в последний раз смотрел пользователь
            if(FileUtil.isLastUsingDailySchedule(this)) {
                showScheduleFragmentForGroup.setAllScheduleForGroup(getScheduleFromFile(defaultSchedule, false));
                onChangeFragment(AvailableFragments.SHOW_SCHEDULES);
            } else{
                onChangeFragment(AvailableFragments.EXAM_SCHEDULE);
            }
        }
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.downloading));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);

        //Показывает сообщение о том, что теперь доступен виджет
        showMessageAboutWidget();
        //Проверяет наличие обновлений для приложения
        checkForUpdated();
        //Проверяет наличие обновлений для дефолтного расписания пользовтаеля
        checkForUpdateSchedule();
    }

    /**
     * Метод вызывается при выборе элемента на ActionBar
     * @param position позиция нажатого элемента
     * @param id id выбранного элемента
     * @return возвращает true если мы обработали нажатие
     */
    public boolean onNavigationItemSelected(int position, long id) {
        changeFragment(position - 1);
        return true;
    }

    /**
     * Создаем Intent для отправки email сообщения. Пользователю откроется диалоговое окно
     * в котором нужно будет выбрать программу с помощью которой он отправит сообщение.
     * В Intent сохраняет адрес получателя и тему письма.
     */
    public void sendEmail(){
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse(getString(R.string.email_for_reports)));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_report_title));

        startActivity(Intent.createChooser(emailIntent, getString(R.string.chooser_title)));
    }

    /**
     * Возвращает список занятий полученных из файла passedFileName
     * @param passedFileName Имя xml файла из которого считывается расписание
     * @param isForExam Указывает необходимо считать расписание для экзаменов, или же обычное
     *                  расписание занятий. Если необходимо расписание для экзаменов, то к названию
     *                  файла добавляется ".exam"
     * @return
     */
    public List<SchoolDay> getScheduleFromFile(String passedFileName, boolean isForExam){
        String fileName = passedFileName;
        if(isForExam){
            fileName = fileName.substring(0, fileName.length() - 4);
            fileName += "exam.xml";
        }
        return XmlDataProvider.parseScheduleXml(getFilesDir(), fileName);
    }

    /**
     * Функция для проверки наличия обновлений для приложения. Вначале проверяется доступность
     * интернет соединения, и если интернет доступен то проверяется наличие обновлений.
     */
    public void checkForUpdated(){
        ConnectivityManager connectMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectMan.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected()) {
            DownloadActualVersionTask task = new DownloadActualVersionTask();
            task.execute();
        }
    }

    /**
     * Функция для проверки наличия обновления для дефолтного расписания. Вначале проверяется
     * доступность интернет соединения, и если интернет доступен то проверяется наличие
     * обновлений.
     */
    public void checkForUpdateSchedule(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected()){
            String defaultSchedule = FileUtil.getDefaultSchedule(this);
            if(defaultSchedule != null) {
                DownloadLastUpdateDate task = new DownloadLastUpdateDate();
                task.execute(FileUtil.getDefaultSchedule(this));
            }
        }
    }

    /**
     * В функцию передается дата последнего обновления для дефолтного расписания.
     * Из SharedPreferences получаем дату скачивания дефолтного расписание, и если обновление
     * расписания было после того, как пользователь скачал расписание создаем диалоговое окно,
     * в котором уведомляем об наличии обновлений в расписании.
     * @param lastUpdateDate Дата последнего обновления расписания
     */
    public void showDialogForUpdateSchedule(Date lastUpdateDate){
        SharedPreferences preferences = getSharedPreferences(getString(R.string.setting_file_name), 0);
        String defaultSchedule = FileUtil.getDefaultSchedule(this);
        if(defaultSchedule != null && ".xml".equalsIgnoreCase(defaultSchedule.substring(defaultSchedule.length() - 4, defaultSchedule.length()))){
            defaultSchedule = defaultSchedule.substring(0, defaultSchedule.length() - 4);
        }
        String downloadDateForDefaultScheduleAsString = preferences.getString(defaultSchedule, "none");
        if(!"none".equalsIgnoreCase(downloadDateForDefaultScheduleAsString)) {
            Date downloadDateForDefaultSchedule = null;
            try {
                DateFormat df = DateFormat.getDateInstance();
                downloadDateForDefaultSchedule = df.parse(downloadDateForDefaultScheduleAsString);
            } catch (ParseException e) {
                Log.e(TAG, "error while parsing date", e);
            }
            if (downloadDateForDefaultSchedule != null && downloadDateForDefaultSchedule.before(lastUpdateDate)) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.update_needed)
                        .setMessage(R.string.update_schedule_message)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (FileUtil.isDefaultStudentGroup(MainActivity.this)) {
                                    onChangeFragment(AvailableFragments.DOWNLOAD_SCHEDULE_FOR_GROUP);
                                } else {
                                    onChangeFragment(AvailableFragments.DOWNLOAD_SCHEDULE_FOR_EMPLOYEE);
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
            }
        }
    }

    /**
     * В метод передается версия актуального приложения, которая получена из веб-сервиса.
     * После этого сравнивается версия установленная на устройстве с актуальной версией.
     * Если они не совпадает пользователю показывается диалоговое окно с уведомлением об
     * наличии обновления для приложения.
     * @param actualVersion Версия актуального приложения.
     */
    public void showDialogForUpdateApplication(String actualVersion){
        try {
            String currentVersionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            if(!actualVersion.equalsIgnoreCase(currentVersionName)) {
                new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.update_needed))
                        .setMessage(getResources().getString(R.string.update_message))
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final DownloadTask downloadTask = new DownloadTask(MainActivity.this);
                                downloadTask.execute(URL_FOR_DOWNLOAD_APK);

                                mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        downloadTask.cancel(true);
                                    }
                                });

                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
            }
        } catch (PackageManager.NameNotFoundException e){
            Log.v(TAG, e.toString(), e);
        }
    }

    /**
     * При первом запуске приложения пользователя показывается сообщение об наличии виджета.
     */
    public void showMessageAboutWidget(){
        final SharedPreferences preferences = getSharedPreferences(getString(R.string.setting_file_name), 0);
        String messageWasShown = preferences.getString(MESSAGE_ABOUT_WIDGET_TAG, "none");
        if("none".equals(messageWasShown)){
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.warning))
                    .setMessage(getString(R.string.message_about_widget))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final SharedPreferences.Editor editor = preferences.edit();
                            editor.putString(MESSAGE_ABOUT_WIDGET_TAG, "true");
                            editor.apply();
                        }
                    }).show();
        }
    }

    /**
     * Обрабатывает нажатия левого выпдающего меню
     * @param position позиция нажатого элемента
     */
    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        changeFragment(position);
    }

    /**
     * Метод меняет текущий открытый фрагмент. Метод вызывается когда в NavigationDrawer  menu
     * выбирается какой-либо элемент
     * @param position Позиция выбранного элемента. На основе этой позиции выбирается какой
     *                 фрагмент открыть
     */
    private void changeFragment(int position){
        switch (position){
            case 0:
                if(showScheduleFragmentForGroup != null) {
                    onChangeFragment(AvailableFragments.SHOW_SCHEDULES);
                }
                break;
            case 1:
                if(scheduleExamViewPagerFragment != null){
                    onChangeFragment(AvailableFragments.EXAM_SCHEDULE);
                }
                break;
            case 2:
                if(whoAreYouFragment != null) {
                    onChangeFragment(AvailableFragments.WHO_ARE_YOU);
                }
                break;
            case 3:
                sendEmail();
                break;
            default:
                throw new NoSuchElementException();
        }
    }

    /**
     * Метод отрисовки ActionBar. В зависимости от значения showNavigationList отрисовывает
     * различные элементы на ActionBar.
     * @param menu Menu текущей activity
     */
    public void restoreActionBar(final Menu menu) {
        final ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            if (showNavigationList == SHOW_ALL) {
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
                selectedWeekNumber = WeekNumberEnum.ALL;
                selectedSubGroup = SubGroupEnum.ENTIRE_GROUP;
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        R.layout.row_layout, R.id.text1, this.getResources().getStringArray(R.array.day_of_week));
                actionBar.setListNavigationCallbacks(adapter, new ActionBarNavigationListener(menu, actionBar));

                actionBar.setDisplayShowTitleEnabled(false);
                setVisibilityForSubMenus(true, menu);
            } else if(showNavigationList == SHOW_WITHOUT_FILTERS){
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.row_layout_exam_schedule, R.id.text1, getTitleArrayForActionBar());
                actionBar.setListNavigationCallbacks(adapter, new ActionBar.OnNavigationListener() {
                    @Override
                    public boolean onNavigationItemSelected(int itemPosition, long l) {
                        if(!changedDayFromViewPager) {
                            scheduleExamViewPagerFragment.updateFiltersForViewPager(itemPosition);
                            selectedDayPosition = itemPosition;
                            changedDayFromViewPager = false;
                        } else{
                            changedDayFromViewPager = false;
                        }
                        return false;
                    }
                });

                actionBar.setDisplayShowTitleEnabled(false);
                setVisibilityForSubMenus(false, menu);
            } else {
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                setVisibilityForSubMenus(false, menu);
            }
            invalidateOptionsMenu();
        }
    }

    /**
     * Метод обновляем текущий день на ActionBar. Данный метод вызывается при перелистывании
     * дней свапом в расписании занятий.
     * @param dayPosition Номер дня который нужно отобразить. 0 - Понедельник, 6 - Воскресенье
     */
    @Override
    public void onChangeDay(Integer dayPosition) {
        ActionBar actionBar = getSupportActionBar();
        if(notTheSameSelectedDayPosition(dayPosition + 1) && actionBarInNavigationMode()){
            changedDayFromViewPager = true;
            selectedDayPosition = dayPosition + 1;
            actionBar.setSelectedNavigationItem(selectedDayPosition);
        }
    }

    /**
     * Метод обновляет текущий день на ActionBar. Данный метод называется при перелистывании
     * дней свапом в расписании экзаменов.
     * @param dayPosition Номер дня который нужно отобразить. 1 - Понедльник, 7 - Воскресенье.
     */
    @Override
    public void onChangeExamDay(Integer dayPosition){
        ActionBar actionBar = getSupportActionBar();
        if(notTheSameSelectedDayPosition(dayPosition) && actionBarInNavigationMode()){
            changedDayFromViewPager = true;
            selectedDayPosition = dayPosition;
            actionBar.setSelectedNavigationItem(selectedDayPosition);
        }
    }

    /**
     * Метод проверяет текущее состояние ActionBar
     * @return Если текущее состояние ActionBar равно NAVIGATION_MODE_LIST то розвращается true,
     * иначе false
     */
    private boolean actionBarInNavigationMode(){
        ActionBar actionBar = getSupportActionBar();
        return actionBar != null && actionBar.getNavigationMode() == ActionBar.NAVIGATION_MODE_LIST;
    }

    /**
     * Проверяет переданный номер дня на эквивалентность текущему выбранному пользователем дню
     * @param dayPosition Номер дня для сравнения
     * @return Если переданный номер совпадает с текущим номером дня, то возвращается false,
     * если отличаются то возврщается true
     */
    private boolean notTheSameSelectedDayPosition(Integer dayPosition){
        return selectedDayPosition != null && dayPosition != null && !dayPosition.equals(selectedDayPosition);
    }

    /**
     * Скрывает или показывает выпадающие меню для выбора подгруппы и номера недели
     * @param visible Переменная указывающая скрыть или показать выпадающие меню
     * @param menu Меню на котором необходимо показать или скрыть элементы
     */
    public void setVisibilityForSubMenus(boolean visible, Menu menu){
        MenuItem weekNumberSubMenu = menu.findItem(R.id.subMenuWeekNumber);
        MenuItem subGroupSubMenu = menu.findItem(R.id.subMenuSubGroup);
        if(weekNumberSubMenu != null) {
            weekNumberSubMenu.setVisible(visible);
        }
        if(subGroupSubMenu != null) {
            subGroupSubMenu.setVisible(visible);
        }
    }

    /**
     * Возвращает массив дней для выбора в расписании экзаменов
     * @return Возвращает массив дней ввиде массива строк
     */
    public String[] getTitleArrayForActionBar(){
        List<String> titles = new ArrayList<>();
        for(SchoolDay schoolDay : examSchedules){
            titles.add(schoolDay.getDayName());
        }
        return titles.toArray(new String[titles.size()]);
    }

    /**
     * Метод выполняющий замену текущего активного фрагмента
     * @param passedFragment Фрагмент который должен стать текущим
     */
    @Override
    public void onChangeFragment(AvailableFragments passedFragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        switch (passedFragment){
            case DOWNLOAD_SCHEDULE_FOR_EMPLOYEE:
                fragmentTransaction.replace(R.id.fragment_container, downloadScheduleForEmployeeFragment);
                fragmentTransaction.commit();
                showNavigationList = NOT_SHOW;
                break;
            case DOWNLOAD_SCHEDULE_FOR_GROUP:
                fragmentTransaction.replace(R.id.fragment_container, downloadScheduleForGroupFragment);
                fragmentTransaction.commit();
                showNavigationList = NOT_SHOW;
                break;
            case WHO_ARE_YOU:
                fragmentTransaction.replace(R.id.fragment_container, whoAreYouFragment);
                fragmentTransaction.commit();
                showNavigationList = NOT_SHOW;
                break;
            case SHOW_SCHEDULES:
                onSelectShowSchedulesFragment(fragmentTransaction);
                break;
            case EXAM_SCHEDULE:
                onSelectExamScheduleFragment(fragmentTransaction);
                break;
            default:
                break;
        }
    }

    /**
     * Метод вызывается когда необходимо сделать текщим фрагмент расписания экзаменов
     * @param fragmentTransaction FragmentTransaction служит для замены фрагментов
     */
    private void onSelectExamScheduleFragment(FragmentTransaction fragmentTransaction){
        if(!scheduleExamViewPagerFragment.isAdded()) {
            String defaultScheduleFromPreference = FileUtil.getDefaultSchedule(this);
            if (defaultScheduleFromPreference == null) {
                onChangeFragment(AvailableFragments.WHO_ARE_YOU);
            } else {
                fragmentTransaction.replace(R.id.fragment_container, scheduleExamViewPagerFragment);
                fragmentTransaction.commit();
                List<SchoolDay> week = getScheduleFromFile(defaultScheduleFromPreference, true);
                examSchedules = week;
                scheduleExamViewPagerFragment.setAllSchedules(week);
                scheduleExamViewPagerFragment.setCurrentSelectedIndex(selectedDayPosition);
                showNavigationList = SHOW_WITHOUT_FILTERS;
            }
            updateLastUsingSchedule(LAST_USING_EXAM_SCHEDULE_TAG);
            invalidateOptionsMenu();
            updateWidgets();
        }
    }

    /**
     * Метод вызывается когда необходимо сделать текщим фрагмент расписания занятий
     * @param fragmentTransaction FragmentTransaction служит для замены фрагментов
     */
    private void onSelectShowSchedulesFragment(FragmentTransaction fragmentTransaction){
        if(!scheduleViewPagerFragment.isAdded()) {
            String defaultSchedule = FileUtil.getDefaultSchedule(this);
            if (defaultSchedule == null) {
                onChangeFragment(AvailableFragments.WHO_ARE_YOU);
            } else {
                Calendar calendar = GregorianCalendar.getInstance();
                int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
                if (currentDay == Calendar.SUNDAY) {
                    currentDay = 8;
                }
                if(selectedWeekNumber == null){
                    selectedWeekNumber = WeekNumberEnum.getByOrder(DateUtil.getWeek(Calendar.getInstance().getTime()));
                }

                scheduleViewPagerFragment = ScheduleViewPagerFragment.newInstance(getScheduleFromFile(defaultSchedule, false), currentDay - 2, selectedWeekNumber, selectedSubGroup);
                fragmentTransaction.replace(R.id.fragment_container, scheduleViewPagerFragment);
                fragmentTransaction.commit();
            }
            updateLastUsingSchedule(LAST_USING_DAILY_SCHEDULE_TAG);
            showNavigationList = SHOW_ALL;
            changedDayFromViewPager = false;
            invalidateOptionsMenu();
            updateWidgets();
        }
    }

    /**
     * Метод обновляет все виджеты данного приложения
     */
    private void updateWidgets(){
        Context context = getApplicationContext();
        ComponentName name = new ComponentName(context, ScheduleWidgetProvider.class);
        int [] ids = AppWidgetManager.getInstance(context).getAppWidgetIds(name);

        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, this, ScheduleWidgetProvider.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                new ComponentName(context, ScheduleWidgetProvider.class));
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.listViewWidget);
    }

    /**
     * Обновляет данные о том, что использовал пользователь в последний раз: расписание занятий
     * или расписание экзаменов
     * @param tag Информация о том, что использовал пользователь в последний раз
     */
    public void updateLastUsingSchedule(String tag){
        final SharedPreferences preferences = getSharedPreferences(getString(R.string.setting_file_name), 0);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putString(LAST_USING_SCHEDULE, tag);
        editor.apply();
    }

    /**
     * Функция обработчик нажатия кнопки "Назад"
     */
    @Override
    public void onBackPressed() {
        //TODO: determine why don't work fragmentTransaction.addToBackStack and rewrite this method
        String defaultSchedule = FileUtil.getDefaultSchedule(this);
        if(scheduleViewPagerFragment.isAdded()) {
            System.exit(0);
        }
        if(defaultSchedule == null && whoAreYouFragment.isAdded()){
            System.exit(0);
        }
         if(!whoAreYouFragment.isAdded()){
             if(defaultSchedule != null) {
                 onChangeFragment(AvailableFragments.SHOW_SCHEDULES);
             } else{
                 onChangeFragment(AvailableFragments.WHO_ARE_YOU);
             }
        } else{
            onChangeFragment(AvailableFragments.SHOW_SCHEDULES);
        }
    }

    /**
     * Инициализурет option menu
     * @param menu в данное menu можно добавлять свои элементы
     * @return возвращает true если нужно чтобы option menu отображалось
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar(menu);
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Функция обрабатывает нажатия на элементы в ActionBar
     * @param item Ссылка на элемент, на который нажал пользователь
     * @return Return false to allow normal menu processing to
     *         proceed, true to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        ActionMenuItemView weekNumberSubMenu = (ActionMenuItemView) findViewById(R.id.subMenuWeekNumber);
        ActionMenuItemView subGroupSubMenu = (ActionMenuItemView) findViewById(R.id.subMenuSubGroup);
        int dayPositionForPass = selectedDayPosition;
        dayPositionForPass = determineSelectedDayPosition(dayPositionForPass);
        switch (id){
            case R.id.menuAllWeekNumber:
                selectedWeekNumber = WeekNumberEnum.ALL;
                scheduleViewPagerFragment.updateFiltersForViewPager(dayPositionForPass - 1, selectedWeekNumber, selectedSubGroup);
                weekNumberSubMenu.setTitle(getResources().getString(R.string.all_week_number));
                break;
            case R.id.menuFirstWeekNumber:
                selectedWeekNumber = WeekNumberEnum.FIRST;
                scheduleViewPagerFragment.updateFiltersForViewPager(dayPositionForPass - 1, selectedWeekNumber, selectedSubGroup);
                weekNumberSubMenu.setTitle(getResources().getString(R.string.first_week_number));
                break;
            case R.id.menuSecondWeekNumber:
                selectedWeekNumber = WeekNumberEnum.SECOND;
                scheduleViewPagerFragment.updateFiltersForViewPager(dayPositionForPass - 1, selectedWeekNumber, selectedSubGroup);
                weekNumberSubMenu.setTitle(getResources().getString(R.string.second_week_number));
                break;
            case R.id.menuThirdWeekNumber:
                selectedWeekNumber = WeekNumberEnum.THIRD;
                scheduleViewPagerFragment.updateFiltersForViewPager(dayPositionForPass - 1, selectedWeekNumber, selectedSubGroup);
                weekNumberSubMenu.setTitle(getResources().getString(R.string.third_week_number));
                break;
            case R.id.menuFourthWeekNumber:
                selectedWeekNumber = WeekNumberEnum.FOURTH;
                scheduleViewPagerFragment.updateFiltersForViewPager(dayPositionForPass - 1, selectedWeekNumber, selectedSubGroup);
                weekNumberSubMenu.setTitle(getResources().getString(R.string.fourth_week_number));
                break;
            case R.id.menuEntireGroup:
                selectedSubGroup = SubGroupEnum.ENTIRE_GROUP;
                scheduleViewPagerFragment.updateFiltersForViewPager(dayPositionForPass - 1, selectedWeekNumber, selectedSubGroup);
                subGroupSubMenu.setTitle(getResources().getString(R.string.entire_group));
                break;
            case R.id.menuFirstSubGroup:
                selectedSubGroup = SubGroupEnum.FIRST_SUB_GROUP;
                scheduleViewPagerFragment.updateFiltersForViewPager(dayPositionForPass - 1, selectedWeekNumber, selectedSubGroup);
                subGroupSubMenu.setTitle(getResources().getString(R.string.first_sub_group));
                break;
            case R.id.menuSecondSubGroup:
                selectedSubGroup = SubGroupEnum.SECOND_SUB_GROUP;
                scheduleViewPagerFragment.updateFiltersForViewPager(dayPositionForPass - 1, selectedWeekNumber, selectedSubGroup);
                subGroupSubMenu.setTitle(getResources().getString(R.string.second_sub_group));
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Функция определяет номер выбранного дня
     * @param passedDayPosition Номер выбранного элементы из выпадающего меню. 0 соответствует пункту
     *                          меню "Сегодня"
     * @return Возвращает номер дня
     */
    private static Integer determineSelectedDayPosition(Integer passedDayPosition){
        Integer dayPositionForPass = passedDayPosition;
        if(dayPositionForPass == 0){
            Calendar calendar = new GregorianCalendar();
            dayPositionForPass = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            if(dayPositionForPass == 0){
                dayPositionForPass = 7;
            }
        }
        return dayPositionForPass;
    }

    /**
     * Асинхронная класс для скачивания актуальной версии приложения
     */
    private class DownloadActualVersionTask extends AsyncTask<Void, String, String> {

        /**
         * Метод выполняющийся в фоне, который скачивает актуальную версиию приложения
         * @param parameters Список параметров. В данном случае параметры не нужны
         * @return Возвращает актуальную версию приложения.
         */
        protected String doInBackground(Void... parameters) {
            return LoadSchedule.loadActualApplicationVersion();
        }

        /**
         * Метод вызывающий после скачивания актуальной версии приложения. Служит для показа
         * диалогового окна об наличии обновлений для приложений.
         * @param result Актуальная версия приложения
         */
        protected void onPostExecute(String result) {
            if(result != null){
                showDialogForUpdateApplication(result);
            }
        }
    }

    /**
     * Асинхронный класс для скачивания информации об последнем обновлении дефолтного расписания.
     */
    private class DownloadLastUpdateDate extends AsyncTask<String, Date, Date>{

        /**
         * Метод который в фоне скачивает дату последнего обновления для расписания
         * @param parameters Номер группы, или имя преподавателя для которого необходимо скачать
         *                   последнюю дату обновления расписания         *
         * @return Возвращает дату последнего обновления
         */
        @Override
        protected Date doInBackground(String... parameters){
            if(FileUtil.isDefaultStudentGroup(MainActivity.this)){
                return LoadSchedule.loadLastUpdateDateForStudentGroup(parameters[0]);
            } else{
                return LoadSchedule.loadLastUpdateDateForEmployee(parameters[0]);
            }
        }

        /**
         * Метод вызывается после скачивания даты последнего обновления расписания. Служит для
         * отображения диалогового окна, если дата обновления позже даты скачивания расписания
         * @param result Дата последнего обновления расписания.
         */
        @Override
        protected void onPostExecute(Date result){
            if(result != null){
                showDialogForUpdateSchedule(result);
            }
        }
    }

    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        /**
         * Асинхронный класс который скачивает новую версию приложения
         * @param context контекст
         */
        public DownloadTask(Context context) {
            this.context = context;
        }

        /**
         * Метод который в фоне скачивает apk файл.
         * @param sUrl Ссылка на apk файл
         * @return Возвращает null, если скачивание завершено успешно, иначе возвращает
         * сообщение об ошибки
         */
        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                output = new FileOutputStream(Environment.getExternalStorageDirectory().getPath() + ANDROID_APK_FILE_NAME);

                writeDataToFile(fileLength, input, output);
            } catch (Exception e) {
                Log.v(TAG, e.getMessage(), e);
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                    Log.v(TAG, ignored.getMessage(), ignored);
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        /**
         * Сохраняет apk файл на устройстве
         * @param fileLength количество байтов apk файла
         * @param input входной поток, из которого считывается apk файл
         * @param output выходной поток, куда записывается apk файл
         * @throws IOException
         */
        private void writeDataToFile(int fileLength, InputStream input, OutputStream output) throws IOException{
            byte[] data = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                // allow canceling with back button
                if (isCancelled()) {
                    input.close();
                    return ;
                }
                total += count;
                // publishing the progress....
                if (fileLength > 0) // only if total length is known
                    publishProgress((int) (total * 100 / fileLength));
                output.write(data, 0, count);
            }
        }

        /**
         * метод вызывается перед началом скачивания новой версии приложения. Служит для настройки
         * диалогового окна, который показывает состояние скачивания приложения
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager manager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            mProgressDialog.show();
        }

        /**
         * Вызывается во время скачивания новой версии приложения, для оторбражения пользователю
         * состояния скачивания
         * @param progress Количество процентов скачанного приложения
         */
        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            mProgressDialog.setMax(100);
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setProgress(progress[0]);
        }

        /**
         * Метод вызывается после того как новое приложение было скачано. Начинает установку
         * новой версии приложения.
         * @param result результат скачивания. Если null, значит скачивание прошло успешно
         */
        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            mProgressDialog.dismiss();
            if (result != null) {
                Toast.makeText(context, "Download error: " + result, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "File downloaded", Toast.LENGTH_SHORT).show();
                File apkFile = new File(Environment.getExternalStorageDirectory().getPath() + ANDROID_APK_FILE_NAME);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
                startActivity(intent);
            }
        }
    }

    private class ActionBarNavigationListener implements ActionBar.OnNavigationListener {
        private Menu menu;
        private ActionBar actionBar;

        /**
         * Обработчик выбора дня на ActionBar
         * @param passedMenu меню
         * @param supportedActionBar actionBar
         */
        public ActionBarNavigationListener(Menu passedMenu, ActionBar supportedActionBar){
            menu = passedMenu;
            actionBar = supportedActionBar;
        }

        /**
         * Метод вызывается когда пользователь выбрал день на ActionBar
         * @param itemPosition номер дня выбранного пользователем
         * @param itemId Ссылка на элемент выбранного пользователем
         * @return возврщает false для того, чтобы вызывался родительский метод.
         */
        @Override
        public boolean onNavigationItemSelected(int itemPosition, long itemId) {
            if(!changedDayFromViewPager) {
                if (itemPosition == 0) {
                    Calendar calendar = GregorianCalendar.getInstance();
                    int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
                    if (currentDay == Calendar.SUNDAY) {
                        currentDay = 8;
                    }
                    MenuItem subMenuWeekNumber = menu.findItem(R.id.subMenuWeekNumber);
                    Integer currentWeekNumber = DateUtil.getWeek(calendar.getTime());
                    if(currentWeekNumber != null) {
                        selectedWeekNumber = updateSelectedWeekNumber(currentWeekNumber, subMenuWeekNumber);
                    }
                    actionBar.setSelectedNavigationItem(currentDay - 1);
                } else {
                    scheduleViewPagerFragment.updateFiltersForViewPager(itemPosition - 1, selectedWeekNumber, selectedSubGroup);
                    selectedDayPosition = itemPosition;
                    changedDayFromViewPager = false;
                }
            } else{
                changedDayFromViewPager = false;
            }
            return false;
        }

        /**
         * Обновляет текущую выбранную неделю
         * @param currentWeekNumber Выбранная пользова
         * @param subMenuWeekNumber Номер элемента выбранного пользовтаелем из выпадающего списка
         *                          номеров недель
         * @return Возвращает текущую выбранную неделю
         */
        private WeekNumberEnum updateSelectedWeekNumber(Integer currentWeekNumber, MenuItem subMenuWeekNumber){
            WeekNumberEnum result;
            switch (currentWeekNumber) {
                case 1:
                    subMenuWeekNumber.setTitle(R.string.first_week_number);
                    result = WeekNumberEnum.FIRST;
                    break;
                case 2:
                    subMenuWeekNumber.setTitle(R.string.second_week_number);
                    result = WeekNumberEnum.SECOND;
                    break;
                case 3:
                    subMenuWeekNumber.setTitle(R.string.third_week_number);
                    result = WeekNumberEnum.THIRD;
                    break;
                case 4:
                    subMenuWeekNumber.setTitle(R.string.fourth_week_number);
                    result = WeekNumberEnum.FOURTH;
                    break;
                default:
                    subMenuWeekNumber.setTitle(R.string.all_week_number);
                    result = WeekNumberEnum.ALL;
                    break;
            }
            return result;
        }
    }
}
