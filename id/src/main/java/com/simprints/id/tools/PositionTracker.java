package com.simprints.id.tools;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.Locale;

@SuppressWarnings("UnusedParameters")
public class PositionTracker implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener
{

    private AppState appState;
    private Activity activity;
    private LocationRequest locationRequest;

    public PositionTracker(Activity activity) {
        appState = AppState.getInstance();
        this.activity = activity;
        locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);

        appState.setGoogleApiClient(
                new GoogleApiClient.Builder(activity)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build());
    }

    public void start() {
        appState.getGoogleApiClient().connect();
    }

    public void finish() {
        GoogleApiClient client = appState.getGoogleApiClient();
        if (client != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    appState.getGoogleApiClient(), this);
            if (client.isConnected()) {
                appState.getGoogleApiClient().disconnect();
            }
            appState.setGoogleApiClient(null);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults)
    {
        switch (requestCode) {
            case InternalConstants.LOCATION_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onConnected(null);
                }
                break;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case InternalConstants.RESOLUTION_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    startLocationUpdates();
                }
                break;

            case InternalConstants.GOOGLE_SERVICE_UPDATE_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    appState.getGoogleApiClient().connect();
                }
                break;
        }
    }










    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(activity, "onConnected");
        if (requestPermission()) {
            getLastLocation();
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(activity, String.format(Locale.UK,
                "onConnectionFailed : %s", connectionResult.toString()));

        switch (connectionResult.getErrorCode()) {
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                GoogleApiAvailability.getInstance().getErrorDialog(
                        activity,
                        connectionResult.getErrorCode(),
                        InternalConstants.GOOGLE_SERVICE_UPDATE_REQUEST).show();
        }
    }



    private boolean requestPermission() {
        int locationPermission = ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
        );

        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    InternalConstants.LOCATION_PERMISSION_REQUEST
            );
            Log.d(activity, "requestionPermission() -> false");
            return false;
        } else {
            Log.d(activity, "requestionPermission() -> true");
            return true;
        }

    }

    private void getLastLocation() {
        Log.d(activity, "getLastLocation()");

        int locationPermission = ContextCompat.checkSelfPermission(
                activity, Manifest.permission.ACCESS_FINE_LOCATION
        );

        if (locationPermission == PackageManager.PERMISSION_GRANTED) {
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    appState.getGoogleApiClient());
            if (lastLocation != null) {
                appState.setLatitude(String.valueOf(lastLocation.getLatitude()));
                appState.setLongitude(String.valueOf(lastLocation.getLongitude()));
            }
            Log.d(activity, String.format(Locale.UK, "Last location: %s", lastLocation));
        }
    }

    private void startLocationUpdates() {
        LocationSettingsRequest settingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .build();

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        appState.getGoogleApiClient(),
                        settingsRequest
                );

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {

                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.d(activity, "startLocationUpdates() -> SUCCESS");
                        requestLocationUpdates();
                        break;

                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.d(activity, "startLocationUpdates() -> RESOLUTION");
                        try {
                            status.startResolutionForResult(
                                    activity, InternalConstants.RESOLUTION_REQUEST
                            );
                        } catch (IntentSender.SendIntentException ignored) {
                        }
                        break;

                    default:
                        Log.d(activity, "startLocationUpdates() -> FAILURE");
                }
            }
        });
    }

    private void requestLocationUpdates() {
        Log.d(activity, "requestLocationUpdates()");
        int locationPermission = ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
        );

        if (locationPermission == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    appState.getGoogleApiClient(),
                    locationRequest,
                    this
            );
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            appState.setLatitude(String.valueOf(location.getLatitude()));
            appState.setLongitude(String.valueOf(location.getLongitude()));
            Log.d(activity, String.format(Locale.UK, "onLocationChanged(%f %f)",
                    location.getLatitude(), location.getLongitude()));
        }
    }



}
