package com.example.reminder_app;

import android.widget.ListView;

import java.util.ArrayList;

public class reminder_data {
    public static String username;
    public static ListView reminder_list;
    public static  reminder_list_adapter list_adapter;

    public static ArrayList<String> reminder_id = new ArrayList<>();
    public static ArrayList<String> reminder_names = new ArrayList<>();
    public static ArrayList<String> reminder_days = new ArrayList<>();
    public static ArrayList<String> reminder_start_times = new ArrayList<>();
    public static ArrayList<String> reminder_end_times = new ArrayList<>();

    // get functions
    public static String getUser() { return username; }
    public static ListView getList() { return reminder_list; }
    public static reminder_list_adapter getAdapter() { return list_adapter; }

    public static ArrayList<String> getIDs() { return reminder_id; }
    public static String getID(int idx) { return reminder_id.get(idx); }

    public static ArrayList<String> getNames() { return reminder_names; }
    public static String getName(int idx) { return reminder_names.get(idx); }

    public static ArrayList<String> getDays() { return reminder_days; }
    public static String getDay(int idx) { return reminder_days.get(idx); }

    public static ArrayList<String> getStartTimes() { return reminder_start_times; }
    public static String getStartTime(int idx) { return reminder_start_times.get(idx); }

    public static ArrayList<String> getEndTimes() { return reminder_end_times; }
    public static String getEndTime(int idx) { return reminder_end_times.get(idx); }

    // add functions
    public static void addID(String data) { reminder_data.reminder_id.add(data); }
    public static void addName(String data) { reminder_data.reminder_names.add(data); }
    public static void addDay(String data) { reminder_data.reminder_days.add(data); }
    public static void addStartTime(String data) { reminder_data.reminder_start_times.add(data); }
    public static void addEndTime(String data) { reminder_data.reminder_end_times.add(data); }

    // remove functions
    public static void removeID(String data) { try{reminder_data.reminder_id.remove(data);} catch(Exception e) {} }
    public static void removeName(String data) { try{reminder_data.reminder_names.remove(data);} catch(Exception e) {} }
    public static void removeDay(String data) { try{reminder_data.reminder_days.remove(data);} catch(Exception e) {} }
    public static void removeStartTime(String data) { try{reminder_data.reminder_start_times.remove(data);} catch(Exception e) {} }
    public static void removeEndTime(String data) { try{reminder_data.reminder_end_times.remove(data);} catch(Exception e) {} }

    // set functions
    public static void setUser(String user) { reminder_data.username = user; }
    public static void setList(ListView data) { reminder_data.reminder_list = data; }
    public static void setAdapter(reminder_list_adapter data) { reminder_data.list_adapter = data; }
    public static void setNames(ArrayList<String> data) { reminder_data.reminder_names = data; }
    public static void setDays(ArrayList<String> data) { reminder_data.reminder_days = data; }
    public static void setStartTimes(ArrayList<String> data) { reminder_data.reminder_start_times = data; }
    public static void setEndTimes(ArrayList<String> data) { reminder_data.reminder_end_times = data; }

    // reset
    public static void reset() {
        reminder_data.username = new String();
        reminder_data.reminder_id.clear();
        reminder_data.reminder_names.clear();
        reminder_data.reminder_days.clear();
        reminder_data.reminder_start_times.clear();
        reminder_data.reminder_end_times.clear();
    }

    public static Boolean name_exists(String name) {
        if(reminder_data.reminder_names.contains(name)) {
            return true;
        }
        return false;
    }

    public static Boolean id_exists(String id) {
        if(reminder_data.reminder_id.contains(id)) {
            return true;
        }
        return false;
    }

}
