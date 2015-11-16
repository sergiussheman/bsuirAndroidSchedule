package com.example.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.myapplication.Adapters.ScheduleExamViewPagerAdapter;
import com.example.myapplication.Model.SchoolDay;

import java.util.List;

/**
 * ViewPager для отображения расписания экзаменов
 */
public class ScheduleExamViewPagerFragment extends Fragment {
    private static final String TAG = "schedExamViewPager";
    private ViewPager scheduleViewPager;
    private View currentView;
    private List<SchoolDay> allSchedules;
    private OnFragmentInteractionListener activity;
    private static final int PAGE_LEFT = 0;
    private static final int PAGE_MIDDLE = 1;
    private static final int PAGE_RIGHT = 2;
    private Integer currentMiddleIndex;
    private Integer currentSelectedIndex;

    /**
     * ViewPager для отображения расписания экзаменов
     */
    public ScheduleExamViewPagerFragment() {
        // Required empty public constructor
    }

    /**
     * Статический метод для создания экземпляра фрагмента
     * @return возвращает созданный фрагмент
     */
    public static ScheduleViewPagerFragment newInstance() {
        return new ScheduleViewPagerFragment();
    }

    /**
     * В этом методе фрагмент создает свое view, которое будет отображаться пользователю
     * @param inflater Объект служащий для создания view
     * @param container Родительский view
     * @param savedInstanceState Сохраненное состояние фрагмента
     * @return Возвращает view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        currentView = inflater.inflate(R.layout.fragment_schedule_exam_view_pager, container, false);
        scheduleViewPager = (ViewPager) currentView.findViewById(R.id.scheduleExamViewPager);
        scheduleViewPager.setOffscreenPageLimit(2);
        scheduleViewPager.addOnPageChangeListener(new ViewPagerChangeListener());

        updateFiltersForViewPager(getCurrentSelectedIndex());
        return currentView;
    }

    /**
     * В методе обновляются параметры ViewPager для расписания экзаменов
     * @param dayPosition День, занятие которого должны отображаться на текущем экране
     * @return null
     */
    public Void updateFiltersForViewPager(Integer dayPosition) {
        ScheduleExamViewPagerAdapter adapter = new ScheduleExamViewPagerAdapter(getActivity().getSupportFragmentManager());
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

    /**
     * Метод вызывается при присоединении фрагмента к активити
     * @param activity активити к которой присоединяется фрагмент
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.activity = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            Log.v(TAG, e.getMessage(), e);
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    /**
     * Метод вызывается при отсоединении фрагмента от активити
     */
    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
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


    /**
     * Обрабтчик событий для ViewPager
     */
    private class ViewPagerChangeListener implements ViewPager.OnPageChangeListener {

        /**
         * Метод вызывается когда текущая страница скролится
         * @param position позиция текущей отображаемой страницы
         * @param positionOffset значение от [0, 1) отображающее смещение
         * @param positionOffsetPixels смещение в пикселях
         */
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            //nothing to do
        }

        /**
         * Вызывается когда пользователь делает свап влево или вправо
         * @param position Позиция новой страницы, которую выбрал пользователь
         */
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
            activity.onChangeExamDay(currentMiddleIndex);
        }

        /**
         * Вызывается при изменении состояния viewPager
         * @param state Новое состояние
         */
        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_IDLE && currentSelectedIndex != null) {
                updateFiltersForViewPager(currentMiddleIndex);
            }
        }
    }
}
