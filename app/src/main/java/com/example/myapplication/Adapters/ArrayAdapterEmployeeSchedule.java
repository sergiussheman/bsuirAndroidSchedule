package com.example.myapplication.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.Model.Schedule;

/**
 * Created by iChrome on 14.08.2015.
 */
public class ArrayAdapterEmployeeSchedule extends BaseArrayAdapterSchedule {

    /**
     * Адаптер для списка расписания преподавателя
     * @param context контекст
     * @param layoutResourceId id listView в котором отображаются занятия преподавателя
     * @param data данные которые необходимо отобразить в listView
     */
    public ArrayAdapterEmployeeSchedule(Context context, int layoutResourceId, Schedule[] data){
        super(context, layoutResourceId, data);
    }

    /**
     * Метод возвращает view для выбранного занятия из списка занятий преподавателя выбранного пользователем дня
     * @param position номер занятия для которого нужно создать view
     * @param passedConvertView View которое нужно заполнить дабнными выбранного занятия
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

        TextView scheduleTimeTextView = (TextView) convertView.findViewById(R.id.scheduleTimeTextView);
        scheduleTimeTextView.setText(currentSchedule.getLessonTime());

        View lessonTypeView = convertView.findViewById(R.id.lessonTypeView);
        updateLessonTypeView(lessonTypeView, currentSchedule.getLessonType());

        TextView subjectName = (TextView) convertView.findViewById(R.id.subjectNameListItem);
        String textForSubjectTextView = currentSchedule.getSubject();
        if(!currentSchedule.getLessonType().isEmpty()){
            textForSubjectTextView += " (" + currentSchedule.getLessonType() + ")";
        }
        subjectName.setText(textForSubjectTextView);
        TextView noteTextView = (TextView) convertView.findViewById(R.id.scheduleNoteTextView);
        noteTextView.setText(currentSchedule.getNote());
        TextView employeeName = (TextView) convertView.findViewById(R.id.employeeNameListItem);
        employeeName.setText(currentSchedule.getStudentGroup());

        if(currentSchedule.getWeekNumbers().size() != 4) {
            TextView weekNumber = (TextView) convertView.findViewById(R.id.weekNumberTextView);
            weekNumber.setText(convertListString(currentSchedule.getWeekNumbers(), " неделя"));
        }

        if(!currentSchedule.getSubGroup().isEmpty()) {
            TextView subGroup = (TextView) convertView.findViewById(R.id.subGroupTextView);
            subGroup.setText(currentSchedule.getSubGroup() + " подгр.");
        }

        TextView auditoryName = (TextView) convertView.findViewById(R.id.auditoryNameListItem);
        auditoryName.setText(convertListString(currentSchedule.getAuditories(), ""));
        return convertView;
    }
}
