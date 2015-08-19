package com.example.myapplication.Utils;

import com.example.myapplication.Model.Employee;

/**
 * Created by iChrome on 18.08.2015.
 */
public class EmployeeUtil {
    private EmployeeUtil(){}

    public static String getEmployeeFIO(Employee employee) {
        String fio = employee.getLastName();
        if (employee.getFirstName() != null && (employee.getFirstName().length() > 0)) {
            fio += " " + employee.getFirstName().substring(0, 1) + ".";
            if (employee.getMiddleName() != null && (employee.getMiddleName().length() > 0)) {
                fio += " " + employee.getMiddleName().substring(0, 1) + ".";
            }
        }
        return fio;
    }
}
