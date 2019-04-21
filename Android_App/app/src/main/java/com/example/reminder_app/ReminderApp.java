package com.example.reminder_app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.amplify.generated.graphql.ListRemindersQuery;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.ArrayList;

import javax.annotation.Nonnull;

public class ReminderApp extends AppCompatActivity {
    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    private final String TAG = ReminderApp.class.getSimpleName();
    ArrayList mReminders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_app);

        // create reminder list
        reminder_data.reset();
        reminder_data.setList((ListView) findViewById(R.id.reminder_list));
        reminder_list_adapter list_adapter = new reminder_list_adapter(this,
                reminder_data.getNames(), reminder_data.getDays(), reminder_data.getTimes(), ReminderApp.this);
        reminder_data.setAdapter(list_adapter);
        reminder_data.getList().setAdapter(list_adapter);

        // add reminder button
        Button add_reminder_btn = findViewById(R.id.add_reminder_btn);
        add_reminder_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent add_reminder_intent = new Intent(getApplicationContext(), add_reminder.class);
                startActivity(add_reminder_intent);
            }
        });

        // add picture button
        Button add_picture_btn = findViewById(R.id.add_picture_btn);
        add_picture_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // capture picture
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                }
                else
                {
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                }
            }
        });

        // sign out button
        Button sign_out_btn = findViewById(R.id.sign_out_btn);
        sign_out_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for(int i = 0; i < mReminders.size(); i++) {
                        String[] Tokens = mReminders.get(i).toString().split("id=|, name=|, day=|, time=");
                        if(!reminder_data.id_exists(Tokens[1])) {
                            reminder_data.addID(Tokens[1]);
                            reminder_data.addName(Tokens[2]);
                            reminder_data.addDay(Tokens[3]);
                            reminder_data.addTime(Tokens[4].replace("}", ""));
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

    // camera stuff
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
            else
            {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK)
        {
            // acquire photo
            Bitmap photo = (Bitmap) data.getExtras().get("data");

            // send picture to cloud
            // TODO
        }
    }
}
