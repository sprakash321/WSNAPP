package com.example.reminder_app;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class reminder_info implements Parcelable {
    String reminder_name;
    ArrayList<String> days;
    String time;

    public reminder_info() {
        this.days = new ArrayList<>();
    }

    protected reminder_info(Parcel in) {
        reminder_name = in.readString();
        if (in.readByte() == 0x01) {
            days = new ArrayList<String>();
            in.readList(days, String.class.getClassLoader());
        } else {
            days = null;
        }
        time = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(reminder_name);
        if (days == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(days);
        }
        dest.writeString(time);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<reminder_info> CREATOR = new Parcelable.Creator<reminder_info>() {
        @Override
        public reminder_info createFromParcel(Parcel in) {
            return new reminder_info(in);
        }

        @Override
        public reminder_info[] newArray(int size) {
            return new reminder_info[size];
        }
    };
}