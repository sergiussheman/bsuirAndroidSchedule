package com.example.myapplication;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.example.myapplication.Adapters.ArrayAdapterEmployeeSchedule;
import com.example.myapplication.Adapters.ArrayAdapterGroupSchedule;
import com.example.myapplication.Model.Schedule;
import com.example.myapplication.Model.SchoolDay;
import com.example.myapplication.Model.SubGroupEnum;
import com.example.myapplication.Model.WeekNumberEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class ScheduleFragmentForGroup extends Fragment {
    private final static String TAG = "scheduleShowFragTAG";
    private Schedule[] schedulesForShow;
    private List<SchoolDay> allScheduleForGroup;
    private View currentView;

    private OnFragmentInteractionListener mListener;

    public static ScheduleFragmentForGroup newInstance(String param1, String param2) {
        ScheduleFragmentForGroup fragment = new ScheduleFragmentForGroup();
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ScheduleFragmentForGroup() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        currentView = inflater.inflate(R.layout.show_schedule_fragment_layout, container, false);

        return currentView;
    }

    public void updateSchedule(int position){
        try {
            List<Schedule> scheduleList = new ArrayList<>();
            if(getAllScheduleForGroup().size() > position) {
                scheduleList = getAllScheduleForGroup().get(position).getSchedules();
            }
            Schedule[] schedules = scheduleList.toArray(new Schedule[scheduleList.size()]);
            setSchedulesForShow(schedules);
            updateListView();
        } catch (Exception e){
            Log.e(TAG, e.toString());
        }
    }

    public void updateListView(){
        ListView mainListView = (ListView) currentView.findViewById(R.id.showScheduleListView);
        if(isDefaultStudentGroup()) {
            mainListView.setAdapter(new ArrayAdapterGroupSchedule(getActivity(), R.layout.schedule_fragment_item_layout, schedulesForShow));
        } else{
            mainListView.setAdapter(new ArrayAdapterEmployeeSchedule(getActivity(), R.layout.schedule_fragment_item_layout, schedulesForShow));
        }
        TextView emptyTextView = (TextView) currentView.findViewById(R.id.emptyResults);
        mainListView.setEmptyView(emptyTextView);
    }

    public boolean isDefaultStudentGroup(){
        String settingFileName = getString(R.string.setting_file_name);
        SharedPreferences preferences = getActivity().getSharedPreferences(settingFileName, 0);
        String defaultGroup = preferences.getString(getActivity().getResources().getString(R.string.default_group_field_in_settings), "none");
        if("none".equals(defaultGroup)){
            return false;
        } else{
            return true;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public void filterScheduleList(Integer dayPosition, WeekNumberEnum weekNumber, SubGroupEnum subGroupEnum){
        List<Schedule> result = new ArrayList<>();
        SchoolDay selectedSchoolDay = getAllScheduleForGroup().get(dayPosition);
        for(Schedule schedule : selectedSchoolDay.getSchedules()){
            boolean matchWeekNumber = false;
            boolean matchSubGroup = false;
            if(weekNumber.getOrder().equals(WeekNumberEnum.ALL.getOrder())){
                matchWeekNumber = true;
            } else{
                for (String weekNumberFromSchedule : schedule.getWeekNumbers()) {
                    if (weekNumberFromSchedule.equalsIgnoreCase(weekNumber.getOrder().toString())) {
                        matchWeekNumber = true;
                        break;
                    }
                }
            }

            if(subGroupEnum.getOrder().equals(SubGroupEnum.ENTIRE_GROUP.getOrder())){
                matchSubGroup = true;
            } else if(schedule.getSubGroup().isEmpty()){
                matchSubGroup = true;
            } else{
                if(schedule.getSubGroup().equalsIgnoreCase(subGroupEnum.getOrder().toString())){
                    matchSubGroup = true;
                }
            }

            if(matchSubGroup && matchWeekNumber){
                result.add(schedule);
            }
        }
        schedulesForShow = result.toArray(new Schedule[result.size()]);
        updateListView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public Schedule[] getSchedulesForShow() {
        return schedulesForShow;
    }

    public void setSchedulesForShow(Schedule[] schedulesForShow) {
        this.schedulesForShow = schedulesForShow;
    }

    public List<SchoolDay> getAllScheduleForGroup() {
        return allScheduleForGroup;
    }

    public void setAllScheduleForGroup(List<SchoolDay> allScheduleForGroup) {
        this.allScheduleForGroup = allScheduleForGroup;
    }
}
