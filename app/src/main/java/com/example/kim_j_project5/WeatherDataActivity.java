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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import org.json.JSONObject;

public class WeatherDataActivity extends AppCompatActivity {
    private TextView temperatureText;
    private TextView feelsLikeText;
    private TextView windSpeedText;
    private TextView humidityText;
    private TextView descriptionText;
    private String apiKey = "7952fc9a03ecf59677b07feb65d3b189";
    private String baseURL = "https://api.openweathermap.org/data/2.5/weather?";
    private String location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_weather_data);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Intent myIntent = getIntent();
        location = myIntent.getStringExtra("location");

        // set text
        TextView locationText = findViewById(R.id.location_text);
        locationText.setText(String.format("Weather at %s", location));

        temperatureText = findViewById(R.id.temp_text);
        feelsLikeText = findViewById(R.id.feels_like_text);
        windSpeedText = findViewById(R.id.wind_speed_text);
        humidityText = findViewById(R.id.humidity_text);
        descriptionText = findViewById(R.id.description_text);

        Button forecastButton = findViewById(R.id.forecast_button);
//        forecastButton.setOnClickListener(v -> );

        getData();
    }

    // get data from current weather api call
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
            Log.i("HERE LOCATION INPUT", "e get lat lng from loc: " + e);
            Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    private class DownloadDataTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPostExecute(String response) {
            if (response == null) {
                Toast.makeText(WeatherDataActivity.this, "Error Retrieving Data", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                JSONObject jsonObject = new JSONObject(response);
                // parse temp details
                JSONObject main = jsonObject.getJSONObject("main");
                double temp = main.getDouble("temp");
                double feelsLike = main.getDouble("feels_like");
                int humidity = main.getInt("humidity");
                // parse wind details
                JSONObject wind = jsonObject.getJSONObject("wind");
                double windSpeed = wind.getDouble("speed");
                // parse weather condition details
                String description = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description");

                temperatureText.setText(String.format("Temperature: %s°F", temp));
                feelsLikeText.setText(String.format("Feels Like: %s°F", feelsLike));
                humidityText.setText(String.format("Humidity: %s%%", humidity));
                windSpeedText.setText(String.format("Wind Speed: %s mph", windSpeed));
                descriptionText.setText(String.format("Condition: %s", description));
            } catch (Exception e) {
                Log.e("JSON Parsing", "Error parsing JSON", e);
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
                Log.e("DownloadDataTask", "Error", e);
                return null;
            }
        }
    }

}