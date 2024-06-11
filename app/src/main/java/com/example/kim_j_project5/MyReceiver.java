package com.example.kim_j_project5;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.HashSet;
import java.util.Set;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("HERE BROADCAST", "here");
        if (intent != null && intent.getAction() != null &&
                intent.getAction().equals("com.example.kim_j_project5_update")) {
            String data1 = intent.getStringExtra("location1");
            String data2 = intent.getStringExtra("location2");
            String data3 = intent.getStringExtra("location3");
            Log.i("HERE BROADCAST", "data " + data1 + data2 + data3);
            send3Notification(context, data1, data2, data3);

            SharedPreferences sharedPreferences = context.getSharedPreferences("Locations", Context.MODE_PRIVATE);
            Set<String> locations = sharedPreferences.getStringSet("addedLocations", new HashSet<>());
            for (String loc : locations) {
                String addedData = intent.getStringExtra(loc);
                Log.i("HERE BROADCAST", "added loc: " + addedData);
                sendNotification(context, addedData);
            }

        }
    }

    // send notifs with Notification Manager
    private void send3Notification(Context context, String data1, String data2, String data3) {
        createNotifChannel(context);
        // Check if notifications are enabled for the app
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "weather_channel")
                    .setContentTitle("Weather Update")
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(data1 + "\n" + data2 + "\n" + data3))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            Log.i("HERE BROADCAST", "triple notification built");
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(1, builder.build());
        } else {
            Toast.makeText(context, "Notifications are disabled.", Toast.LENGTH_SHORT).show();
        }
    }

    // send single notification
    private void sendNotification(Context context, String data) {
        createNotifChannel(context);
        // Check if notifications are enabled for the app
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "weather_channel")
                    .setContentTitle("Weather Update")
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(data))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            Log.i("HERE BROADCAST", "single notification built");
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(1, builder.build());
        } else {
            Toast.makeText(context, "Notifications are disabled.", Toast.LENGTH_SHORT).show();
        }
    }

    private void createNotifChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Weather Channel";
            String description = "Channel for weather updates";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("weather_channel", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}