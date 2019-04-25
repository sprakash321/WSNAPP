package com.example.reminder_app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.amplify.generated.graphql.CreatePicturesMutation;
import com.amazonaws.amplify.generated.graphql.CreateReminderMutation;
import com.amazonaws.amplify.generated.graphql.ListRemindersQuery;
import com.amazonaws.amplify.generated.graphql.OnCreateReminderSubscription;
import com.amazonaws.amplify.generated.graphql.OnUpdateReminderSubscription;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.SignInUIOptions;
import com.amazonaws.mobile.client.SignOutOptions;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.AppSyncSubscriptionCall;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import javax.annotation.Nonnull;

import type.CreatePicturesInput;

public class ReminderApp extends AppCompatActivity {
    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    private final String TAG = ReminderApp.class.getSimpleName();
    ArrayList mReminders;
    reminder_list_adapter list_adapter;

    Semaphore flag = new Semaphore(1);
    int valid = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_app);

        // create reminder list
        reminder_data.reset();
        reminder_data.setList((ListView) findViewById(R.id.reminder_list));
        list_adapter = new reminder_list_adapter(this,
                reminder_data.getIDs(), reminder_data.getNames(), reminder_data.getDays(),
                reminder_data.getStartTimes(), reminder_data.getEndTimes(), ReminderApp.this);
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
                try {
                    AWSMobileClient.getInstance().signOut();

                    reminder_data.reset();
                    reminder_data.getAdapter().reset();

                    finish();
                    valid = 0;
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
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

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        reminder_data.getAdapter().notifyDataSetChanged();
                        flag.acquire();

                        if(valid == 0) {
                            valid = 1;
                        } else {
                            for (int i = 0; i < mReminders.size(); i++) {
                                String[] Tokens = mReminders.get(i).toString().split("id=|, name=|, day=|, start_time=|, end_time=");
                                if (!reminder_data.id_exists(Tokens[1])) {
                                    reminder_data.addID(Tokens[1]);
                                    reminder_data.addName(Tokens[2]);
                                    reminder_data.addDay(Tokens[3]);
                                    reminder_data.addStartTime(Tokens[4]);
                                    reminder_data.addEndTime(Tokens[5].replace("}", ""));
                                }
                            }
                            reminder_data.getAdapter().notifyDataSetChanged();
                        }
                    }
                    catch(Exception e) { }
                    flag.release();
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
            ArrayList<Integer> photo_array = new ArrayList<>();
            for(int ii = 0; ii < photo.getWidth(); ii++) {
                for(int jj =0; jj < photo.getHeight(); jj++) {
                    photo_array.add(photo.getPixel(ii,jj));
                }
            }

            int width = photo.getWidth(); // 121
            int height = photo.getHeight(); // 162


            // send picture to cloud
            CreatePicturesInput input = CreatePicturesInput.builder()
                    .name(AWSMobileClient.getInstance().getIdentityId())
                    .picture(photo_array)
                    .build();

            CreatePicturesMutation addPictureMutation = CreatePicturesMutation.builder()
                    .input(input)
                    .build();

            ClientFactory.appSyncClient().mutate(addPictureMutation).enqueue(mutateCallback);
        }
    }

    // Mutation callback code
    private GraphQLCall.Callback<CreatePicturesMutation.Data> mutateCallback = new GraphQLCall.Callback<CreatePicturesMutation.Data>() {
        @Override
        public void onResponse(@Nonnull final Response<CreatePicturesMutation.Data> response) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ReminderApp.this, "Added picture", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onFailure(@Nonnull final ApolloException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("", "Failed to perform AddPicturesInput", e);
                    Toast.makeText(ReminderApp.this, "Failed to add picture", Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

}
