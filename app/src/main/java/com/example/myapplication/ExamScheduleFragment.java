package com.example.myapplication;

import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.example.myapplication.Utils.FileUtil;

import java.util.List;


public class ExamScheduleFragment extends Fragment {
    private static final String TAG = "examScheduleTAG" ;
    private View currentView;
    private List<SchoolDay> allSchedules;
    private Schedule[] schedulesForShow;
    private Integer currentSelectedPosition;

    public static ExamScheduleFragment newInstance(List<SchoolDay> allSchedules,int position) {
        ExamScheduleFragment fragment = new ExamScheduleFragment();
        fragment.setAllSchedules(allSchedules);
        fragment.setCurrentSelectedPosition(position);
        return fragment;
    }

    public ExamScheduleFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        currentView = inflater.inflate(R.layout.fragment_exam_schedule, container, false);
        updateSchedule(currentSelectedPosition);
        return currentView;
    }

    public void updateListView(){
        if(currentView != null && getActivity() != null) {
            ListView mainListView = (ListView) currentView.findViewById(R.id.showExamScheduleView);
            if (FileUtil.isDefaultStudentGroup(getActivity())) {
                mainListView.setAdapter(new ArrayAdapterGroupSchedule(getActivity(), R.layout.schedule_fragment_item_layout, schedulesForShow));
            } else {
                mainListView.setAdapter(new ArrayAdapterEmployeeSchedule(getActivity(), R.layout.schedule_fragment_item_layout, schedulesForShow));
            }
            TextView emptyTextView = (TextView) currentView.findViewById(R.id.emptyExamList);
            mainListView.setEmptyView(emptyTextView);
        }
    }

    public void updateSchedule(int position){
        try {
            List<Schedule> scheduleList;
            if(getAllSchedules().size() > position) {
                scheduleList = getAllSchedules().get(position).getSchedules();
            } else{
                scheduleList = getAllSchedules().get(0).getSchedules();
            }
            Schedule[] schedules = scheduleList.toArray(new Schedule[scheduleList.size()]);
            setSchedulesForShow(schedules);
            updateListView();
        } catch (Exception e){
            Log.e(TAG, e.toString());
        }
    }

    public List<SchoolDay> getAllSchedules() {
        return allSchedules;
    }

    public void setAllSchedules(List<SchoolDay> allSchedules) {
        this.allSchedules = allSchedules;
    }

    public Schedule[] getSchedulesForShow() {
        return schedulesForShow;
    }

    public void setSchedulesForShow(Schedule[] schedulesForShow) {
        this.schedulesForShow = schedulesForShow;
    }

    public Integer getCurrentSelectedPosition() {
        return currentSelectedPosition;
    }

    public void setCurrentSelectedPosition(Integer currentSelectedPosition) {
        this.currentSelectedPosition = currentSelectedPosition;
    }
}
