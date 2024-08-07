package com.coopbankoromia.postracer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = "MainActivity";
    private FusedLocationProviderClient fusedLocationClient;

    private TextView latValue;
    private TextView lonValue;
    private TextView locValue;
    private TextView lastUpdateValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        latValue = findViewById(R.id.lat_value);
        lonValue = findViewById(R.id.lon_value);
        locValue = findViewById(R.id.loc_value);
        lastUpdateValue = findViewById(R.id.last_update_value);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLastLocation();
        }
    }

    private void getLastLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Task<Location> locationTask = fusedLocationClient.getLastLocation();
            locationTask.addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        latValue.setText(String.format(Locale.getDefault(), "%.6f", latitude));
                        lonValue.setText(String.format(Locale.getDefault(), "%.6f", longitude));

                        // Get the address from the location
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                            if (addresses != null && !addresses.isEmpty()) {
                                Address address = addresses.get(0);
                                String addressText = address.getAddressLine(0);
                                locValue.setText(addressText);
                            } else {
                                locValue.setText("Address not found");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            locValue.setText("Geocoding failed");
                        }

                        // Update last update time
                        lastUpdateValue.setText(java.time.LocalDateTime.now().toString());
                    } else {
                        Log.d(TAG, "Location is null");
                    }
                }
            }).addOnFailureListener(e -> {
                Log.d(TAG, "Failed to get location", e);
                Toast.makeText(MainActivity.this, "Failed to get location", Toast.LENGTH_SHORT).show();
            });
        } else {
            Log.d(TAG, "Location permission not granted");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Log.d(TAG, "Location permission denied");
                Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
