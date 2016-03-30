package com.example.myapplication.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

import com.example.myapplication.R;
import com.example.myapplication.model.Employee;
import com.example.myapplication.model.Schedule;
import com.example.myapplication.utils.EmployeeUtil;

import java.util.List;

/**
 * Created by iChrome on 13.08.2015.
 */
public class ArrayAdapterGroupSchedule extends BaseArrayAdapterSchedule {

    View.OnCreateContextMenuListener menuListener;
    Integer id = 0;
    public int pos;
    /**
     * Адаптер для отображения расписания занятий группы
     * @param context контекст
     * @param layoutResourceId id ListView в котором отображается список занятий группы
     * @param data список занятий которые нужно отобразить в listView
     */
    public ArrayAdapterGroupSchedule(Context context, int layoutResourceId, Schedule[] data){
        super(context, layoutResourceId, data);
    }

    public Integer getPosition() {
        return pos;
    }

    public Schedule getSchedule(int position) {
        return data[position];
    }

    /**
     * Метод возвращает view для выбранного занятия из списка занятий группы для выбранного дня
     * @param position номер занятия для которого нужно создать view
     * @param passedConvertView View которое нужно заполнить дабнными выбранного занятия группы
     * @param parent родительское View
     * @return возвращает результирующее view
     */
    @Override
    public View getView(int position, View passedConvertView, ViewGroup parent){
        View convertView = passedConvertView;
        if(convertView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(layoutID, parent, false);
        }
        Schedule currentSchedule = data[position];

        View lessonTypeView = convertView.findViewById(R.id.lessonTypeView);
        updateLessonTypeView(lessonTypeView, currentSchedule.getLessonType());

        TextView scheduleTimeTextView = (TextView) convertView.findViewById(R.id.scheduleTimeTextView);
        scheduleTimeTextView.setText(currentSchedule.getLessonTime());

        TextView subjectName = (TextView) convertView.findViewById(R.id.subjectNameListItem);
        subjectName.setMaxWidth(200);
        String textForSubjectTextView = currentSchedule.getSubject();
        if(!currentSchedule.getLessonType().isEmpty()){
            textForSubjectTextView += " (" + currentSchedule.getLessonType() + ")";
        }
        subjectName.setText(textForSubjectTextView);

        TextView noteTextView = (TextView) convertView.findViewById(R.id.scheduleNoteTextView);
        noteTextView.setText(currentSchedule.getNote());


        TextView employeeName = (TextView) convertView.findViewById(R.id.employeeNameListItem);
        employeeName.append(convertEmployeeListToString(currentSchedule.getEmployeeList()));

        if(!currentSchedule.getSubGroup().isEmpty()) {
            TextView subGroup = (TextView) convertView.findViewById(R.id.subGroupTextView);
            subGroup.setText(currentSchedule.getSubGroup() + " подгр.");
        }
        if(currentSchedule.getWeekNumbers().size() != 4) {
            TextView weekNumber = (TextView) convertView.findViewById(R.id.weekNumberTextView);
            weekNumber.setText(convertListString(currentSchedule.getWeekNumbers(), " неделя"));
        }

        TextView auditoryName = (TextView) convertView.findViewById(R.id.auditoryNameListItem);
        auditoryName.setText(convertListString(currentSchedule.getAuditories(), ""));

        return convertView;
    }

    /**
     * Метод конвертит список преподавателей в одну строку
     * @param employeeList список преподавателей
     * @return Возвращает строку состоящую из всех переданных преподавателей
     */
    @NonNull
    private String convertEmployeeListToString(List<Employee> employeeList){
        StringBuilder builder = new StringBuilder();
        for(Employee employee : employeeList){
            builder.append(EmployeeUtil.getEmployeeFIO(employee));
            builder.append(", ");
        }
        if(builder.length() > 2) {
            builder.delete(builder.length() - 2, builder.length());
        }
        return builder.toString();
    }
}
