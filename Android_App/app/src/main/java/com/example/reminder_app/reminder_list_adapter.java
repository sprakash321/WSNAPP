package com.example.reminder_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class reminder_list_adapter extends BaseAdapter {
    LayoutInflater mInflator;
    ArrayList<String> reminder_names = new ArrayList<>();
    ArrayList<String> reminder_days = new ArrayList<>();
    ArrayList<String> reminder_time = new ArrayList<>();

    public reminder_list_adapter(Context c, ArrayList<String> reminder_names,
                                 ArrayList<String> reminder_days,
                                 ArrayList<String> reminder_time) {
        this.reminder_names = reminder_names;
        this.reminder_days = reminder_days;
        this.reminder_time = reminder_time;
        mInflator = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return reminder_names.size();
    }

    @Override
    public Object getItem(int i) {
        return reminder_names.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        View v = mInflator.inflate(R.layout.reminder_list_display, null);
        final TextView reminder_name_txt = v.findViewById(R.id.reminder_name_input);
        final TextView reminder_days_txt = v.findViewById(R.id.reminder_days);
        final TextView reminder_time_txt = v.findViewById(R.id.reminder_time);

        // delete reminder button
        Button delete_btn = v.findViewById(R.id.delete_btn);
        delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reminder_names.remove(i);
                reminder_days.remove(i);
                reminder_time.remove(i);
                notifyDataSetChanged();
            }
        });

        String name = reminder_names.get(i);
        String days = reminder_days.get(i);
        String time = reminder_time.get(i);

        reminder_name_txt.setText(name);
        reminder_days_txt.setText(days);
        reminder_time_txt.setText(time);

        return v;
    }

}
