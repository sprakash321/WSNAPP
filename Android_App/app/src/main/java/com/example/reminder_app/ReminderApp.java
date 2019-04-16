package com.example.reminder_app;

import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.amazonaws.amplify.generated.graphql.ListRemindersQuery;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.ArrayList;

import javax.annotation.Nonnull;

public class ReminderApp extends AppCompatActivity {
    private final String TAG = ReminderApp.class.getSimpleName();
    ArrayList mReminders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_app);
        // get username from main activity after sign in

        // acquire reminder data from AWS
        // TODO
        ArrayList<reminder_info> reminders = new ArrayList<>();

        // split into multiple data into multiple lists
        reminder_data.reset();
        for (reminder_info reminder : reminders)
        {
            reminder_data.addName(reminder.reminder_name);
            reminder_data.addDay(reminder.days.toString());
            reminder_data.addTime(reminder.time);
        }

        // create reminder list
        reminder_data.setList((ListView) findViewById(R.id.reminder_list));
        reminder_list_adapter list_adapter = new reminder_list_adapter(this,
                reminder_data.getNames(), reminder_data.getDays(), reminder_data.getTimes());
        reminder_data.setAdapter(list_adapter);
        reminder_data.getList().setAdapter(list_adapter);

        // add reminder button
        Button add_btn = findViewById(R.id.add_reminder_btn);
        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent add_reminder_intent = new Intent(getApplicationContext(), add_reminder.class);
                startActivity(add_reminder_intent);
            }
        });

        ClientFactory.init(this);
    }
    @Override
    public void onResume() {
        super.onResume();

        // Query list data when we return to the screen
        query();
    }

    public void query(){
        ClientFactory.appSyncClient().query(ListRemindersQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(queryCallback);
    }

    private GraphQLCall.Callback<ListRemindersQuery.Data> queryCallback = new GraphQLCall.Callback<ListRemindersQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListRemindersQuery.Data> response) {

            mReminders = new ArrayList<>(response.data().listReminders().items());

            Log.i(TAG, "Retrieved list items: " + mReminders.toString());

//            Log.i(TAG, "Retrieved list items: " + mPets.toString());

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for(int i = 0; i < mReminders.size(); i++) {
                        String[] Tokens = mReminders.get(i).toString().split(", name=|, day=|, time=");
                        if(!reminder_data.name_exists(Tokens[1])) {
                            reminder_data.addName(Tokens[1]);
                            reminder_data.addDay(Tokens[2]);
                            reminder_data.addTime(Tokens[3].replace("}", ""));
                        }
                    }
                    reminder_data.getAdapter().notifyDataSetChanged();
                }
            });
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
           Log.e(TAG, e.toString());
        }
    };
}
