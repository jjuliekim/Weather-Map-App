package com.example.kim_j_project5;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeekForecastActivity extends AppCompatActivity {
    private String apiKey = "7952fc9a03ecf59677b07feb65d3b189";
    private String baseURL = "https://api.openweathermap.org/data/2.5/forecast?units=imperial&";
    private String location;
    ForecastAdapter adapter;
    List<ForecastDetails> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_week_forecast);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Intent myIntent = getIntent();
        location = myIntent.getStringExtra("location");

        ListView listView = findViewById(R.id.listView);
        items = new ArrayList<>();
        getData();
        adapter = new ForecastAdapter(this, items);
        listView.setAdapter(adapter);
    }

    // get json data from api call
    private void getData() {
        LatLng locationLatLng = getLatLngFromLocation(location);
        String restURL = baseURL + "lat=" + locationLatLng.latitude + "&lon=" + locationLatLng.longitude + "&appid=" + apiKey;
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected()){
            new DownloadDataTask().execute(restURL);
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
            Log.i("HERE WEEK FORECAST", "e get lat lng from loc: " + e);
            Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    private class DownloadDataTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPostExecute(String response) {
            if (response == null) {
                Toast.makeText(WeekForecastActivity.this, "Error Retrieving Data", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat output = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault());
                JSONObject jsonObject = new JSONObject(response);
                // parse daily details
                JSONArray list = jsonObject.getJSONArray("list");
                for (int i = 0; i < list.length(); i++) {
                    JSONObject forecast = list.getJSONObject(i);
                    String dateString = forecast.getString("dt_txt");
                    Date date = input.parse(dateString);
                    String formattedDate = output.format(date);
                    JSONObject main = forecast.getJSONObject("main");
                    double highestTemp = main.getDouble("temp_max");
                    double lowestTemp = main.getDouble("temp_min");
                    double precipitation = forecast.has("pop") ? forecast.getDouble("pop") : 0;

                    ForecastDetails details = new ForecastDetails(formattedDate, lowestTemp, highestTemp, precipitation);
                    items.add(details);
                }
                adapter.notifyDataSetChanged();
            } catch (Exception e) {
                Log.e("HERE WEEK JSON Parsing", "Error parsing JSON", e);
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
                Log.e("HERE WEEK DownloadDataTask", "Error", e);
                return null;
            }
        }
    }


}