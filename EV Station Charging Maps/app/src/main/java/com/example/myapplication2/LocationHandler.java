package com.example.myapplication2;
import android.Manifest;
import android.app.Activity;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import static android.content.Context.LOCATION_SERVICE;

//import android.location.LocationListener;


public class LocationHandler {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationManager locationManager;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private LocationResultListener locationResultListener;

    private Activity activity;

    private int activityRequestCode;
    private int permissionRequestCode;

    private final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private final int GRANTED = PackageManager.PERMISSION_GRANTED;

    public LocationHandler(Activity activity, LocationResultListener locationResultListener,
                           int activityRequestCode, int permissionRequestCode) {
        this.activity = activity;
        this.locationResultListener = locationResultListener;
        this.activityRequestCode = activityRequestCode;
        this.permissionRequestCode = permissionRequestCode;
        initLocationVariables();
    }

    private void initLocationVariables() {
        locationManager = (LocationManager) activity.getSystemService(LOCATION_SERVICE);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity);
        locationRequest = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(0).setFastestInterval(0);
        initLocationCallBack();
    }

    private void initLocationCallBack() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                locationResultListener.getLocation(locationResult.getLastLocation());
                fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            }
        };
    }

    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private boolean isPermissionGranted(Activity activity) {
        return ContextCompat.checkSelfPermission(activity, FINE_LOCATION) == GRANTED &&
                ContextCompat.checkSelfPermission(activity, COARSE_LOCATION) == GRANTED;
    }

    private void requestPermission(Activity activity, int requestCode) {
        String[] permissions = {FINE_LOCATION, COARSE_LOCATION};
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

    private void promptUserToEnableLocation(final int requestCode) {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        LocationServices.getSettingsClient(activity).checkLocationSettings(builder.build()).addOnSuccessListener(locationSettingsResponse -> getLastKnownLocation()).addOnFailureListener(e -> {
                    int status = ((ApiException) e).getStatusCode();
                    switch (status) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(activity, requestCode);
                            } catch (IntentSender.SendIntentException exception) {
                                exception.printStackTrace();
                            }
                            break;
                    }
                });
    }

    public void getUserLocation() {
        if (!isGooglePlayServicesAvailable(activity)) {
            return;
        }
        if (!isPermissionGranted(activity)) {
            requestPermission(activity, permissionRequestCode);
            return;
        }
        if (!isLocationEnabled()) {
            promptUserToEnableLocation(activityRequestCode);
            return;
        }
        getLastKnownLocation();
    }

    @SuppressWarnings("MissingPermission")
    private void getLastKnownLocation() {
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {
            Location location = task.getResult();
            if (location == null) {
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            } else {
                locationResultListener.getLocation(location);
            }
        });
    }

    private boolean isGooglePlayServicesAvailable(Activity activity) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, 1001).show();
            }
            return false;
        }
        return true;
    }
}
