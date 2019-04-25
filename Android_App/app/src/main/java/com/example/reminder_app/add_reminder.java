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
import com.amazonaws.amplify.generated.graphql.ListRemindersQuery;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Operation;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.SimpleTimeZone;

import javax.annotation.Nonnull;

import type.CreateReminderInput;

public class add_reminder extends AppCompatActivity {
    EditText reminder_name_input;
    TextView date_input;
    TimePicker start_time_input;
    TimePicker end_time_input;
    Button add_confirm_btn;
    Button add_deny_btn;
    Calendar myCalendar;
    DatePickerDialog.OnDateSetListener date;

    ArrayList<ListRemindersQuery.Item> mReminders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reminder);

        reminder_name_input = findViewById(R.id.reminder_name_input);
        date_input = findViewById(R.id.date_input);
        start_time_input = findViewById(R.id.start_time_input);
        end_time_input = findViewById(R.id.end_time_input);
        add_confirm_btn = findViewById(R.id.add_confirm_btn);
        add_deny_btn = findViewById((R.id.add_deny_btn));
        myCalendar = Calendar.getInstance();

        add_confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = reminder_name_input.getText().toString();
                String day = date_input.getText().toString();
                String start_time = time2String(start_time_input);
                String end_time = time2String(end_time_input);

                int start_time_total = start_time_input.getHour()*60 + start_time_input.getMinute();
                int end_time_total = end_time_input.getHour()*60 + end_time_input.getMinute();

                if(name.isEmpty() || day.isEmpty() || start_time.isEmpty() || end_time.isEmpty()) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "At least one of the input fields are empty", Toast.LENGTH_SHORT);
                    toast.show();
                } else if (start_time_total <= end_time_total) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "End time is later than start time", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    save(name, day, start_time, end_time);
                    finish();
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

    private String time2String(TimePicker time){
        String output;
        int hour = time.getHour();
        int min = time.getMinute();
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
            output = new StringBuilder().append(hour).append(" : ")
                    .append("0").append(min).append(" ").append(format).toString();
        } else {
            output = new StringBuilder().append(hour).append(" : ")
                    .append(min).append(" ").append(format).toString();
        }
        return output;
    }

    private void updateLabel() {
        String myFormat = "MM/dd/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        date_input.setText(sdf.format(myCalendar.getTime()));
    }
    private void save(String name, String day, String start_time, String end_time) {

        CreateReminderInput input = CreateReminderInput.builder()
                .name(name)
                .day(day)
                .start_time(start_time)
                .end_time(end_time)
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
                    query();
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
                }
            });
        }
    };

    public void query(){
        ClientFactory.appSyncClient().query(ListRemindersQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(queryCallback);
    }

    private GraphQLCall.Callback<ListRemindersQuery.Data> queryCallback = new GraphQLCall.Callback<ListRemindersQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListRemindersQuery.Data> response) {

            mReminders = new ArrayList<>(response.data().listReminders().items());

            Log.i("", "Retrieved list items: " + mReminders.toString());

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for(int i = 0; i < mReminders.size(); i++) {
                        String[] Tokens = mReminders.get(i).toString().split("id=|, name=|, day=|, start_time=|, end_time=");
                        if(!reminder_data.id_exists(Tokens[1])) {
                            reminder_data.addID(Tokens[1]);
                            reminder_data.addName(Tokens[2]);
                            reminder_data.addDay(Tokens[3]);
                            reminder_data.addStartTime(Tokens[4]);
                            reminder_data.addEndTime(Tokens[5].replace("}", ""));
                        }
                    }
                    reminder_data.getAdapter().notifyDataSetChanged();
                }
            });
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("", e.toString());
        }
    };
}
