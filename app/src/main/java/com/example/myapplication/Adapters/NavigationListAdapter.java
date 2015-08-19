package com.example.myapplication.Adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.R;

/**
 * Created by iChrome on 19.08.2015.
 */
public class NavigationListAdapter extends ArrayAdapter<String> {
    private Context context;
    private int layoutID;
    private String[] data = null;
    private int[] iconsIDs = {R.drawable.ic_action_go_to_today,
                              R.drawable.ic_action_time,
                              R.drawable.ic_action_download,
                              R.drawable.ic_action_settings};


    public NavigationListAdapter(Context context, int layoutResourceId, String[] data){
        super(context, layoutResourceId, data);
        this.context = context;
        this.layoutID = layoutResourceId;
        this.data = data;
        context.getResources().getDrawable(R.drawable.ic_action_go_to_today);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            if (convertView == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                convertView = inflater.inflate(layoutID, parent, false);
            }

            ImageView icon = (ImageView) convertView.findViewById(R.id.navigationListIcon);
            icon.setImageDrawable(context.getResources().getDrawable(iconsIDs[position]));

            TextView label = (TextView) convertView.findViewById(R.id.navigationListLabel);
            label.setText(data[position]);

        } catch (Exception e){
            Log.e("navigationAdapter", e.toString());
        }
        return convertView;
    }


}
