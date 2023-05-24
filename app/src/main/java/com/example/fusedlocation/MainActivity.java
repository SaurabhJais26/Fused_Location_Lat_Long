package com.example.fusedlocation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Granularity;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    String TAG = "location ";

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private Location lastLocation;

    Context context;

    TextView tv_lat, tv_lon, tv_address;
    Double d_lat, d_long;

    String fetched_address="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_lat=(TextView)findViewById(R.id.tv_lat);
        tv_lon=(TextView)findViewById(R.id.tv_lon);
        tv_address=(TextView) findViewById(R.id.tv_address);

        context=getApplicationContext();

        checkLocationPermission();

        init();
    }
    //======Step 1=======
    //======Location permission code======

    public void checkLocationPermission()
    {
        Log.d(TAG,"inside check location");

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION))
            {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION},1);
            }
            else{
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION},1);
            }
        }
    }

    //=======Step 2========
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case 1:
                if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)
                    {
                        Toast.makeText(this,"Permission Granted..",Toast.LENGTH_SHORT).show();

                        init(); //code for after request for location is granted....//uncomment it later
                    }
                }
                return;
        }
    }
    //=======Step 3========
    @SuppressLint("MissingPermission")
    private void startLocationUpdates()
    {
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(locationSettingsResponse -> {
                    Log.d(TAG,"Location settings are Ok");
                    mFusedLocationClient.requestLocationUpdates(
                            mLocationRequest,mLocationCallback, Looper.myLooper());
                })
                .addOnFailureListener(e -> {
                    int statusCode = ((ApiException) e).getStatusCode();
                    Log.d(TAG, "inside error -> "+ statusCode);
                });
    }

    //=======Step 4========

    public void stopLocationUpdates()
    {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(task -> {Log.d(TAG, "stop location updates");});
    }
    //=======Step 5========
    private void receiveLocation(LocationResult locationResult)
    {
        lastLocation=locationResult.getLastLocation();

        Log.d(TAG,"Latitude: "+ lastLocation.getLatitude());
        Log.d(TAG,"Longitude: "+ lastLocation.getLongitude());
        Log.d(TAG,"Altitude: "+ lastLocation.getAltitude());

        //===== %.6f because after decimal fixed 6 digits...
        String s_lat= String.format(Locale.ROOT,"%.6f",lastLocation.getLatitude());
        String s_lon= String.format(Locale.ROOT,"%.6f",lastLocation.getLongitude());

        d_lat = lastLocation.getLatitude();
        d_long = lastLocation.getLongitude();

        tv_lat.setText(""+s_lat);
        tv_lon.setText(""+s_lon);

        try {
            //code to fetch address from lat long
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses= geocoder.getFromLocation(d_lat,d_long, 1);

            fetched_address=addresses.get(0).getAddressLine(0);

            Log.d(TAG, ""+ fetched_address);
            tv_address.setText(fetched_address+"");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    //=======Step 6========
    public void init()
    {
        mFusedLocationClient= LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient= LocationServices.getSettingsClient(this);
        mLocationCallback= new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                receiveLocation(locationResult);
            }
        };

        mLocationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,5000)
                .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
                .setMinUpdateIntervalMillis(500)
                .setMinUpdateDistanceMeters(1)
                .setWaitForAccurateLocation(true)
                .build();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest=builder.build();
        startLocationUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }
}