package com.example.reminder_app;

import android.app.DatePickerDialog;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.amazonaws.amplify.generated.graphql.CreateReminderMutation;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.SimpleTimeZone;

import javax.annotation.Nonnull;

import type.CreateReminderInput;

public class add_reminder extends AppCompatActivity {
    EditText reminder_name_input;
    TextView date_input;
    TimePicker time_input;
    Button add_confirm_btn;
    Button add_deny_btn;
    Calendar myCalendar;
    DatePickerDialog.OnDateSetListener date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reminder);

        reminder_name_input = findViewById(R.id.reminder_name_input);
        date_input = findViewById(R.id.date_input);
        time_input = findViewById(R.id.time_input);
        add_confirm_btn = findViewById(R.id.add_confirm_btn);
        add_deny_btn = findViewById((R.id.add_deny_btn));
        myCalendar = Calendar.getInstance();

        add_confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = reminder_name_input.getText().toString();
                String day = date_input.getText().toString();
                String time;

                int hour = time_input.getHour();
                int min = time_input.getMinute();
                String format;
                if (hour == 0) {
                    hour += 12;
                    format = "AM";
                } else if (hour == 12) {
                    format = "PM";
                } else if (hour > 12) {
                    hour -= 12;
                    format = "PM";
                } else {
                    format = "AM";
                }
                if(min < 10) {
                    time = new StringBuilder().append(hour).append(" : ")
                            .append("0").append(min).append(" ").append(format).toString();
                } else {
                    time = new StringBuilder().append(hour).append(" : ")
                            .append(min).append(" ").append(format).toString();
                }

                if(name.isEmpty() || day.isEmpty() || time.isEmpty()) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "At least one of the input fields are empty", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
//                    reminder_data.addName(name);
//                    reminder_data.addDay(day);
//                    reminder_data.addTime(time);
//                    reminder_data.getAdapter().notifyDataSetChanged();

                    save(name, day, time);
                }
            }
        });

        add_deny_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        reminder_name_input.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };

        date_input.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(add_reminder.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private void updateLabel() {
        String myFormat = "MM/dd/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        date_input.setText(sdf.format(myCalendar.getTime()));
    }
    private void save(String name, String day, String time) {

        CreateReminderInput input = CreateReminderInput.builder()
                .name(name)
                .day(day)
                .time(time)
                .build();

        CreateReminderMutation addReminderMutation = CreateReminderMutation.builder()
                .input(input)
                .build();
        ClientFactory.appSyncClient().mutate(addReminderMutation).enqueue(mutateCallback);
    }

    // Mutation callback code
    private GraphQLCall.Callback<CreateReminderMutation.Data> mutateCallback = new GraphQLCall.Callback<CreateReminderMutation.Data>() {
        @Override
        public void onResponse(@Nonnull final Response<CreateReminderMutation.Data> response) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(add_reminder.this, "Added reminder", Toast.LENGTH_SHORT).show();
                    add_reminder.this.finish();
                }
            });
        }

        @Override
        public void onFailure(@Nonnull final ApolloException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("", "Failed to perform AddReminderMutation", e);
                    Toast.makeText(add_reminder.this, "Failed to add reminder", Toast.LENGTH_SHORT).show();
                    add_reminder.this.finish();
                }
            });
        }
    };
}