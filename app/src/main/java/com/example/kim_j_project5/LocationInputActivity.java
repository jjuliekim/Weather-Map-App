package com.example.kim_j_project5;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class LocationInputActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private EditText locationEditText;
    private FusedLocationProviderClient fusedLocationClient;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_input);

        locationEditText = findViewById(R.id.enter_loc_text);
        Button checkWeatherButton = findViewById(R.id.check_weather_button);
        Button getCurrLocButton = findViewById(R.id.curr_loc_button);
        Button addLocationButton = findViewById(R.id.add_loc_button);

        // location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        requestLocationPermission();

        sharedPreferences = getSharedPreferences("Locations", MODE_PRIVATE);
        updateLastSearchedButtons();

        // update map marker from inputted location
        locationEditText.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO || event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                String location = locationEditText.getText().toString();
                if (!location.isEmpty()) {
                    getLatLngFromLocation(location);
                    saveLastSearched(location);
                }
                return true;
            }
            return false;
        });

        // go to weather data activity with inputted location
        checkWeatherButton.setOnClickListener(v -> {
            String location = locationEditText.getText().toString();
            if (location.isEmpty()) {
                Toast.makeText(this, "Enter Valid Location", Toast.LENGTH_SHORT).show();
                return;
            }
            saveLastSearched(location);
            Intent nextIntent = new Intent(LocationInputActivity.this, WeatherDataActivity.class);
            nextIntent.putExtra("location", location);
            startActivity(nextIntent);
        });

        // get current location and get weather data
        getCurrLocButton.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null) {
                        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        updateMapLocation(currentLatLng);
                        getCityNameFromLatLng(currentLatLng);
                    } else {
                        Toast.makeText(LocationInputActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                requestLocationPermission();
            }
        });

        // add location button actions
        addLocationButton.setOnClickListener(v -> showAddLocationDialog());

    }

    // request location permissions
    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            getDeviceLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                    getDeviceLocation();
                }
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
            return;
        }
        mMap.setMyLocationEnabled(true);
        getDeviceLocation();
    }

    // set map marker with user's current location (as default)
    private void getDeviceLocation() {
        try {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Task<Location> locationResult = fusedLocationClient.getLastLocation();
                locationResult.addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        updateMapLocation(currentLatLng);
                    } else {
                        Log.i("HERE LOCATION INPUT", "unable to get device location");
                    }
                });
            }
        } catch (SecurityException e) {
            Log.i("HERE LOCATION INPUT", "e get location: " + e);
        }
    }

    // get coords from inputted location to mark on map
    private void getLatLngFromLocation(String location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(location, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                updateMapLocation(latLng);
                saveLastSearched(location);
            } else {
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.i("HERE LOCATION INPUT", "e get lat lng from loc: " + e);
            Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show();
        }
    }

    // get city name from LatLng and start WeatherDataActivity with city name
    private void getCityNameFromLatLng(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String cityName = address.getLocality();
                if (!cityName.isEmpty()) {
                    saveLastSearched(cityName);
                    Intent nextIntent = new Intent(LocationInputActivity.this, WeatherDataActivity.class);
                    nextIntent.putExtra("location", cityName);
                    startActivity(nextIntent);
                }
            }
        } catch (IOException e) {
            Log.i("LOCATION INPUT", "Exception getting city name from lat lng: " + e);
            Toast.makeText(this, "Unable to get city name", Toast.LENGTH_SHORT).show();
        }
    }

    // clear map and update with marker
    private void updateMapLocation(LatLng latLng) {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
    }

    // save searched locations with shared preferences
    private void saveLastSearched(String location) {
        Log.i("HERE LOCATION INPUT", "location: " + location);
        String lastLocation1 = sharedPreferences.getString("lastLocation1", "--");
        String lastLocation2 = sharedPreferences.getString("lastLocation2", "--");
        String lastLocation3 = sharedPreferences.getString("lastLocation3", "--");
        if (!lastLocation1.equals(location) && !lastLocation2.equals(location) && !lastLocation3.equals(location)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("lastLocation3", lastLocation2);
            editor.putString("lastLocation2", lastLocation1);
            editor.putString("lastLocation1", location);
            Log.i("HERE LOCATION INPUT", "loc 1: " + lastLocation1);
            Log.i("HERE LOCATION INPUT", "loc 2: " + lastLocation2);
            Log.i("HERE LOCATION INPUT", "loc 3: " + lastLocation3);
            editor.apply();
            updateLastSearchedButtons();
        }
    }

    // display last searched locations
    private void updateLastSearchedButtons() {
        String lastLocation1 = sharedPreferences.getString("lastLocation1", "--");
        String lastLocation2 = sharedPreferences.getString("lastLocation2", "--");
        Button savedLoc1Button = findViewById(R.id.saved_loc_1);
        Button savedLoc2Button = findViewById(R.id.saved_loc_2);
        savedLoc1Button.setText(lastLocation1);
        savedLoc2Button.setText(lastLocation2);

        // send location to weather data activity
        savedLoc1Button.setOnClickListener(v -> {
            if (!lastLocation1.equals("--")) {
                saveLastSearched(lastLocation1);
                Intent nextIntent = new Intent(LocationInputActivity.this, WeatherDataActivity.class);
                nextIntent.putExtra("location", lastLocation1);
                startActivity(nextIntent);
            }
        });

        savedLoc2Button.setOnClickListener(v -> {
            if (!lastLocation2.equals("--")) {
                saveLastSearched(lastLocation2);
                Intent nextIntent = new Intent(LocationInputActivity.this, WeatherDataActivity.class);
                nextIntent.putExtra("location", lastLocation2);
                startActivity(nextIntent);
            }
        });
    }

    private void showAddLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_location, null);
        builder.setView(dialogView);
        EditText editTextDialogLocation = dialogView.findViewById(R.id.addLocationText);
        Button buttonDialogAddLocation = dialogView.findViewById(R.id.addNotifsButton);
        AlertDialog dialog = builder.create();

        buttonDialogAddLocation.setOnClickListener(v -> {
            String location = editTextDialogLocation.getText().toString();
            if (!location.isEmpty()) {
                // add to background service/receiver
                dialog.dismiss();
                Toast.makeText(LocationInputActivity.this, "Location Added", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(LocationInputActivity.this, "Enter Location", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }

}