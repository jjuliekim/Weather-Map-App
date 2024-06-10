package com.example.kim_j_project5;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

            if (data1 == null) {
                data1 = "";
            }
            if (data2 == null) {
                data2 = "";
            }
            if (data3 == null) {
                data3 = "";
            }

            sendNotification(context, data1, data2, data3);

            SharedPreferences sharedPreferences = context.getSharedPreferences("Locations", Context.MODE_PRIVATE);
            Set<String> locationsSet = sharedPreferences.getStringSet("locationsSet", new HashSet<>());
            for (String loc : locationsSet) {
                sendNotification(context, intent.getStringExtra("location" + loc.hashCode()), "", "");
            }
        }
    }

    // send notifs with Notification Manager
    private void sendNotification(Context context, String data1, String data2, String data3) {
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

}