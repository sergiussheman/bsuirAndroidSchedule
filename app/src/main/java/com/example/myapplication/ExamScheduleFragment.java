package com.example.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.myapplication.adapters.ArrayAdapterEmployeeSchedule;
import com.example.myapplication.adapters.ArrayAdapterGroupSchedule;
import com.example.myapplication.adapters.ScheduleExamViewPagerAdapter;
import com.example.myapplication.dao.DBHelper;
import com.example.myapplication.dao.SchoolDayDao;
import com.example.myapplication.model.Employee;
import com.example.myapplication.model.Schedule;
import com.example.myapplication.model.SchoolDay;
import com.example.myapplication.utils.FileUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Фрагмент для скачивания расписания экзаменов
 */
public class ExamScheduleFragment extends Fragment {
    private static final String TAG = "examScheduleTAG" ;
    private static final String ARG_ALL_SCHEDULE = "examAllSchedule";
    private static final String ARG_SELECTED_POSITION = "examSelectedPosition";
    private static final String ARG_SHOW_HIDDEN_SCHEDULE = "showHidden";
    private View currentView;
    private List<SchoolDay> allSchedules;
    private Schedule[] schedulesForShow;
    private Integer currentSelectedPosition;
    private boolean showHidden;

    public static boolean isRefreshed = true;

