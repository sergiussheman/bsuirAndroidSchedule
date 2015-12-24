package com.example.myapplication;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.example.myapplication.adapters.ArrayAdapterEmployeeSchedule;
import com.example.myapplication.adapters.ArrayAdapterGroupSchedule;
import com.example.myapplication.model.Schedule;
import com.example.myapplication.model.SchoolDay;
import com.example.myapplication.utils.FileUtil;

import java.io.Serializable;
import java.util.List;


/**
 * Фрагмент для скачивания расписания экзаменов
 */
public class ExamScheduleFragment extends Fragment {
    private static final String TAG = "examScheduleTAG" ;
    private static final String ARG_ALL_SCHEDULE = "examAllSchedule";
    private static final String ARG_SELECTED_POSITION = "examSelectedPosition";
    private View currentView;
    private List<SchoolDay> allSchedules;
    private Schedule[] schedulesForShow;
    private Integer currentSelectedPosition;

    /**
     * Фрагмент для скачивания расписания экзаменов
     */
    public ExamScheduleFragment() {
        // Required empty public constructor
    }

    /**
     * Статический метод для создания экземпляра данного фрагмента.
     * Параметры сетятся в экземпляр фрагмента через метод "setArguments", это позволит фрагменту
     * восстановить параметры после пересоздания
     * @param allSchedules список всех экзаменов
     * @param position номер текущего дня
     * @return созданный фрагмент
     */
    public static ExamScheduleFragment newInstance(List<SchoolDay> allSchedules,int position) {
        ExamScheduleFragment fragment = new ExamScheduleFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ALL_SCHEDULE, (Serializable) allSchedules);
        args.putInt(ARG_SELECTED_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if(args != null){
            List<SchoolDay> allSchedules = (List<SchoolDay>) args.getSerializable(ARG_ALL_SCHEDULE);
            setAllSchedules(allSchedules);
            setCurrentSelectedPosition(args.getInt(ARG_SELECTED_POSITION));
        }
    }

    /**
     * Вызывается для того чтобы фрагмент создал свое представление
     * @param inflater Объект служащий для создания view
     * @param container Ссылка указывающая на родительское view
     * @param savedInstanceState Если фрагмент пересоздается, то здесь будут хранится сохраненные
     *                           значения
     * @return Возвращает созданное view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        currentView = inflater.inflate(R.layout.fragment_exam_schedule, container, false);
        updateSchedule(currentSelectedPosition);
        return currentView;
    }

    /**
     * Обновляет ListView со списком занятий
     */
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

    /**
     * Метод вызывается после выбора пользователем дня для показа расписания. В методе достается
     * список занятий для введенного дня, и обновление ListView с отображаемым списком занятий
     * @param position номер дня, для которого нужно показать расписание
     */
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
            Log.e(TAG, e.toString(), e);
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
