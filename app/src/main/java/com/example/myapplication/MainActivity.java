package com.example.myapplication;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.internal.view.menu.ActionMenuItemView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ArrayAdapter;

import com.example.myapplication.DataProvider.LoadSchedule;
import com.example.myapplication.Utils.FileUtil;
import com.example.myapplication.DataProvider.XmlDataProvider;
import com.example.myapplication.Model.AvailableFragments;
import com.example.myapplication.Model.SchoolDay;
import com.example.myapplication.Model.SubGroupEnum;
import com.example.myapplication.Model.WeekNumberEnum;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.NoSuchElementException;

public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, OnFragmentInteractionListener, ActionBar.OnNavigationListener {
    private static final String TAG = "mainActivityTAG";

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
        String defaultSchedule = FileUtil.getDefaultSchedule(this);
        if(defaultSchedule == null) {
            onChangeFragment(AvailableFragments.WhoAreYou);
        } else{
            showScheduleFragmentForGroup.setAllScheduleForGroup(getScheduleFromFile(defaultSchedule));
            onChangeFragment(AvailableFragments.ShowSchedules);
        }
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
            default:
                throw new UnsupportedOperationException();
        }
        return true;
    }

    public List<SchoolDay> getScheduleFromFile(String fileName){
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
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_for_update_application)));
                                startActivity(browserIntent);
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
            default:
                throw new NoSuchElementException();
        }
    }

    public void restoreActionBar(Menu menu) {
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            if (showScheduleFragmentForGroup.isAdded()) {
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                        R.layout.row_layout, R.id.text1, this.getResources().getStringArray(R.array.day_of_week));
                actionBar.setListNavigationCallbacks(adapter, new ActionBar.OnNavigationListener() {
                    @Override
                    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                        if(itemPosition == 0){
                            Calendar calendar = GregorianCalendar.getInstance();
                            int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
                            showScheduleFragmentForGroup.updateSchedule(currentDay - 2);
                            showScheduleFragmentForGroup.filterScheduleList(currentDay - 2, selectedWeekNumber, selectedSubGroup);
                            selectedDayPosition = itemPosition;
                        } else {
                            showScheduleFragmentForGroup.updateSchedule(itemPosition - 1);
                            showScheduleFragmentForGroup.filterScheduleList(itemPosition - 1, selectedWeekNumber, selectedSubGroup);
                            selectedDayPosition = itemPosition;
                        }
                        return false;
                    }
                });

                actionBar.setDisplayShowTitleEnabled(false);
                setVisibilityForSubMenus(true, menu);
            } else {
                actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                setVisibilityForSubMenus(false, menu);
            }
            invalidateOptionsMenu();
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

    @Override
    public void onChangeFragment(AvailableFragments passedFragment) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        switch (passedFragment){
            case DownloadScheduleForEmployee:
                fragmentTransaction.replace(R.id.fragment_container, downloadScheduleForEmployeeFragment);
                break;
            case DownloadScheduleForGroup:
                fragmentTransaction.replace(R.id.fragment_container, downloadScheduleForGroupFragment);
                break;
            case WhoAreYou:
                fragmentTransaction.replace(R.id.fragment_container, whoAreYouFragment);
                break;
            case ShowSchedules:
                String defaultSchedule = FileUtil.getDefaultSchedule(this);
                if(defaultSchedule == null) {
                    onChangeFragment(AvailableFragments.WhoAreYou);
                } else {
                    showScheduleFragmentForGroup.setAllScheduleForGroup(getScheduleFromFile(defaultSchedule));
                    if(selectedDayPosition != null && selectedDayPosition > 1) {
                        showScheduleFragmentForGroup.updateSchedule(selectedDayPosition - 1);
                        showScheduleFragmentForGroup.filterScheduleList(selectedDayPosition - 1, selectedWeekNumber, selectedSubGroup);
                    } else {
                        showScheduleFragmentForGroup.updateSchedule(0);
                        showScheduleFragmentForGroup.filterScheduleList(0, selectedWeekNumber, selectedSubGroup);
                    }
                    fragmentTransaction.replace(R.id.fragment_container, showScheduleFragmentForGroup);
                }
                invalidateOptionsMenu();
                break;
            case ExamSchedule:
                fragmentTransaction.replace(R.id.fragment_container, examScheduleFragment);
                break;
            default:
                break;
        }
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        if(showScheduleFragmentForGroup.isAdded()) {
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
        }
        switch (id){
            case R.id.menuAllWeekNumber:
                selectedWeekNumber = WeekNumberEnum.ALL;
                showScheduleFragmentForGroup.filterScheduleList(dayPositionForPass - 1, selectedWeekNumber, selectedSubGroup);
                weekNumberSubMenu.setTitle(getResources().getString(R.string.all_week_number));
                break;
            case R.id.menuFirstWeekNumber:
                selectedWeekNumber = WeekNumberEnum.FIRST;
                showScheduleFragmentForGroup.filterScheduleList(dayPositionForPass - 1, selectedWeekNumber, selectedSubGroup);
                weekNumberSubMenu.setTitle(getResources().getString(R.string.first_week_number));
                break;
            case R.id.menuSecondWeekNumber:
                selectedWeekNumber = WeekNumberEnum.SECOND;
                showScheduleFragmentForGroup.filterScheduleList(dayPositionForPass - 1, selectedWeekNumber, selectedSubGroup);
                weekNumberSubMenu.setTitle(getResources().getString(R.string.second_week_number));
                break;
            case R.id.menuThirdWeekNumber:
                selectedWeekNumber = WeekNumberEnum.THIRD;
                showScheduleFragmentForGroup.filterScheduleList(dayPositionForPass - 1, selectedWeekNumber, selectedSubGroup);
                weekNumberSubMenu.setTitle(getResources().getString(R.string.third_week_number));
                break;
            case R.id.menuFourthWeekNumber:
                selectedWeekNumber = WeekNumberEnum.FOURTH;
                showScheduleFragmentForGroup.filterScheduleList(dayPositionForPass - 1, selectedWeekNumber, selectedSubGroup);
                weekNumberSubMenu.setTitle(getResources().getString(R.string.fourth_week_number));
                break;
            case R.id.menuEntireGroup:
                selectedSubGroup = SubGroupEnum.ENTIRE_GROUP;
                showScheduleFragmentForGroup.filterScheduleList(dayPositionForPass - 1, selectedWeekNumber, selectedSubGroup);
                subGroupSubMenu.setTitle(getResources().getString(R.string.entire_group));
                break;
            case R.id.menuFirstSubGroup:
                selectedSubGroup = SubGroupEnum.FIRST_SUB_GROUP;
                showScheduleFragmentForGroup.filterScheduleList(dayPositionForPass - 1, selectedWeekNumber, selectedSubGroup);
                subGroupSubMenu.setTitle(getResources().getString(R.string.first_sub_group));
                break;
            case R.id.menuSecondSubGroup:
                selectedSubGroup = SubGroupEnum.SECOND_SUB_GROUP;
                showScheduleFragmentForGroup.filterScheduleList(dayPositionForPass - 1, selectedWeekNumber, selectedSubGroup);
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
}
