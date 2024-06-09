package com.example.kim_j_project5;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class BroadcastReceiverWeather extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null &&
                intent.getAction().equals("com.example.kim_j_project5.background_update")) {
            // Retrieve weather information from the received intent
             String data1 = intent.getStringExtra("location1");
             String data2 = intent.getStringExtra("location2");
             String data3 = intent.getStringExtra("location3");

            // Send notification using Notification Manager with weather information
            sendNotification(context, data1, data2, data3);
        }
    }

    private void sendNotification(Context context, String data1, String data2, String data3) {
        // Check if notifications are enabled for the app
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "weather_channel")
                    .setContentTitle("Weather Update")
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText("Location 1: " + data1 + "\n" +
                                    "Location 2: " + data2 + "\n" +
                                    "Location 3: " + data3))
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(123, builder.build());
        } else {
            Toast.makeText(context, "Notifications are disabled.", Toast.LENGTH_SHORT).show();
        }
    }

}