package com.example.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.myapplication.Adapters.ScheduleExamViewPagerAdapter;
import com.example.myapplication.Model.SchoolDay;

import java.util.List;

public class ScheduleExamViewPagerFragment extends Fragment {
    private ViewPager scheduleViewPager;
    private View currentView;
    private ScheduleExamViewPagerAdapter adapter;
    private List<SchoolDay> allSchedules;
    private OnFragmentInteractionListener mListener;
    private static final int PAGE_LEFT = 0;
    private static final int PAGE_MIDDLE = 1;
    private static final int PAGE_RIGHT = 2;
    private Integer currentMiddleIndex;
    private Integer currentSelectedIndex;


    public static ScheduleViewPagerFragment newInstance(String param1, String param2) {
        ScheduleViewPagerFragment fragment = new ScheduleViewPagerFragment();
        return fragment;
    }

    public ScheduleExamViewPagerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        currentView = inflater.inflate(R.layout.fragment_schedule_exam_view_pager, container, false);
        scheduleViewPager = (ViewPager) currentView.findViewById(R.id.scheduleExamViewPager);
        scheduleViewPager.setOffscreenPageLimit(2);
        scheduleViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentSelectedIndex = position;
                if (currentSelectedIndex == PAGE_LEFT) {
                    if(currentMiddleIndex == 0){
                        currentMiddleIndex = allSchedules.size() - 1;
                    } else {
                        currentMiddleIndex--;
                    }
                    // user swiped to right direction
                } else if (currentSelectedIndex == PAGE_RIGHT) {

                    if(currentMiddleIndex == allSchedules.size() - 1){
                        currentMiddleIndex = 0;
                    } else{
                        currentMiddleIndex++;
                    }

                }
                mListener.onChangeExamDay(currentMiddleIndex);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE && currentSelectedIndex != null) {
                    updateFiltersForViewPager(currentMiddleIndex);
                }
            }
        });

        updateFiltersForViewPager(getCurrentSelectedIndex());
        return currentView;
    }

    public Void updateFiltersForViewPager(Integer dayPosition) {
        adapter = new ScheduleExamViewPagerAdapter(getActivity().getSupportFragmentManager());
        adapter.setAllSchedules(getAllSchedules());
        currentMiddleIndex = dayPosition;
        adapter.setSelectedDayPosition(dayPosition);
        scheduleViewPager.setAdapter(adapter);
        scheduleViewPager.setCurrentItem(PAGE_MIDDLE);

        TextView noLessonsView = (TextView) currentView.findViewById(R.id.examViewPagerNoLessons);
        if(getAllSchedules().isEmpty()){
            noLessonsView.setVisibility(View.VISIBLE);
        } else{
            noLessonsView.setVisibility(View.INVISIBLE);
        }
        return null;
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

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public List<SchoolDay> getAllSchedules() {
        return allSchedules;
    }

    public void setAllSchedules(List<SchoolDay> allSchedules) {
        this.allSchedules = allSchedules;
    }

    public Integer getCurrentMiddleIndex() {
        return currentMiddleIndex;
    }

    public void setCurrentMiddleIndex(Integer currentMiddleIndex) {
        this.currentMiddleIndex = currentMiddleIndex;
    }

    public Integer getCurrentSelectedIndex() {
        return currentSelectedIndex;
    }

    public void setCurrentSelectedIndex(Integer currentSelectedIndex) {
        this.currentSelectedIndex = currentSelectedIndex;
    }
}
