package com.example.myapplication.Adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.myapplication.Model.Employee;
import com.example.myapplication.Model.Schedule;
import com.example.myapplication.R;
import com.example.myapplication.Utils.EmployeeUtil;

import java.util.List;

/**
 * Created by iChrome on 13.08.2015.
 */
public class ArrayAdapterGroupSchedule extends ArrayAdapter<Schedule> {

    Context context;
    int layoutID;
    Schedule[] data = null;

    public ArrayAdapterGroupSchedule(Context context, int layoutResourceId, Schedule[] data){
        super(context, layoutResourceId, data);
        this.context = context;
        this.layoutID = layoutResourceId;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
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
        String textForSubjectTextView = currentSchedule.getSubject();
        if(!currentSchedule.getLessonType().isEmpty()){
            textForSubjectTextView += " (" + currentSchedule.getLessonType() + ")";
        }
        subjectName.setText(textForSubjectTextView);
        TextView noteTextView = (TextView) convertView.findViewById(R.id.scheduleNoteTextView);
        noteTextView.setText(currentSchedule.getNote());
        TextView employeeName = (TextView) convertView.findViewById(R.id.employeeNameListItem);
        employeeName.setText(convertEmployeeListToString(currentSchedule.getEmployeeList()));

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

    public void updateLessonTypeView(View view, String lessonType){
        switch(lessonType){
            case "ПЗ":
                view.setBackgroundResource(R.color.yellow);
                break;
            case "УПз":
                view.setBackgroundResource(R.color.yellow);
                break;
            case "ЛК":
                view.setBackgroundResource(R.color.green);
                break;
            case "УЛк":
                view.setBackgroundResource(R.color.green);
                break;
            case "ЛР":
                view.setBackgroundResource(R.color.red);
                break;
            default:
                view.setBackgroundResource(R.color.blue);
                break;
        }
    }

    @NonNull
    private String convertEmployeeListToString(List<Employee> employeeList){
        StringBuilder result = new StringBuilder();
        for(Employee employee : employeeList){
            result.append(EmployeeUtil.getEmployeeFIO(employee));
            result.append(", ");
        }
        if(result.length() > 2) {
            result.delete(result.length() - 2, result.length());
        }
        return result.toString();
    }

    @NonNull
    private String convertListString(List<String> values, String addition){
        StringBuilder result = new StringBuilder();
        for(String value : values){
            result.append(value);
            result.append(", ");
        }
        if(result.length() > 2) {
            result.delete(result.length() - 2, result.length());
            result.append(addition);
        }
        return result.toString();
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }
}
