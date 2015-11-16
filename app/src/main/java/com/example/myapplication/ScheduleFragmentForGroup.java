package com.example.myapplication;

import android.content.Context;
import android.os.Bundle;
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
    private Schedule[] schedulesForShow;
    private List<SchoolDay> allScheduleForGroup;
    private View currentView;
    private String[] weekDays = new String[]{"Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье"};
    private Context context;
    private Integer currentPosition;
    private WeekNumberEnum selectedWeekNumber;
    private SubGroupEnum selectedSubGroup;

    /**
     * Фрагмент для отображения списка занятий для студенченской группы
     */
    public ScheduleFragmentForGroup() {
         // Mandatory empty constructor for the fragment manager to instantiate the
         // fragment (e.g. upon screen orientation changes).
    }

    /**
     * Статический метод для создания экземпляра фрагмента с заданными параметрами
     * @param allSchedules лист всех занятий на неделю
     * @param position текущий выбранный день недели
     * @param weekNumber текущая выбранная учебная неделя
     * @param subGroup текущая выбранная подгруппа
     * @return возвращает созданный фрагмент
     */
    public static ScheduleFragmentForGroup newInstance(List<SchoolDay> allSchedules,int position, WeekNumberEnum weekNumber, SubGroupEnum subGroup) {
        ScheduleFragmentForGroup fragment = new ScheduleFragmentForGroup();
        fragment.setAllScheduleForGroup(allSchedules);
        fragment.selectedWeekNumber = weekNumber;
        fragment.selectedSubGroup = subGroup;
        fragment.currentPosition = position;
        return fragment;
    }

    /**
     * Метод для создания view которую будет отбражаться пользователю
     * @param inflater Объект служащий создания view
     * @param container Родительское view
     * @param savedInstanceState Сохраненное состояние фрагмента
     * @return Возвращает view для отображения пользователю
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        currentView = inflater.inflate(R.layout.show_schedule_fragment_layout, container, false);
        if(currentPosition != null) {
            filterScheduleList(currentPosition, selectedWeekNumber, selectedSubGroup);
        }
        return currentView;
    }

    /**
     * Обновляет listView со списком занятий на выбранный день недели
     */
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

    /**
     * Обновляет лист со списком занятий на основе выбранных пользователем фильтров
     * @param dayPosition номере дня недели
     * @param weekNumber номер учебной недели
     * @param subGroupEnum номер подгруппы
     */
    public void filterScheduleList(Integer dayPosition, WeekNumberEnum weekNumber, SubGroupEnum subGroupEnum){
        List<Schedule> result = new ArrayList<>();
        List<Schedule> selectedSchoolDay = getListSchedules(dayPosition);
        for (Schedule schedule : selectedSchoolDay) {
            boolean matchWeekNumber = isMatchWeekNumber(weekNumber, schedule);
            boolean matchSubGroup = isMatchSubGroup(subGroupEnum, schedule);

            if (matchSubGroup && matchWeekNumber) {
                result.add(schedule);
            }
        }
        schedulesForShow = result.toArray(new Schedule[result.size()]);
        updateListView();
    }

    /**
     * Фильтруем занятие на основе введенной пользователем подгруппы
     * @param subGroupEnum подгруппа введенная пользователем
     * @param schedule занятие которое нужно проверить
     * @return Возвращает false, если занятие не для выбранной подгруппы, иначе возвращает true
     */
    private static boolean isMatchSubGroup(SubGroupEnum subGroupEnum, Schedule schedule){
        boolean result = false;
        if (subGroupEnum.getOrder().equals(SubGroupEnum.ENTIRE_GROUP.getOrder())) {
            result = true;
        } else if (schedule.getSubGroup().isEmpty()) {
            result = true;
        } else {
            if (schedule.getSubGroup().equalsIgnoreCase(subGroupEnum.getOrder().toString())) {
                result = true;
            }
        }
        return result;
    }

    /**
     * Проверяет занятие на соответствие введенной пользователем учебной недели
     * @param weekNumber Номер учебной недели введенной пользователем
     * @param schedule Занятие для проверки
     * @return Возвращает false, если занятие не проводится на выбранной неделе, иначе возвращает true
     */
    private static boolean isMatchWeekNumber(WeekNumberEnum weekNumber, Schedule schedule){
        boolean result = false;
        if (weekNumber.getOrder().equals(WeekNumberEnum.ALL.getOrder())) {
            result = true;
        } else {
            for (String weekNumberFromSchedule : schedule.getWeekNumbers()) {
                if (weekNumberFromSchedule.equalsIgnoreCase(weekNumber.getOrder().toString())) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Возвращает список занятий для определенного дня недели
     * @param position номер дня недели
     * @return Возвращает список занятий в переданный день недели
     */
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
