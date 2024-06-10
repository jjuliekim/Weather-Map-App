package com.example.kim_j_project5;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.HashSet;
import java.util.Set;

public class BroadcastReceiverWeather extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null &&
                intent.getAction().equals("com.example.kim_j_project5.background_update")) {
            String data1 = intent.getStringExtra("location1");
            String data2 = intent.getStringExtra("location2");
            String data3 = intent.getStringExtra("location3");
            Log.i("HERE BROADCAST", "data " + data1 + data2 + data3);
            send3Notification(context, data1, data2, data3);

            SharedPreferences sharedPreferences = context.getSharedPreferences("Locations", Context.MODE_PRIVATE);
            Set<String> locations = sharedPreferences.getStringSet("addedLocations", new HashSet<>());
            for (String loc : locations) {
                String addedData = intent.getStringExtra(loc);
                Log.i("HERE BROADCAST", "added: " + addedData);
                sendNotification(context, addedData);
            }
        }
    }

    // send notifs with Notification Manager
    private void send3Notification(Context context, String data1, String data2, String data3) {
        // Check if notifications are enabled for the app
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "weather_channel")
                    .setContentTitle("Weather Update")
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(data1 + "\n" + data2 + "\n" + data3))
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(123, builder.build());
        } else {
            Toast.makeText(context, "Notifications are disabled.", Toast.LENGTH_SHORT).show();
        }
    }

    // send single notification
    private void sendNotification(Context context, String data) {
        // Check if notifications are enabled for the app
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "weather_channel")
                    .setContentTitle("Weather Update")
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(data))
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(123, builder.build());
        } else {
            Toast.makeText(context, "Notifications are disabled.", Toast.LENGTH_SHORT).show();
        }
    }

}