package com.example.kim_j_project5;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class BackgroundWeatherService extends Service {
    private final int fetchInterval = 60 * 1000; // 15 minutes (testing with 1 min)
    private String apiKey = "7952fc9a03ecf59677b07feb65d3b189";
    private String baseURL = "https://api.openweathermap.org/data/2.5/weather?units=imperial&";
    private SharedPreferences sharedPreferences;
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // fetch weather info for the 3 recent locations in shared prefs
            fetchWeatherInfo();
            handler.postDelayed(this, fetchInterval);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = getSharedPreferences("Locations", MODE_PRIVATE);
        handler.postDelayed(runnable, fetchInterval);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void fetchWeatherInfo() {
        String loc1 = sharedPreferences.getString("location1", null);
        String loc2 = sharedPreferences.getString("location2", null);
        String loc3 = sharedPreferences.getString("location3", null);
        Intent weatherIntent = new Intent("com.example.kim_j_project5.background_update");
        getData(loc1, "location1", weatherIntent);
        getData(loc2, "location2", weatherIntent);
        getData(loc3, "location3", weatherIntent);
        sendBroadcast(weatherIntent);

        Set<String> locationsSet = sharedPreferences.getStringSet("locationsSet", new HashSet<>());
        for (String loc : locationsSet) {
            Intent weatherIntent2 = new Intent("com.example.kim_j_project5.background_update");
            getData(loc, "location" + loc.hashCode(), weatherIntent2);
            sendBroadcast(weatherIntent2);
        }
    }

    // get data from current weather api call
    private void getData(String location, String locationKey, Intent weatherIntent) {
        LatLng locationLatLng = getLatLngFromLocation(location);
        String restURL = baseURL + "lat=" + locationLatLng.latitude + "&lon=" + locationLatLng.longitude + "&appid=" + apiKey;
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected()){
            new DownloadDataTask(location, locationKey, weatherIntent).execute(restURL);
        } else {
            Toast.makeText(this, "Network Connection Unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    // get coords from inputted location
    private LatLng getLatLngFromLocation(String loc) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(loc, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                return new LatLng(address.getLatitude(), address.getLongitude());
            } else {
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.i("HERE BG WEATHER", "e get lat lng from loc: " + e);
            Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    private class DownloadDataTask extends AsyncTask<String, Void, String> {
        private String location;
        private String locationKey;
        private Intent weatherIntent;
        public DownloadDataTask(String location, String locationKey, Intent weatherIntent) {
            this.location = location;
            this.locationKey = locationKey;
            this.weatherIntent = weatherIntent;
        }

        @Override
        protected void onPostExecute(String response) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONObject main = jsonObject.getJSONObject("main");
                double temp = main.getDouble("temp");
                String description = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description");

                weatherIntent.putExtra(locationKey, location + ": " + temp + "°F " + description);
            } catch (Exception e) {
                Log.e("HERE BG WEATHER JSON Parsing", "Error parsing JSON", e);
            }
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                Log.e("HERE BG WEATHER DownloadDataTask", "Error", e);
                return null;
            }
        }
    }
}
