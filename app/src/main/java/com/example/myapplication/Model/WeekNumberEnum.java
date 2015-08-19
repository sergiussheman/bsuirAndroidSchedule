package com.example.myapplication.Model;

/**
 * Created by iChrome on 18.08.2015.
 */
public enum WeekNumberEnum {
    FIRST(1),
    SECOND(2),
    THIRD(3),
    FOURTH(4),
    ALL(5);

    private Integer order;

    WeekNumberEnum(Integer passedOrder){
        order = passedOrder;
    }

    public Integer getOrder(){
        return order;
    }
}
