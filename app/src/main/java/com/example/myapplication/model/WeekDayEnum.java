package com.example.myapplication.model;

/**
 * Created by iChrome on 29.12.2015.
 */
public enum WeekDayEnum {
    MONDAY(1, "Понедельник"),
    TUESDAY(2, "Вторник"),
    WEDNESDAY(3, "Среда"),
    THURSDAY(4, "Четверг"),
    FRIDAY(5, "Пятница"),
    SATURDAY(6, "Суббота"),
    SUNDAY(7, "Воскресенье");

    private Integer order;
    private String name;

    WeekDayEnum(Integer passedOrder, String passedName){
        order = passedOrder;
        name = passedName;
    }

    public Integer getOrder() {
        return order;
    }

    public String getName() {
        return name;
    }

    public static WeekDayEnum getDayByName(String passedName){
        for(WeekDayEnum weekDayEnum : WeekDayEnum.values()){
            if(weekDayEnum.getName().equalsIgnoreCase(passedName)){
                return weekDayEnum;
            }
        }
        return null;
    }
}
