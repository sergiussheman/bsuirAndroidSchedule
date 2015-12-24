package com.example.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.dataprovider.LoadSchedule;
import com.example.myapplication.model.AvailableFragments;
import com.example.myapplication.model.Employee;
import com.example.myapplication.utils.DateUtil;
import com.example.myapplication.utils.FileUtil;
import com.example.myapplication.utils.WidgetUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Фрагмент для скачивания расписание преподавателей
 */
public class DownloadScheduleForEmployee extends Fragment {
    private static final String TAG = "employeeDownloadTAG";
    private static final String PATTERN = "^[а-яА-ЯёЁ]+";

    private List<Employee> availableEmployeeList;
    private List<String> downloadedSchedules;
    private  View currentView;
    private boolean isDownloadingNewSchedule;
    private TableLayout tableLayoutForDownloadedSchedules;
    ProgressDialog mProgressDialog;

    private OnFragmentInteractionListener mListener;

    /**
     * Фрагмент для скачивания расписание преподавателей
     */
    public DownloadScheduleForEmployee() {
        // Required empty public constructor
    }

    /**
     * Метод вызывается для того чтобы fragment создал view которую будет показано пользователю.
     * @param inflater объект служащий для создания view
     * @param container container это родительский view, к которому фрагмент будет присоединен
     * @param savedInstanceState Если фрагмент был пересоздан, то в savedInstanceState будут
     *                           хранится его сохраненные параметры
     * @return Возвращает View которое будет показано пользователю
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        currentView = inflater.inflate(R.layout.fragment_download_schedule_for_employee, container, false);

        try {
            ConnectivityManager connectMan = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectMan.getActiveNetworkInfo();
            if(networkInfo != null && networkInfo.isConnected()) {
                new DownloadEmployeeXML().execute();
            }
        } catch (Exception e){
            Toast.makeText(getActivity(), R.string.can_not_load_list_of_employees, Toast.LENGTH_LONG).show();
            Log.v(TAG, e.getMessage(), e);
        }

        Button downloadButton = (Button) currentView.findViewById(R.id.buttonForDownloadEmployeeSchedule);
        downloadButton.setOnClickListener(new DownloadButtonClickListener());

        setTableLayoutForDownloadedSchedules((TableLayout) currentView.findViewById(R.id.tableLayoutForEmployee));
        setDownloadedSchedules(FileUtil.getAllDownloadedSchedules(getActivity(), false));
        populateTableLayout(getTableLayoutForDownloadedSchedules(), downloadedSchedules);

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getString(R.string.downloading));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);
        return currentView;
    }

    /**
     * Метод вызывает асинхронный метод для скачивания или обновления расписания.
     * @param employeeNameForDownload Имя преподавателя для которого необходимо скачать или
     *                                обновить расписание
     */
    public void downloadOrUpdateScheduleForEmployee(String employeeNameForDownload){
        DownloadFilesTask task = new DownloadFilesTask(getActivity());
        task.filesDir = getActivity().getFilesDir();
        task.execute(employeeNameForDownload);
    }

    /**
     * Метод заполняет таблицу списком уже скачанных и сохраненных на устройстве
     * расписаний для преподавателя
     * @param tableLayout ссылка на таблицу в которую помещается список скачанных расписаний
     * @param schedulesForEmployee Список преподавателей для которых скачано расписание
     */
    public void populateTableLayout(final TableLayout tableLayout, final List<String> schedulesForEmployee){
        Integer currentRowNumber = 0;
        tableLayout.removeAllViews();
        tableLayout.setPadding(5, 0, 5, 0);
        TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);

        TableRow headerRow = new TableRow(getActivity());
        TextView employeeHeaderTextView = new TextView(getActivity());
        employeeHeaderTextView.setText(getResources().getString(R.string.employee_shortcut));
        employeeHeaderTextView.setGravity(Gravity.START);
        employeeHeaderTextView.setLayoutParams(params);
        employeeHeaderTextView.setTypeface(null, Typeface.BOLD);
        headerRow.addView(employeeHeaderTextView);

        TextView lastUpdateHeader = new TextView(getActivity());
        lastUpdateHeader.setText(getResources().getString(R.string.last_updated));
        lastUpdateHeader.setTypeface(null, Typeface.BOLD);
        headerRow.addView(lastUpdateHeader);

        TextView deleteHeader = new TextView(getActivity());
        deleteHeader.setText(getResources().getString(R.string.delete));
        deleteHeader.setTypeface(null, Typeface.BOLD);
        deleteHeader.setGravity(Gravity.CENTER);
        deleteHeader.setPadding(5, 0, 5, 0);
        headerRow.addView(deleteHeader);

