package com.example.reminder_app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.amplify.generated.graphql.CreateReminderMutation;
import com.amazonaws.amplify.generated.graphql.DeleteReminderMutation;
import com.amazonaws.amplify.generated.graphql.ListRemindersQuery;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.ArrayList;

import javax.annotation.Nonnull;

import type.CreateReminderInput;
import type.DeleteReminderInput;

public class reminder_list_adapter extends BaseAdapter {
    private final String TAG = reminder_list_adapter.class.getSimpleName();
    ArrayList<ListRemindersQuery.Item> mReminders;

    LayoutInflater mInflator;
    ArrayList<String> reminder_ids;
    ArrayList<String> reminder_names;
    ArrayList<String> reminder_days;
    ArrayList<String> reminder_time;

    private Activity activity; //activity is defined as a global variable in your AsyncTask

    public reminder_list_adapter(Context c, ArrayList<String> reminder_ids,
                                 ArrayList<String> reminder_names,
                                 ArrayList<String> reminder_days,
                                 ArrayList<String> reminder_time,
                                 Activity activity) {
        this.reminder_ids = reminder_ids;
        this.reminder_names = reminder_names;
        this.reminder_days = reminder_days;
        this.reminder_time = reminder_time;
        this.activity = activity;
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
                delete(reminder_data.getID(i));

                reminder_ids.remove(i);
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

    private void delete(String id) {
        DeleteReminderInput input = DeleteReminderInput.builder()
                .id(id)
                .build();

        DeleteReminderMutation deleteReminderMutation = DeleteReminderMutation.builder()
                .input(input)
                .build();
        ClientFactory.appSyncClient().mutate(deleteReminderMutation).enqueue(mutateCallback);
    }

    // Mutation callback code
    private GraphQLCall.Callback<DeleteReminderMutation.Data> mutateCallback = new GraphQLCall.Callback<DeleteReminderMutation.Data>() {
        @Override
        public void onResponse(@Nonnull final Response<DeleteReminderMutation.Data> response) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                Toast.makeText(activity, "Deleted reminder", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onFailure(@Nonnull final ApolloException e) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("", "Failed to perform DeleteReminderMutation", e);
                    Toast.makeText(activity, "Failed to delete reminder", Toast.LENGTH_SHORT).show();
                    query();
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

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for(int i = 0; i < mReminders.size(); i++) {
                        String[] Tokens = mReminders.get(i).toString().split("id=|, name=|, day=|, time=");
                        if(!reminder_data.id_exists(Tokens[1])) {
                            reminder_data.removeID(Tokens[1]);
                            reminder_data.removeName(Tokens[2]);
                            reminder_data.removeDay(Tokens[3]);
                            reminder_data.removeTime(Tokens[4].replace("}", ""));
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