    private ArrayAdapterGroupSchedule groupAdapter;
    private ArrayAdapterEmployeeSchedule empAdapter;

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
    public static ExamScheduleFragment newInstance(List<SchoolDay> allSchedules, int position, boolean showHidden) {
        ExamScheduleFragment fragment = new ExamScheduleFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ALL_SCHEDULE, (Serializable) allSchedules);
        args.putInt(ARG_SELECTED_POSITION, position);
        args.putBoolean(ARG_SHOW_HIDDEN_SCHEDULE, showHidden);
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
            setShowHidden(args.getBoolean(ARG_SHOW_HIDDEN_SCHEDULE));
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
        updateSchedule(currentSelectedPosition, showHidden);
        return currentView;
    }

    /**
     * Обновляет ListView со списком занятий
     */
    public void updateListView(){
        if(currentView != null && getActivity() != null) {
            ListView mainListView = (ListView) currentView.findViewById(R.id.showExamScheduleView);
            groupAdapter = new ArrayAdapterGroupSchedule(getActivity(),
                    R.layout.schedule_fragment_item_layout, schedulesForShow);
            empAdapter = new ArrayAdapterEmployeeSchedule(getActivity(),
                    R.layout.schedule_fragment_item_layout, schedulesForShow);

            if (FileUtil.isDefaultStudentGroup(getActivity())) {
                mainListView.setAdapter(groupAdapter);
                mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Schedule s = groupAdapter.getSchedule(position);
                        Log.d("List View", s.getLessonTime() + " " + s.getLessonType() + " " + s.getSubGroup()
                                + " " + s.getSubject() + " " + s.getSubGroup() + " " + s.getStudentGroup());
                        createChooseActionDialog(s, false);
                    }
                });
                final TextView tvAddSchedule = (TextView) currentView.findViewById(R.id.addNewExamSchedule);
                final Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.onclick_anim);
                tvAddSchedule.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tvAddSchedule.startAnimation(anim);
                        Schedule s = allSchedules.get(currentSelectedPosition).getSchedules().get(0);
                        createAddDialog(s, false);
                    }
                });
            } else {
                mainListView.setAdapter(empAdapter);
                mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Schedule s = empAdapter.getSchedule(position);
                        Log.d("List View", s.getLessonTime() + " " + s.getLessonType() + " " + s.getSubGroup()
                                + " " + s.getSubject() + " " + s.getSubGroup() + " " + s.getStudentGroup());
                        createChooseActionDialog(s, true);
                    }
                });
                final TextView tvAddSchedule = (TextView) currentView.findViewById(R.id.addNewExamSchedule);
                final Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.onclick_anim);
                tvAddSchedule.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tvAddSchedule.startAnimation(anim);
                        Schedule s = empAdapter.getSchedule(0);
                        createAddDialog(s, true);
                    }
                });
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
    public void updateSchedule(int position, boolean showHidden){
        try {
            List<Schedule> scheduleList = new ArrayList<>();
            if(getAllSchedules().size() > position) {
                for (Schedule schedule: getAllSchedules().get(position).getSchedules()) {
                    if (showHidden) {
                        scheduleList.add(schedule);
                    } else {
                        if (!schedule.isHidden()) {
                            scheduleList.add(schedule);
                        }
                    }
                }
            } else{
                for (Schedule schedule: getAllSchedules().get(0).getSchedules()) {
                    if (showHidden) {
                        scheduleList.add(schedule);
                    } else {
                        if (!schedule.isHidden()) {
                            scheduleList.add(schedule);
                        }
                    }
                }
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

    public ExamScheduleFragment getThisFragment() {
        return this;
    }

    public Schedule[] getSchedulesForShow() {
        return schedulesForShow;
    }

    public void setShowHidden(boolean show) {
        this.showHidden = show;
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

    //create view for action dialog
    private View getEmptyView(Schedule scheduleForFill, boolean isForGroup) {
        LayoutInflater ltInflater = getActivity().getLayoutInflater();
        View view = ltInflater.inflate(R.layout.edit_exam_dialog, null, false);
        ViewGroup.LayoutParams lp = view.getLayoutParams();

        EditText etAud, etSubj, etEmpLastName, etEmpFirstName,
                etEmpMiddleName, etNote, etGroup, etTime, etLType;

        etGroup = (EditText) view.findViewById(R.id.etSetExamGroup);
        etGroup.setText(scheduleForFill.getStudentGroup());
        if (!isForGroup) {
            etGroup.setEnabled(false);
        }

        List<Employee> emps = new ArrayList<Employee>();
        emps = scheduleForFill.getEmployeeList();
        etEmpLastName = (EditText) view.findViewById(R.id.etSetExamEmpLastName);
        etEmpFirstName = (EditText) view.findViewById(R.id.etSetExamEmpFirstName);
        etEmpMiddleName = (EditText) view.findViewById(R.id.etSetExamEmpMiddleName);
        String lastNameBuf = "", firstNameBuf = "", middleNameBuf = "";
        for (Employee e: emps) {
            lastNameBuf += e.getLastName();
            lastNameBuf += ",";

            firstNameBuf += e.getFirstName();
            firstNameBuf += ",";

            middleNameBuf += e.getMiddleName();
            middleNameBuf += ",";
        }
        if (lastNameBuf.length() > 0) {
            lastNameBuf = lastNameBuf.substring(0, lastNameBuf.length() - 1);
            firstNameBuf = firstNameBuf.substring(0, firstNameBuf.length() - 1);
            middleNameBuf = middleNameBuf.substring(0, middleNameBuf.length() - 1);
        }


        if (isForGroup) {
            etEmpFirstName.setText(firstNameBuf);
            etEmpLastName.setText(lastNameBuf);
            etEmpMiddleName.setText(middleNameBuf);
            etEmpFirstName.setEnabled(false);
            etEmpLastName.setEnabled(false);
            etEmpMiddleName.setEnabled(false);
        }

        return view;
    }

    //create view for action dialog
    private View getEditView(Schedule scheduleForFill, boolean isForGroup) {
        LayoutInflater ltInflater = getActivity().getLayoutInflater();
        View view = ltInflater.inflate(R.layout.edit_exam_dialog, null, false);
        ViewGroup.LayoutParams lp = view.getLayoutParams();

        EditText etAud, etSubj, etEmpLastName, etEmpFirstName, etDate,
                etEmpMiddleName, etNote, etGroup, etTime, etLType;

        etDate = (EditText) view.findViewById(R.id.etSetExamDate);
        etDate.setText(scheduleForFill.getDate());
        etDate.setEnabled(false);

        etAud = (EditText) view.findViewById(R.id.etSetExamAud);
        String auds[] = scheduleForFill.getAuditories().toArray(new String[scheduleForFill.getAuditories().size()]);
        String buf = "";
        for (String str: auds) {
            buf += str;
        }
        etAud.setText(buf);

        etSubj = (EditText) view.findViewById(R.id.etSetExamSubj);
        etSubj.setText(scheduleForFill.getSubject());

        etGroup = (EditText) view.findViewById(R.id.etSetExamGroup);
        etGroup.setText(scheduleForFill.getStudentGroup());
        if (!isForGroup) {
            etGroup.setEnabled(false);
        }

        List<Employee> emps = new ArrayList<Employee>();
        emps = scheduleForFill.getEmployeeList();
        etEmpLastName = (EditText) view.findViewById(R.id.etSetExamEmpLastName);
        etEmpFirstName = (EditText) view.findViewById(R.id.etSetExamEmpFirstName);
        etEmpMiddleName = (EditText) view.findViewById(R.id.etSetExamEmpMiddleName);
        String lastNameBuf = "", firstNameBuf = "", middleNameBuf = "";
        for (Employee e: emps) {
            lastNameBuf += e.getLastName();
            lastNameBuf += ",";

            firstNameBuf += e.getFirstName();
            firstNameBuf += ",";

            middleNameBuf += e.getMiddleName();
            middleNameBuf += ",";
        }
        if (lastNameBuf.length() > 0) {
            lastNameBuf = lastNameBuf.substring(0, lastNameBuf.length() - 1);
            firstNameBuf = firstNameBuf.substring(0, firstNameBuf.length() - 1);
            middleNameBuf = middleNameBuf.substring(0, middleNameBuf.length() - 1);
        }

        etEmpFirstName.setText(firstNameBuf);
        etEmpLastName.setText(lastNameBuf);
        etEmpMiddleName.setText(middleNameBuf);

        if (isForGroup) {
            etEmpFirstName.setEnabled(false);
            etEmpLastName.setEnabled(false);
            etEmpMiddleName.setEnabled(false);
        }

        etNote = (EditText) view.findViewById(R.id.etSetExamNote);
        etNote.setText(scheduleForFill.getNote());

        etTime = (EditText) view.findViewById(R.id.etSetExamTime);
        etTime.setText(scheduleForFill.getLessonTime());


        etLType = (EditText) view.findViewById(R.id.etSetExamLessonType);
        etLType.setText(scheduleForFill.getLessonType());

        return view;
    }

    //get schedule params from edit dialog
    private Schedule fillScheduleFromEditDialog(View view) {
        EditText etAud, etSubj, etEmpLastName, etEmpFirstName,
                etEmpMiddleName, etNote, etTime, etLType, etDate;

        Schedule newSchedule = new Schedule();

        etDate = (EditText) view.findViewById(R.id.etSetExamDate);
        newSchedule.setDate(etDate.getText().toString());

        etAud = (EditText) view.findViewById(R.id.etSetExamAud);
        newSchedule.setAuditories(Arrays.asList(etAud.getText().toString().split("\\,")));

        etSubj = (EditText) view.findViewById(R.id.etSetExamSubj);
        newSchedule.setSubject(etSubj.getText().toString());

        List<Employee> emps = new ArrayList<Employee>();

        etEmpLastName = (EditText) view.findViewById(R.id.etSetExamEmpLastName);
        etEmpFirstName = (EditText) view.findViewById(R.id.etSetExamEmpFirstName);
        etEmpMiddleName = (EditText) view.findViewById(R.id.etSetExamEmpMiddleName);

        String[] firstNameBuf, lastNameBuf, middleNameBuf;
        firstNameBuf = etEmpFirstName.getText().toString().split("\\,");
        lastNameBuf = etEmpLastName.getText().toString().split("\\,");
        middleNameBuf = etEmpMiddleName.getText().toString().split("\\,");

        for (int i = 0; i < firstNameBuf.length; i++) {
            Employee emp = new Employee();
            emp.setFirstName(firstNameBuf[i]);
            emp.setLastName(lastNameBuf[i]);
            emp.setMiddleName(middleNameBuf[i]);
            emps.add(emp);
        }

        newSchedule.setEmployeeList(emps);

        etNote = (EditText) view.findViewById(R.id.etSetExamNote);
        newSchedule.setNote(etNote.getText().toString());

        etTime = (EditText) view.findViewById(R.id.etSetExamTime);
        newSchedule.setLessonTime(etTime.getText().toString());

        etLType = (EditText) view.findViewById(R.id.etSetExamLessonType);
        newSchedule.setLessonType(etLType.getText().toString());

        return newSchedule;
    }

    //get schedule params from add dialog
    private Schedule fillScheduleFromAddDialog(View view) {
        EditText etAud, etSubj, etEmpLastName, etEmpFirstName, etDate,
                etEmpMiddleName, etNote, etTime, etLType, etGroupName;

        Schedule newSchedule = new Schedule();

        etDate = (EditText) view.findViewById(R.id.etSetExamDate);
        newSchedule.setDate(etDate.getText().toString());

        etGroupName = (EditText) view.findViewById(R.id.etSetExamGroup);
        newSchedule.setStudentGroup(etGroupName.getText().toString());

        etAud = (EditText) view.findViewById(R.id.etSetExamAud);
        newSchedule.setAuditories(Arrays.asList(etAud.getText().toString().split("\\,")));

        etSubj = (EditText) view.findViewById(R.id.etSetExamSubj);
        newSchedule.setSubject(etSubj.getText().toString());


        List<Employee> emps = new ArrayList<Employee>();

        etEmpLastName = (EditText) view.findViewById(R.id.etSetExamEmpLastName);
        etEmpFirstName = (EditText) view.findViewById(R.id.etSetExamEmpFirstName);
        etEmpMiddleName = (EditText) view.findViewById(R.id.etSetExamEmpMiddleName);

        String[] firstNameBuf, lastNameBuf, middleNameBuf;
        firstNameBuf = etEmpFirstName.getText().toString().split("\\,");
        lastNameBuf = etEmpLastName.getText().toString().split("\\,");
        middleNameBuf = etEmpMiddleName.getText().toString().split("\\,");

        for (int i = 0; i < firstNameBuf.length; i++) {
            Employee emp = new Employee();
            emp.setFirstName(firstNameBuf[i]);
            emp.setLastName(lastNameBuf[i]);
            emp.setMiddleName(middleNameBuf[i]);
            emps.add(emp);
        }

        newSchedule.setEmployeeList(emps);

        etNote = (EditText) view.findViewById(R.id.etSetExamNote);
        newSchedule.setNote(etNote.getText().toString());

        etTime = (EditText) view.findViewById(R.id.etSetExamTime);
        newSchedule.setLessonTime(etTime.getText().toString());

        etLType = (EditText) view.findViewById(R.id.etSetExamLessonType);
        newSchedule.setLessonType(etLType.getText().toString());


        return newSchedule;
    }

    private void addScheduleRowAction(Schedule schedule, View view) {
        boolean isDateExist = false;
        Schedule record = fillScheduleFromAddDialog(view);
        SchoolDayDao sdd = new SchoolDayDao(DBHelper.getInstance(getActivity()));
        String buf = String.valueOf(sdd.getDbHelper().addScheduleToDataBase(record));
        record.setScheduleTableRowId(buf);
        sdd.setAsManual(record.getScheduleTableRowId());

        for (SchoolDay sd : allSchedules) {
            if (sd.getDayName().equals(record.getDate())) {
                sd.getSchedules().add(record);
                isDateExist = true;
                break;
            }
        }
        if (!isDateExist) {
            SchoolDay newSd = new SchoolDay();
            newSd.setDayName(record.getDate());
            List<Schedule> newSl = new ArrayList<Schedule>();
            newSl.add(record);
            newSd.setSchedules(newSl);
            isRefreshed = false;
            allSchedules.add(newSd);
            Intent intent = new Intent(getActivity(), MainActivity.class);
            getThisFragment().startActivity(intent);
        }

        updateListView();
        updateSchedule(currentSelectedPosition, showHidden);
    }

    private void editScheduleRowAction(Schedule schedule, View view) {
        Schedule record = fillScheduleFromEditDialog(view);
        record.setStudentGroup(schedule.getStudentGroup());

        SchoolDayDao sdd = new SchoolDayDao(DBHelper.getInstance(getActivity()));
        sdd.deleteScheduleTableRow(schedule.getScheduleTableRowId());

        String buf = String.valueOf(sdd.getDbHelper().addScheduleToDataBase(record));
        record.setScheduleTableRowId(buf);
        sdd.setAsManual(record.getScheduleTableRowId());


        allSchedules.get(currentSelectedPosition).getSchedules().remove(schedule);
        allSchedules.get(currentSelectedPosition).getSchedules().add(record);
        allSchedules.get(currentSelectedPosition).setSchedules(sdd.sortScheduleListByTime(
                allSchedules.get(currentSelectedPosition).getSchedules()));
        updateListView();
        updateSchedule(currentSelectedPosition, showHidden);
    }

    private void deleteScheduleRowAction(Schedule schedule) {
        SchoolDayDao sdd = new SchoolDayDao(DBHelper.getInstance(getActivity()));
        sdd.deleteScheduleTableRow(schedule.getScheduleTableRowId());

        Integer currDay = Integer.valueOf(schedule.getWeekDay().toString()) - 1;
        allSchedules.get(currentSelectedPosition).getSchedules().remove(schedule);
        allSchedules.get(currentSelectedPosition).setSchedules(sdd.sortScheduleListByTime(
                allSchedules.get(currentSelectedPosition).getSchedules()));
        updateListView();
        updateSchedule(currentSelectedPosition, showHidden);
    }

    private Integer executeAction(int which, Schedule schedule, boolean isForEmp, DialogInterface dialog) {
        switch (which) {
            case 0:
                createConfirmDeletingDialog(schedule);
                dialog.dismiss();
                return 0;

            case 1:
                createEditDialog(schedule, isForEmp);
                dialog.dismiss();
                return 0;

            case 2:
                SchoolDayDao sdd = new SchoolDayDao(DBHelper.getInstance(getActivity()));

                if (schedule.isHidden()) {
                    sdd.showScheduleRow(schedule.getScheduleTableRowId());
                    schedule.setHidden(false);
                } else {
                    sdd.hideScheduleRow(schedule.getScheduleTableRowId());
                    schedule.setHidden(true);
                }

                allSchedules.get(currentSelectedPosition).getSchedules().remove(schedule);
                allSchedules.get(currentSelectedPosition).getSchedules().add(schedule);
                allSchedules.get(currentSelectedPosition).setSchedules(sdd.sortScheduleListByTime(
                        allSchedules.get(currentSelectedPosition).getSchedules()));
                updateListView();
                updateSchedule(currentSelectedPosition, showHidden);
                dialog.dismiss();
                return 0;

            default: return -1;
        }
    }

    public void createAddDialog(Schedule s, boolean isForEmp) {
        final Schedule schedule = s;
        final View view = getEmptyView(s, isForEmp);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!getActivity().isFinishing()) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Добавление")
                            .setCancelable(false)
                            .setView(view)
                            .setPositiveButton("Добавить",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            addScheduleRowAction(schedule, view);
                                        }
                                    })

                            .setNegativeButton("Отмена",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                            .create().show();
                }
            }
        });
    }

    public void createEditDialog(Schedule s, boolean isForEmp) {
        final Schedule schedule = s;
        final View view = getEditView(s, isForEmp);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!getActivity().isFinishing()) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Редактирование")
                            .setCancelable(false)
                            .setView(view)
                            .setPositiveButton("Редактировать",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            editScheduleRowAction(schedule, view);
                                        }
                                    })

                            .setNegativeButton("Отмена",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                            .create().show();
                }
            }
        });
    }

    public void createConfirmDeletingDialog(Schedule s) {
        final Schedule schedule = s;
        TextView tv = new TextView(getActivity());
        tv.setTextSize(18);
        tv.setText("Вы точно хотите удалить эту запись?");
        final View view = tv;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!getActivity().isFinishing()) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Удаление")
                            .setCancelable(false)
                            .setView(view)
                            .setPositiveButton("Да",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            deleteScheduleRowAction(schedule);
                                        }
                                    })

                            .setNegativeButton("Нет",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                            .create().show();
                }
            }
        });
    }

    public void createChooseActionDialog(Schedule s, final boolean isForEmp) {
        final Schedule schedule = s;

        String[] actionsIfHidden = {"Удалить", "Редактировать", "Скрыть"};
        String[] actionsIfNotHidden = {"Удалить", "Редактировать", "Показывать"};
        final String[] items;
        if (!schedule.isHidden()) {
            items = actionsIfHidden;
        } else {
            items = actionsIfNotHidden;
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!getActivity().isFinishing()) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Выберите действие.")
                            .setCancelable(false)
                            .setItems(items, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (executeAction(which, schedule, isForEmp, dialog) < 0) {
                                        Log.d("Action Dialog", "Can't find handler for this action.");
                                    } else {
                                        Log.d("Action Dialog", "Action executed.");
                                    }
                                }
                            })
                            .setNegativeButton("Отмена",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                            .create().show();
                }
            }
        });
    }

}
