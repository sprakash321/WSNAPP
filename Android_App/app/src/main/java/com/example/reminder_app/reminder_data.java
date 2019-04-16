package com.example.reminder_app;

import android.widget.ListView;

import java.util.ArrayList;

public class reminder_data {
    public static String username;
    public static ListView reminder_list;
    public static  reminder_list_adapter list_adapter;

    public static ArrayList<String> reminder_names;
    public static ArrayList<String> reminder_days;
    public static ArrayList<String> reminder_times;

    // get functions
    public static String getUser() { return username; }
    public static ListView getList() { return reminder_list; }
    public static reminder_list_adapter getAdapter() { return list_adapter; }

    public static ArrayList<String> getNames() { return reminder_names; }
    public static String getName(int idx) { return reminder_names.get(idx); }

    public static ArrayList<String> getDays() { return reminder_days; }
    public static String getDay(int idx) { return reminder_days.get(idx); }

    public static ArrayList<String> getTimes() { return reminder_times; }
    public static String getTime(int idx) { return reminder_times.get(idx); }

    // add functions
    public static void addName(String data) { reminder_data.reminder_names.add(data); }
    public static void addDay(String data) { reminder_data.reminder_days.add(data); }
    public static void addTime(String data) { reminder_data.reminder_times.add(data); }

    // remove functions
    public static void removeName(String data) { reminder_data.reminder_names.remove(data); }
    public static void removeDay(String data) { reminder_data.reminder_days.remove(data); }
    public static void removeTime(String data) { reminder_data.reminder_times.remove(data); }

    // set functions
    public static void setUser(String user) { reminder_data.username = user; }
    public static void setList(ListView data) { reminder_data.reminder_list = data; }
    public static void setAdapter(reminder_list_adapter data) { reminder_data.list_adapter = data; }
    public static void setNames(ArrayList<String> data) { reminder_data.reminder_names = data; }
    public static void setDays(ArrayList<String> data) { reminder_data.reminder_days = data; }
    public static void setTimes(ArrayList<String> data) { reminder_data.reminder_times = data; }

    // reset
    public static void reset() {
        reminder_data.username = new String();
        reminder_data.reminder_names = new ArrayList<String>();
        reminder_data.reminder_days = new ArrayList<String>();
        reminder_data.reminder_times = new ArrayList<String>();
    }

    public static Boolean name_exists(String name) {
        if(reminder_data.reminder_names.contains(name)) {
            return true;
        }
        return false;
    }
}