        TextView refreshHeader = new TextView(getActivity());
        refreshHeader.setText(R.string.refresh);
        refreshHeader.setTypeface(null, Typeface.BOLD);
        refreshHeader.setGravity(Gravity.CENTER);
        refreshHeader.setPadding(5,0,5,0);
        headerRow.addView(refreshHeader);

        headerRow.setPadding(0, 0, 0, 18);
        tableLayout.addView(headerRow);

        for(String currentEmployeeSchedule : schedulesForEmployee) {
            TableRow rowForEmployeeSchedule = new TableRow(getActivity());
            TextView textViewForEmployeeName = new TextView(getActivity());
            textViewForEmployeeName.setGravity(Gravity.START);
            textViewForEmployeeName.setText(getEmployeeNameFromString(currentEmployeeSchedule));
            textViewForEmployeeName.setLayoutParams(params);
            rowForEmployeeSchedule.addView(textViewForEmployeeName);

            TextView lastUpdatedTextView = new TextView(getActivity());
            lastUpdatedTextView.setText(getLastUpdateFromPreference(currentEmployeeSchedule));
            rowForEmployeeSchedule.addView(lastUpdatedTextView);


            ImageView deleteImageView = new ImageView(getActivity());
            deleteImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_remove));
            deleteImageView.setOnClickListener(new ButtonClickListener(tableLayout, schedulesForEmployee));
            rowForEmployeeSchedule.addView(deleteImageView);

            ImageView refreshImageView = new ImageView(getActivity());
            refreshImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_refresh));
            refreshImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ConnectivityManager connectMan = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connectMan.getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.isConnected()) {
                        TableRow selectedRow = (TableRow) v.getParent();
                        Integer rowNumber = (Integer) selectedRow.getTag();
                        String fileNameForRefresh = schedulesForEmployee.get(rowNumber);
                        fileNameForRefresh = fileNameForRefresh.substring(0, fileNameForRefresh.length() - 4);
                        setIsDownloadingNewSchedule(false);
                        downloadOrUpdateScheduleForEmployee(fileNameForRefresh);
                        updateDefaultEmployee(fileNameForRefresh, true);
                    } else {
                        Toast.makeText(getActivity(), getResources().getString(R.string.no_connection_to_network), Toast.LENGTH_LONG).show();
                    }
                }
            });
            rowForEmployeeSchedule.addView(refreshImageView);

            rowForEmployeeSchedule.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TableRow selectedTableRow = (TableRow) v;
                    Integer rowNumber = (Integer) selectedTableRow.getTag();
                    String selectedEmployee = schedulesForEmployee.get(rowNumber);
                    updateDefaultEmployee(selectedEmployee, false);
                    mListener.onChangeFragment(AvailableFragments.SHOW_SCHEDULES);
                }
            });

            rowForEmployeeSchedule.setTag(currentRowNumber);
            currentRowNumber++;
            tableLayout.addView(rowForEmployeeSchedule);
        }
    }

    /**
     * Метод вызывается при удалении расписания преподавателя. Метод проверяет является
     * ли дефолтное расписание расписанием удаляемого преподавателя. Если это так, то устанавливаем
     * дефолтное расписание в null
     * @param passedEmployeeName Имя удаляемого преподавателя
     */
    private void deleteDefaultEmployeeIfNeed(String passedEmployeeName){
        String settingFileName = getActivity().getString(R.string.setting_file_name);
        final SharedPreferences preferences = getActivity().getSharedPreferences(settingFileName, 0);
        final SharedPreferences.Editor editor = preferences.edit();
        String employeeFieldInSettings = getActivity().getString(R.string.default_employee_field_in_settings);
        String defaultEmployeeName = preferences.getString(employeeFieldInSettings, "none");
        String employeeName = passedEmployeeName.substring(0, passedEmployeeName.length() - 4);
        if(employeeName.equalsIgnoreCase(defaultEmployeeName)){
            editor.remove(employeeFieldInSettings);

        }
        editor.remove(employeeName);
        editor.apply();
    }

    /**
     * Метод обновляет название дефолтного расписания. Метод взывается после скачивания нового
     * расписания, или если пользователь выбирает расписание из списка скачанных ранее расписаний.
     * @param employee Объект указыающий на удаляемого преподавателя
     * @param isDownloadedSchedule Переменная указывает было ли расписание скачано, или же
     *                             оно было выбрано из списка ранее скачанных
     */
    private void updateDefaultEmployee(Employee employee, boolean isDownloadedSchedule){
        String settingFileName = getActivity().getString(R.string.setting_file_name);
        final SharedPreferences preferences = getActivity().getSharedPreferences(settingFileName, 0);
        final SharedPreferences.Editor editor = preferences.edit();
        String employeeFieldInSettings = getActivity().getString(R.string.default_employee_field_in_settings);
        String groupFiledInSettings = getActivity().getString(R.string.default_group_field_in_settings);
        editor.putString(groupFiledInSettings, "none");
        String employeeForPreferences = getFileNameForEmployeeSchedule(employee);
        editor.putString(employeeFieldInSettings, employeeForPreferences);
        if(isDownloadedSchedule) {
            editor.putString(employeeForPreferences, DateUtil.getCurrentDateAsString());
        }
        editor.apply();
        WidgetUtil.updateWidgets(getActivity());
    }

    /**
     * Метод обновляет название дефолтного расписания. Метод взывается после скачивания нового
     * расписания, или если пользователь выбирает расписание из списка скачанных ранее расписаний.
     * @param passedDefaultEmployee Имя удаляемого преподавателя
     * @param isDownloadedSchedule Переменная указывает было ли расписание скачано, или же
     *                             оно было выбрано из списка ранее скачанных
     */
    private void updateDefaultEmployee(String passedDefaultEmployee, boolean isDownloadedSchedule){
        String defaultEmployee = passedDefaultEmployee;
        if(".xml".equalsIgnoreCase(defaultEmployee.substring(defaultEmployee.length() -4))) {
            defaultEmployee = defaultEmployee.substring(0, defaultEmployee.length() - 4);
        }
        String settingFileName = getActivity().getString(R.string.setting_file_name);
        final SharedPreferences preferences = getActivity().getSharedPreferences(settingFileName, 0);
        final SharedPreferences.Editor editor = preferences.edit();
        String employeeFieldInSettings = getActivity().getString(R.string.default_employee_field_in_settings);
        String currentDefaultEmployee = preferences.getString(employeeFieldInSettings, "none");
        if(!defaultEmployee.equalsIgnoreCase(currentDefaultEmployee)){
            editor.putString(employeeFieldInSettings, defaultEmployee);
            String groupFiledInSettings = getActivity().getString(R.string.default_group_field_in_settings);
            editor.putString(groupFiledInSettings, "none");
        }
        if(isDownloadedSchedule) {
            editor.putString(defaultEmployee, DateUtil.getCurrentDateAsString());
        }
        editor.apply();
        WidgetUtil.updateWidgets(getActivity());
    }

    /**
     * Получает даты последнего обновления для расписания
     * @param passedSchedulesName Расписание для которого необходимо получить дату последнего
     *                            обновления
     * @return возвращает дату последнего обновления
     */
    private String getLastUpdateFromPreference(String passedSchedulesName){
        String schedulesName = passedSchedulesName.substring(0, passedSchedulesName.length() - 4);
        String settingFileName = getActivity().getString(R.string.setting_file_name);
        final SharedPreferences preferences = getActivity().getSharedPreferences(settingFileName, 0);
        return preferences.getString(schedulesName, "-");
    }

    /**
     * Метод получает строку в которой соединены имя преподавателя и его id. Используя регулярное
     * выражения достается только имя преподавателя
     * @param passedString строка состоящая из имени преподавателя и его id
     * @return Возвращает имя преподавателя
     */
    private static String getEmployeeNameFromString(String passedString){
        Pattern pattern = Pattern.compile(PATTERN);
        Matcher matcher = pattern.matcher(passedString);
        if(matcher.find()){
            return matcher.group(0);
        }
        return "Not found matches";
    }

    /**
     * Метод возвращает ФИО преподавтеля
     * @param employee Преподаватель для которого нужно вернуть ФИО
     * @return возвращает ФИО
     */
    private String getFileNameForEmployeeSchedule(Employee employee){
        return employee.getLastName() + employee.getFirstName().charAt(0) + employee.getMiddleName().charAt(0) + employee.getId();
    }

    /**
     * Возвращает объект Employee по полученному имени преподавателя
     * @param selectedEmployee Введенное пользователем имя преподавателя
     * @return возвращает объект Employee
     */
    @Nullable
    private Employee getEmployeeByName(String selectedEmployee){
        if(getAvailableEmployeeList() != null && !getAvailableEmployeeList().isEmpty()) {
            for (Employee employee : getAvailableEmployeeList()) {
                String employeeAsString = employeeToString(employee);
                if (employeeAsString.equalsIgnoreCase(selectedEmployee)) {
                    return employee;
                }
            }
        }
        return null;
    }

    /**
     * Конвертит лист преподавателей в массив имен преподавателей
     * @param employees лист преподавателй
     * @return возвращает массив имен преподавателей
     */
    private String[] convertEmployeeToArray(List<Employee> employees){
        List<String> resultList = new ArrayList<>();
        for(Employee employee : employees){
            resultList.add(employeeToString(employee));
        }
        String[] resultArray = new String[resultList.size()];
        resultArray = resultList.toArray(resultArray);
        return resultArray;
    }

    /**
     * Конвертит объкт Employee в строку
     * @param employee Преподаватель
     * @return Возаращает ФИО полученного преподавателя
     */
    private String employeeToString(Employee employee){
        String employeeFIO = employee.getLastName();
        if(employee.getFirstName() != null && employee.getFirstName().length() > 0){
            employeeFIO += " " + employee.getFirstName();
            if(employee.getMiddleName() != null && employee.getMiddleName().length() > 0){
                employeeFIO += " " + employee.getMiddleName();
            }
        }
        return employeeFIO;
    }

    /**
     * Метод вызывается после присоединения фрагмента к активити
     * @param activity ссылка на активити к которой присоединен фрагмент
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            Log.v(TAG, e.getMessage(), e);
            throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    /**
     * Метод вызывается при отсоединении фрагмента от активити
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public List<Employee> getAvailableEmployeeList() {
        return availableEmployeeList;
    }

    public void setAvailableEmployeeList(List<Employee> availableEmployeeList) {
        this.availableEmployeeList = availableEmployeeList;
    }

    /**
     * Асинхронный класс для скачивания списка преподавателей у которых есть расписание
     */
    private class DownloadEmployeeXML extends AsyncTask<Void, Void, String> {

        /**
         * Метод который в фоне скачивает список преподавателей у которых есть расписание
         * @param parameters null
         * @return Возвращает null если скачивание прошло успешно, иначе возвращает сообщение об
         * ошибке
         */
        @Override
        protected String doInBackground(Void... parameters) {
            try {
                List<Employee> loadedEmployees = LoadSchedule.loadListEmployee();
                setAvailableEmployeeList(loadedEmployees);
                return null;
            } catch (Exception e){
                Log.v(TAG, e.getMessage(), e);
                return e.toString();
            }
        }

        /**
         * Метод вызывается после скачивания списка доступных преподавателей. В  методе
         * происходит конфигурация AutoCompleteTextView
         * @param result null
         */
        @Override
        protected void onPostExecute(String result) {
            try {
                if(result == null) {
                    String[] employeesAsArray = convertEmployeeToArray(availableEmployeeList);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, employeesAsArray);
                    AutoCompleteTextView textView = (AutoCompleteTextView) currentView.findViewById(R.id.autoCompleteForEmployee);
                    textView.setAdapter(adapter);
                }
            } catch (Exception e){
                Log.v(TAG, "Exception occurred" + e.toString(), e);
            }
        }
    }

    private class DownloadFilesTask extends AsyncTask<String, Integer, String> {
        private File filesDir;
        private Context context;
        private PowerManager.WakeLock mWakeLock;

        /**
         * Класс для скачивания расписания для выбранного пользователем преподавателя
         * @param context контекст
         */
        public DownloadFilesTask(Context context) {
            this.context = context;
        }

        /**
         * Метод в фоне скачивает расписание для преподавателя
         * @param employeeName Имя преподавателя для которого нужно скачать расписание
         * @return Возвращает null если скачивание прошло успешно, иначе возвращает сообщение об
         * ошибке
         */
        protected String doInBackground(String... employeeName) {
            return LoadSchedule.loadScheduleForEmployee(employeeName[0], filesDir);
        }

        /**
         * Метод показывает диалоговое окно, с информацией об скачивании расписания
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            mProgressDialog.show();
        }

        /**
         * Обновляе диалое окно, в котором отображается процесс скачивания
         * @param progress процент скачанного
         */
        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgress(progress[0]);
        }

        /**
         * Метод вызывается после скачивания расписания для преподавателя. Выводит сообщение об успешном
         * скачивании расписания, или сообщение об ошибке
         * @param finalResult Результат скачивания. null если скачивание прошло успешно, иначе
         *                    сообщение об ошибке
         */
        protected void onPostExecute(String finalResult) {
            mWakeLock.release();
            mProgressDialog.dismiss();
            if(finalResult != null) {
                Toast.makeText(getActivity(), getString(R.string.error_while_downloading_schedule), Toast.LENGTH_LONG).show();
            } else {
                if(isDownloadingNewSchedule()) {
                    mListener.onChangeFragment(AvailableFragments.SHOW_SCHEDULES);
                } else{
                    populateTableLayout(getTableLayoutForDownloadedSchedules(), getDownloadedSchedules());
                }
            }
        }
    }

    public List<String> getDownloadedSchedules() {
        return downloadedSchedules;
    }

    public void setDownloadedSchedules(List<String> downloadedSchedules) {
        this.downloadedSchedules = downloadedSchedules;
    }

    public boolean isDownloadingNewSchedule() {
        return isDownloadingNewSchedule;
    }

    public void setIsDownloadingNewSchedule(boolean isDownloadingNewSchedule) {
        this.isDownloadingNewSchedule = isDownloadingNewSchedule;
    }

    public TableLayout getTableLayoutForDownloadedSchedules() {
        return tableLayoutForDownloadedSchedules;
    }

    public void setTableLayoutForDownloadedSchedules(TableLayout tableLayoutForDownloadedSchedules) {
        this.tableLayoutForDownloadedSchedules = tableLayoutForDownloadedSchedules;
    }

    private class ButtonClickListener implements View.OnClickListener {
        private TableLayout tableLayout;
        private List<String> schedulesForEmployee;

        /**
         * listener для кнопки удаления преподавателя
         * @param passedTableLayout таблица, которую нужно обновить после удаления преподавателя
         * @param passedSchedulesForEmployee список преподаватель для которых скачано расписание
         */
        public ButtonClickListener(final TableLayout passedTableLayout, final List<String> passedSchedulesForEmployee){
            tableLayout = passedTableLayout;
            schedulesForEmployee = passedSchedulesForEmployee;
        }

        /**
         * Обработчик нажатия на кнопку удаления расписания преподавателя
         * @param v Ссылка на нажатую кнопку
         */
        @Override
        public void onClick(final View v) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(getResources().getString(R.string.delete))
                    .setMessage(getResources().getString(R.string.confirm_delete_message))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            TableRow selectedRow = (TableRow) v.getParent();
                            Integer rowNumber = (Integer) selectedRow.getTag();
                            String fileNameForDelete = schedulesForEmployee.get(rowNumber);
                            File file = new File(getActivity().getFilesDir(), fileNameForDelete);
                            if (!file.delete()) {
                                Toast.makeText(getActivity(), R.string.error_while_deleting_file, Toast.LENGTH_LONG).show();
                            }
                            deleteDefaultEmployeeIfNeed(fileNameForDelete);
                            setDownloadedSchedules(FileUtil.getAllDownloadedSchedules(getActivity(), false));
                            populateTableLayout(tableLayout, getDownloadedSchedules());
                        }
                    })
                    .setNegativeButton(android.R.string.no, null).show();
        }
    }

    /**
     * Обработчик на нажатие кнопки скачивания расписания
     */
    private class DownloadButtonClickListener implements View.OnClickListener {
        /**
         * Метод вызывается когда пользователь нажимает на кнопку скачивания расписания для преподавателя
         * @param v Ссылка на кнопку скачивания расписания
         */
        @Override
        public void onClick(View v) {
            ConnectivityManager connectMan = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectMan.getActiveNetworkInfo();
            if(networkInfo != null && networkInfo.isConnected()) {
                AutoCompleteTextView textView = (AutoCompleteTextView) currentView.findViewById(R.id.autoCompleteForEmployee);
                String selectedEmployeeName = textView.getText().toString();
                Employee selectedEmployee = getEmployeeByName(selectedEmployeeName);
                if(selectedEmployee != null) {
                    String parameterForDownloadSchedule = getFileNameForEmployeeSchedule(selectedEmployee);
                    setIsDownloadingNewSchedule(true);
                    downloadOrUpdateScheduleForEmployee(parameterForDownloadSchedule);
                    updateDefaultEmployee(selectedEmployee, true);

                } else{
                    Toast toast = Toast.makeText(getActivity(), getResources().getString(R.string.not_found_schedule_for_you), Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP, 0, 30);
                    toast.show();
                }
            } else{
                Toast.makeText(getActivity(), getResources().getString(R.string.no_connection_to_network), Toast.LENGTH_LONG).show();
            }
        }
    }
}
