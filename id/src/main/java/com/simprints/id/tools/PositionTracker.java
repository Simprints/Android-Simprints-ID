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
import com.simprints.id.Application;
import com.simprints.id.data.analytics.eventData.SessionEventsManager;
import com.simprints.id.data.analytics.eventData.models.session.SessionEvents;
import com.simprints.id.data.prefs.PreferencesManager;

import java.util.Locale;

import javax.inject.Inject;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

@SuppressWarnings("UnusedParameters")
public class PositionTracker implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener
{

    // TODO: make sure that the concurrent reads and writes on this client are safe
    private GoogleApiClient googleApiClient;
    private Activity activity;
    private PreferencesManager preferencesManager;
    private LocationRequest locationRequest;

    @Inject SessionEventsManager sessionEventsManager;

    public PositionTracker(Activity activity, PreferencesManager preferencesManager) {
        this.activity = activity;
        ((Application) activity.getApplicationContext()).getComponent().inject(this);

        this.preferencesManager = preferencesManager;
        locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
        googleApiClient = new GoogleApiClient.Builder(activity)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build();
    }

    public void start() {
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    public void finish() {
        if (googleApiClient != null) {
            if (googleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
                googleApiClient.disconnect();
            }
            googleApiClient = null;
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
                if (resultCode == Activity.RESULT_OK && googleApiClient != null) {
                    googleApiClient.connect();
                }
                break;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.INSTANCE.d(activity, "PositionTracker.onConnected");
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
        Log.INSTANCE.d(activity, String.format(Locale.UK,
                "PositionTracker.onConnectionFailed : %s", connectionResult.toString()));

        switch (connectionResult.getErrorCode()) {
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                if (!activity.isFinishing()) {
                    GoogleApiAvailability.getInstance().getErrorDialog(
                            activity,
                            connectionResult.getErrorCode(),
                            InternalConstants.GOOGLE_SERVICE_UPDATE_REQUEST).show();
                }
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
            Log.INSTANCE.d(activity, "PositionTracker.requestionPermission() -> false");
            return false;
        } else {
            Log.INSTANCE.d(activity, "PositionTracker.requestionPermission() -> true");
            return true;
        }

    }

    private void getLastLocation() {
        Log.INSTANCE.d(activity, "PositionTracker.getLastLocation()");

        int locationPermission = ContextCompat.checkSelfPermission(
                activity, Manifest.permission.ACCESS_FINE_LOCATION
        );

        if (locationPermission == PackageManager.PERMISSION_GRANTED && googleApiClient != null)
        {
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (lastLocation != null) {
                preferencesManager.setLocation(com.simprints.id.domain.Location.Companion.fromAndroidLocation(lastLocation));
            }
            Log.INSTANCE.d(activity, String.format(Locale.UK, "Last location: %s", lastLocation));
        }
    }

    private void startLocationUpdates() {
        LocationSettingsRequest settingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .build();

        if (googleApiClient != null) {
            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient,settingsRequest);

            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                    Status status = locationSettingsResult.getStatus();
                    switch (status.getStatusCode()) {

                        case LocationSettingsStatusCodes.SUCCESS:
                            Log.INSTANCE.d(activity, "PositionTracker.startLocationUpdates() -> SUCCESS");
                            requestLocationUpdates();
                            break;

                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            Log.INSTANCE.d(activity, "PositionTracker.startLocationUpdates() -> RESOLUTION");
                            try {
                                status.startResolutionForResult(
                                        activity, InternalConstants.RESOLUTION_REQUEST
                                );
                            } catch (IntentSender.SendIntentException ignored) {
                            }
                            break;

                        default:
                            Log.INSTANCE.d(activity, "PositionTracker.startLocationUpdates() -> FAILURE");
                    }
                }
            });
        }
    }

    private void requestLocationUpdates() {
        Log.INSTANCE.d(activity, "PositionTracker.requestLocationUpdates()");
        int locationPermission = ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
        );

        if (locationPermission == PackageManager.PERMISSION_GRANTED && googleApiClient != null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,
                    locationRequest,this
            );
        }
    }

    @Override
    public void onLocationChanged(final Location location) {
        if (location != null) {

            preferencesManager.setLocation(com.simprints.id.domain.Location.Companion.fromAndroidLocation(location));
            sessionEventsManager.addLocationToSession(location.getLatitude(), location.getLongitude());

            Log.INSTANCE.d(activity, String.format(Locale.UK, "PositionTracker.onLocationChanged(%f %f)",
                    location.getLatitude(), location.getLongitude()));

            if(location.hasAccuracy() && location.getAccuracy() < 100) {
                finish();
            }
        }
    }

}
