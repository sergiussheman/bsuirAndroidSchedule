package com.example.myapplication;

import com.example.myapplication.Model.AvailableFragments;

/**
 * Created by iChrome on 13.08.2015.
 */
public interface OnFragmentInteractionListener {
    void onChangeFragment(AvailableFragments passedFragment);

    void onChangeDay(Integer dayPosition);

    void onChangeExamDay(Integer dayPosition);
}
