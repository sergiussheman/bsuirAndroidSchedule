package com.example.myapplication;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.internal.view.menu.ActionMenuItemView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;

import com.example.myapplication.Utils.FileUtil;
import com.example.myapplication.DataProvider.XmlDataProvider;
import com.example.myapplication.Model.AvailableFragments;
import com.example.myapplication.Model.SchoolDay;
import com.example.myapplication.Model.SubGroupEnum;
import com.example.myapplication.Model.WeekNumberEnum;

import java.util.List;

public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, OnFragmentInteractionListener, ActionBar.OnNavigationListener {


    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    private WhoAreYou whoAreYouFragment;
    private DownloadScheduleForGroup downloadScheduleForGroupFragment;
    private DownloadScheduleForEmployee downloadScheduleForEmployeeFragment;
    private ScheduleFragmentForGroup showScheduleFragmentForGroup;


    private WeekNumberEnum selectedWeekNumber;
    private SubGroupEnum selectedSubGroup;
    private Integer selectedDayPosition;
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        selectedWeekNumber = WeekNumberEnum.ALL;
        selectedSubGroup = SubGroupEnum.ENTIRE_GROUP;
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));


        whoAreYouFragment = new WhoAreYou();
        downloadScheduleForGroupFragment = new DownloadScheduleForGroup();
        downloadScheduleForEmployeeFragment = new DownloadScheduleForEmployee();
        showScheduleFragmentForGroup = new ScheduleFragmentForGroup();
        String defaultSchedule = FileUtil.getDefaultSchedule(this);
        if(defaultSchedule == null) {
            onChangeFragment(AvailableFragments.WhoAreYou);
        } else{
            showScheduleFragmentForGroup.setAllScheduleForGroup(getScheduleFromFile(defaultSchedule));
            onChangeFragment(AvailableFragments.ShowSchedules);
        }
    }

    public boolean onNavigationItemSelected(int position, long id) {
        switch (position){
            case 1:
                onChangeFragment(AvailableFragments.ShowSchedules);
                break;
            case 2:
                break;
            case 3:
                onChangeFragment(AvailableFragments.WhoAreYou);
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return true;
    }

    public List<SchoolDay> getScheduleFromFile(String fileName){
        return XmlDataProvider.parseScheduleXml(getFilesDir(), fileName);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                onChangeFragment(AvailableFragments.ShowSchedules);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                onChangeFragment(AvailableFragments.WhoAreYou);
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    public void restoreActionBar(Menu menu) {
        ActionBar actionBar = getSupportActionBar();
        if(showScheduleFragmentForGroup.isAdded()) {
            assert actionBar != null;
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    R.layout.row_layout, R.id.text1, this.getResources().getStringArray(R.array.day_of_week));
            assert actionBar != null;
            actionBar.setListNavigationCallbacks(adapter, new ActionBar.OnNavigationListener() {
                @Override
                public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                    showScheduleFragmentForGroup.updateSchedule(itemPosition);
                    selectedDayPosition = itemPosition;
                    return false;
                }
            });

            actionBar.setDisplayShowTitleEnabled(false);
            setVisibilityForSubMenus(true, menu);
        } else{
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            setVisibilityForSubMenus(false, menu);
        }
        invalidateOptionsMenu();
        //actionBar.setTitle(mTitle);
    }

    public void setVisibilityForSubMenus(boolean visible, Menu menu){
        //MenuItem weekNumberSubMenu = (MenuItem) findViewById(R.id.subMenuWeekNumber);
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
                    if(selectedDayPosition != null) {
                        showScheduleFragmentForGroup.updateSchedule(selectedDayPosition);
                    } else {
                        showScheduleFragmentForGroup.updateSchedule(0);
                    }
                }
                fragmentTransaction.replace(R.id.fragment_container, showScheduleFragmentForGroup);
                invalidateOptionsMenu();
                break;
            default:
                break;
        }
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
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
        //noinspection SimplifiableIfStatement
        switch (id){
            case R.id.menuAllWeekNumber:
                selectedWeekNumber = WeekNumberEnum.ALL;
                showScheduleFragmentForGroup.filterScheduleList(selectedDayPosition, selectedWeekNumber, selectedSubGroup);
                weekNumberSubMenu.setTitle(getResources().getString(R.string.all_week_number));
                break;
            case R.id.menuFirstWeekNumber:
                selectedWeekNumber = WeekNumberEnum.FIRST;
                showScheduleFragmentForGroup.filterScheduleList(selectedDayPosition, selectedWeekNumber, selectedSubGroup);
                weekNumberSubMenu.setTitle(getResources().getString(R.string.first_week_number));
                break;
            case R.id.menuSecondWeekNumber:
                selectedWeekNumber = WeekNumberEnum.SECOND;
                showScheduleFragmentForGroup.filterScheduleList(selectedDayPosition, selectedWeekNumber, selectedSubGroup);
                weekNumberSubMenu.setTitle(getResources().getString(R.string.second_week_number));
                break;
            case R.id.menuThirdWeekNumber:
                selectedWeekNumber = WeekNumberEnum.THIRD;
                showScheduleFragmentForGroup.filterScheduleList(selectedDayPosition, selectedWeekNumber, selectedSubGroup);
                weekNumberSubMenu.setTitle(getResources().getString(R.string.third_week_number));
                break;
            case R.id.menuFourthWeekNumber:
                selectedWeekNumber = WeekNumberEnum.FOURTH;
                showScheduleFragmentForGroup.filterScheduleList(selectedDayPosition, selectedWeekNumber, selectedSubGroup);
                weekNumberSubMenu.setTitle(getResources().getString(R.string.fourth_week_number));
                break;
            case R.id.menuEntireGroup:
                selectedSubGroup = SubGroupEnum.ENTIRE_GROUP;
                showScheduleFragmentForGroup.filterScheduleList(selectedDayPosition, selectedWeekNumber, selectedSubGroup);
                subGroupSubMenu.setTitle(getResources().getString(R.string.entire_group));
                break;
            case R.id.menuFirstSubGroup:
                selectedSubGroup = SubGroupEnum.FIRST_SUB_GROUP;
                showScheduleFragmentForGroup.filterScheduleList(selectedDayPosition, selectedWeekNumber, selectedSubGroup);
                subGroupSubMenu.setTitle(getResources().getString(R.string.first_sub_group));
                break;
            case R.id.menuSecondSubGroup:
                selectedSubGroup = SubGroupEnum.SECOND_SUB_GROUP;
                showScheduleFragmentForGroup.filterScheduleList(selectedDayPosition, selectedWeekNumber, selectedSubGroup);
                subGroupSubMenu.setTitle(getResources().getString(R.string.second_sub_group));
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

}
