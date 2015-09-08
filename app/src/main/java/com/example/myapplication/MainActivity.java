package com.example.myapplication;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.example.myapplication.DataProvider.LoadSchedule;
import com.example.myapplication.Utils.DateUtil;
import com.example.myapplication.Utils.FileUtil;
import com.example.myapplication.DataProvider.XmlDataProvider;
import com.example.myapplication.Model.AvailableFragments;
import com.example.myapplication.Model.SchoolDay;
import com.example.myapplication.Model.SubGroupEnum;
import com.example.myapplication.Model.WeekNumberEnum;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.NoSuchElementException;

public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, OnFragmentInteractionListener, ActionBar.OnNavigationListener {
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
    private ExamScheduleFragment examScheduleFragment;


    private WeekNumberEnum selectedWeekNumber = WeekNumberEnum.ALL;
    private SubGroupEnum selectedSubGroup = SubGroupEnum.ENTIRE_GROUP;
    private Integer selectedDayPosition;

    private ScheduleViewPagerFragment scheduleViewPagerFragment;
    private List<SchoolDay> examSchedules;

    private int showNavigationList;
    private boolean changedDayFromViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        selectedWeekNumber = WeekNumberEnum.ALL;
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
        examScheduleFragment = new ExamScheduleFragment();
        scheduleViewPagerFragment = new ScheduleViewPagerFragment();
        String defaultSchedule = FileUtil.getDefaultSchedule(this);
        if(defaultSchedule == null) {
            onChangeFragment(AvailableFragments.WhoAreYou);
        } else{
            showScheduleFragmentForGroup.setAllScheduleForGroup(getScheduleFromFile(defaultSchedule, false));
            onChangeFragment(AvailableFragments.ShowSchedules);
        }
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.downloading));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);
        checkForUpdated();
    }

    public boolean onNavigationItemSelected(int position, long id) {
        switch (position){
            case 1:
                if(showScheduleFragmentForGroup != null) {
                    onChangeFragment(AvailableFragments.ShowSchedules);
                }
                break;
            case 2:
                if(examScheduleFragment != null){
                    onChangeFragment(AvailableFragments.ExamSchedule);
                }
                break;
            case 3:
                if(whoAreYouFragment != null) {
                    onChangeFragment(AvailableFragments.WhoAreYou);
                }
                break;
            case 4:
                sendEmail();
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return true;
    }

    public void sendEmail(){
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse(getString(R.string.email_for_reports)));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_report_title));

        startActivity(Intent.createChooser(emailIntent, getString(R.string.chooser_title)));
    }

    public List<SchoolDay> getScheduleFromFile(String fileName, boolean isForExam){
        if(isForExam){
            fileName = fileName.substring(0, fileName.length() - 4);
            fileName += "exam.xml";
        }
        return XmlDataProvider.parseScheduleXml(getFilesDir(), fileName);
    }

    public void checkForUpdated(){
        ConnectivityManager connectMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectMan.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected()) {
            DownloadActualVersionTask task = new DownloadActualVersionTask();
            task.execute();
        }
    }

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
            Log.v(TAG, e.toString());
        }
    }
    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        switch (position){
            case 0:
                if(showScheduleFragmentForGroup != null) {
                    onChangeFragment(AvailableFragments.ShowSchedules);
                }
                break;
            case 1:
                if(examScheduleFragment != null){
                    onChangeFragment(AvailableFragments.ExamSchedule);
                }
                break;
            case 2:
                if(whoAreYouFragment != null) {
                    onChangeFragment(AvailableFragments.WhoAreYou);
                }
                break;
            case 3:
                sendEmail();
                break;
            default:
                throw new NoSuchElementException();
        }
    }

    public void restoreActionBar(final Menu menu) {
        final ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            if (showNavigationList == SHOW_ALL) {
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
                selectedWeekNumber = WeekNumberEnum.ALL;
                selectedSubGroup = SubGroupEnum.ENTIRE_GROUP;
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        R.layout.row_layout, R.id.text1, this.getResources().getStringArray(R.array.day_of_week));
                actionBar.setListNavigationCallbacks(adapter, new ActionBar.OnNavigationListener() {
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
                                    switch (currentWeekNumber) {
                                        case 1:
                                            subMenuWeekNumber.setTitle(R.string.first_week_number);
                                            selectedWeekNumber = WeekNumberEnum.FIRST;
                                            break;
                                        case 2:
                                            subMenuWeekNumber.setTitle(R.string.second_week_number);
                                            selectedWeekNumber = WeekNumberEnum.SECOND;
                                            break;
                                        case 3:
                                            subMenuWeekNumber.setTitle(R.string.third_week_number);
                                            selectedWeekNumber = WeekNumberEnum.THIRD;
                                            break;
                                        case 4:
                                            subMenuWeekNumber.setTitle(R.string.fourth_week_number);
                                            selectedWeekNumber = WeekNumberEnum.FOURTH;
                                            break;
                                        default:
                                            subMenuWeekNumber.setTitle(R.string.all_week_number);
                                            selectedWeekNumber = WeekNumberEnum.ALL;
                                            break;
                                    }
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
                });

                actionBar.setDisplayShowTitleEnabled(false);
                setVisibilityForSubMenus(true, menu);
            } else if(showNavigationList == SHOW_WITHOUT_FILTERS){
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.row_layout, R.id.text1, getTitleArrayForActionBar());
                actionBar.setListNavigationCallbacks(adapter, new ActionBar.OnNavigationListener() {
                    @Override
                    public boolean onNavigationItemSelected(int itemPosition, long l) {
                        examScheduleFragment.updateSchedule(itemPosition - 1);
                        selectedDayPosition = itemPosition;
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

    @Override
    public void onChangeDay(Integer dayPosition) {
        ActionBar actionBar = getSupportActionBar();
        if(selectedDayPosition != null && dayPosition != null && (dayPosition + 1 != selectedDayPosition)) {
            if (actionBar != null && actionBar.getNavigationMode() == ActionBar.NAVIGATION_MODE_LIST) {
                changedDayFromViewPager = true;
                selectedDayPosition = dayPosition + 1;
                actionBar.setSelectedNavigationItem(dayPosition + 1);
            }
        }
    }


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

    public String[] getTitleArrayForActionBar(){
        List<String> titles = new ArrayList<>();
        for(SchoolDay schoolDay : examSchedules){
            titles.add(schoolDay.getDayName());
        }
        return titles.toArray(new String[titles.size()]);
    }

    @Override
    public void onChangeFragment(AvailableFragments passedFragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        switch (passedFragment){
            case DownloadScheduleForEmployee:
                fragmentTransaction.replace(R.id.fragment_container, downloadScheduleForEmployeeFragment);
                fragmentTransaction.commit();
                showNavigationList = NOT_SHOW;
                break;
            case DownloadScheduleForGroup:
                fragmentTransaction.replace(R.id.fragment_container, downloadScheduleForGroupFragment);
                fragmentTransaction.commit();
                showNavigationList = NOT_SHOW;
                break;
            case WhoAreYou:
                fragmentTransaction.replace(R.id.fragment_container, whoAreYouFragment);
                fragmentTransaction.commit();
                showNavigationList = NOT_SHOW;
                break;
            case ShowSchedules:
                if(!scheduleViewPagerFragment.isAdded()) {
                    String defaultSchedule = FileUtil.getDefaultSchedule(this);
                    if (defaultSchedule == null) {
                        onChangeFragment(AvailableFragments.WhoAreYou);
                    } else {
                        fragmentTransaction.replace(R.id.fragment_container, scheduleViewPagerFragment);
                        fragmentTransaction.commit();
                        scheduleViewPagerFragment.setAllWeekSchedules(getScheduleFromFile(defaultSchedule, false));

                        Calendar calendar = GregorianCalendar.getInstance();
                        int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
                        if (currentDay == Calendar.SUNDAY) {
                            currentDay = 8;
                        }
                        scheduleViewPagerFragment.setCurrentMiddleIndex(currentDay - 2);
                        scheduleViewPagerFragment.setSelectedWeekNumber(selectedWeekNumber);
                        scheduleViewPagerFragment.setSelectedSubGroup(selectedSubGroup);
                    }
                    showNavigationList = SHOW_ALL;
                    changedDayFromViewPager = false;
                    invalidateOptionsMenu();
                }
                break;
            case ExamSchedule:
                String defaultScheduleFromPreference = FileUtil.getDefaultSchedule(this);
                if(defaultScheduleFromPreference == null){
                    onChangeFragment(AvailableFragments.WhoAreYou);
                } else {
                    fragmentTransaction.replace(R.id.fragment_container, examScheduleFragment);
                    fragmentTransaction.commit();
                    getFragmentManager().executePendingTransactions();
                    List<SchoolDay> week = getScheduleFromFile(defaultScheduleFromPreference, true);
                    examSchedules = week;
                    examScheduleFragment.setAllSchedules(week);
                    examScheduleFragment.updateSchedule(selectedDayPosition);
                    showNavigationList = SHOW_WITHOUT_FILTERS;
                }
                invalidateOptionsMenu();
                break;
            default:
                break;
        }

    }

    @Override
    public void onBackPressed() {
        //TODO: determine why don't work fragmentTransaction.addToBackStack and rewrite this method
        String defaultSchedule = FileUtil.getDefaultSchedule(this);
        if(scheduleViewPagerFragment.isAdded()) {
            System.exit(0);
        } else if(!whoAreYouFragment.isAdded()){
            onChangeFragment(AvailableFragments.ShowSchedules);
        } else if(defaultSchedule == null){
            System.exit(0);
        } else{
            onChangeFragment(AvailableFragments.ShowSchedules);
        }
    }

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        ActionMenuItemView weekNumberSubMenu = (ActionMenuItemView) findViewById(R.id.subMenuWeekNumber);
        ActionMenuItemView subGroupSubMenu = (ActionMenuItemView) findViewById(R.id.subMenuSubGroup);
        int dayPositionForPass = selectedDayPosition;
        if(dayPositionForPass == 0){
            Calendar calendar = new GregorianCalendar();
            dayPositionForPass = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            if(dayPositionForPass == 0){
                dayPositionForPass = 7;
            }
        }
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
        }
        return super.onOptionsItemSelected(item);
    }



    private class DownloadActualVersionTask extends AsyncTask<Void, String, String> {

        protected String doInBackground(Void... parameters) {
            return LoadSchedule.loadActualApplicationVersion();
        }

        protected void onPostExecute(String result) {
            if(result != null){
                showDialogForUpdateApplication(result);
            }
        }
    }

    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public DownloadTask(Context context) {
            this.context = context;
        }

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
                //output = new FileOutputStream("/sdcard/file_name.extension");

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(progress[0]);
        }

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
}
