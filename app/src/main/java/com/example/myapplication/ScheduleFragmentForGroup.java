package com.example.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.example.myapplication.Utils.DateUtil;
import com.example.myapplication.Utils.FileUtil;

import java.util.ArrayList;
import java.util.Calendar;
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
    private String[] weekDays = new String[]{"Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье"};
    private Context context;
    private Integer currentPosition;
    private WeekNumberEnum selectedWeekNumber;
    private SubGroupEnum selectedSubGroup;

    public static ScheduleFragmentForGroup newInstance(List<SchoolDay> allSchedules,int position, WeekNumberEnum weekNumber, SubGroupEnum subGroup) {
        ScheduleFragmentForGroup fragment = new ScheduleFragmentForGroup();
        fragment.setAllScheduleForGroup(allSchedules);
        fragment.selectedWeekNumber = weekNumber;
        fragment.selectedSubGroup = subGroup;
        fragment.currentPosition = position;
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
        if(currentPosition != null) {
            filterScheduleList(currentPosition, selectedWeekNumber, selectedSubGroup);
        }
        return currentView;
    }

    public void updateListView(){
        if(currentView != null && getActivity() != null) {
            Integer currentWeekNumber = DateUtil.getWeek(Calendar.getInstance().getTime());
            TextView currentWeekTextView = (TextView) currentView.findViewById(R.id.currentWeekNumber);
            currentWeekTextView.setText("Сейчас " + currentWeekNumber + "-я уч. неделя");

            ListView mainListView = (ListView) currentView.findViewById(R.id.showScheduleListView);
            if (FileUtil.isDefaultStudentGroup(getActivity())) {
                mainListView.setAdapter(new ArrayAdapterGroupSchedule(getActivity(), R.layout.schedule_fragment_item_layout, schedulesForShow));
            } else {
                mainListView.setAdapter(new ArrayAdapterEmployeeSchedule(getActivity(), R.layout.schedule_fragment_item_layout, schedulesForShow));
            }
            TextView emptyTextView = (TextView) currentView.findViewById(R.id.emptyResults);
            mainListView.setEmptyView(emptyTextView);
        }
    }

    public void filterScheduleList(Integer dayPosition, WeekNumberEnum weekNumber, SubGroupEnum subGroupEnum){
        List<Schedule> result = new ArrayList<>();
        List<Schedule> selectedSchoolDay = getListSchedules(dayPosition);
        for (Schedule schedule : selectedSchoolDay) {
            boolean matchWeekNumber = false;
            boolean matchSubGroup = false;
            if (weekNumber.getOrder().equals(WeekNumberEnum.ALL.getOrder())) {
                matchWeekNumber = true;
            } else {
                for (String weekNumberFromSchedule : schedule.getWeekNumbers()) {
                    if (weekNumberFromSchedule.equalsIgnoreCase(weekNumber.getOrder().toString())) {
                        matchWeekNumber = true;
                        break;
                    }
                }
            }

            if (subGroupEnum.getOrder().equals(SubGroupEnum.ENTIRE_GROUP.getOrder())) {
                matchSubGroup = true;
            } else if (schedule.getSubGroup().isEmpty()) {
                matchSubGroup = true;
            } else {
                if (schedule.getSubGroup().equalsIgnoreCase(subGroupEnum.getOrder().toString())) {
                    matchSubGroup = true;
                }
            }

            if (matchSubGroup && matchWeekNumber) {
                result.add(schedule);
            }
        }
        schedulesForShow = result.toArray(new Schedule[result.size()]);
        updateListView();
    }

    public List<Schedule> getListSchedules(int position){
        if(position >= 0 && position < weekDays.length) {
            String dayAsString = weekDays[position];
            for (SchoolDay schoolDay : getAllScheduleForGroup()) {
                if (schoolDay.getDayName().equalsIgnoreCase(dayAsString)) {
                    return schoolDay.getSchedules();
                }
            }
        }
        return new ArrayList<>();
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

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
