package com.simprints.id.tools;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;
import com.simprints.id.activities.ALERT_TYPE;

public class Analytics {

    private static Analytics singleton;

    public synchronized static Analytics getInstance(Context context) {
        if (singleton == null) {
            singleton = new Analytics(context);
        }
        return singleton;
    }

    private FirebaseAnalytics firebaseAnalytics;

    private Analytics(Context context) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        firebaseAnalytics.setAnalyticsCollectionEnabled(true);
    }

    public void setActivity(Activity activity, String screenName) {
        firebaseAnalytics.setCurrentScreen(activity, screenName, null);
    }

    public void setUser(String userId, String apiKey) {
        firebaseAnalytics.setUserId(userId);
        firebaseAnalytics.setUserProperty("API Key", apiKey);
    }

    public void setDeviceId(String deviceId) {
        firebaseAnalytics.setUserProperty("Device ID", deviceId);
    }

    public void setScannerMac(String scannerMac) {
        firebaseAnalytics.setUserProperty("Scanner ID", scannerMac);
    }

    public void setAlert(ALERT_TYPE alertType, Boolean retry) {
        FirebaseCrash.log(alertType.name());

        Bundle bundle = new Bundle();
        bundle.putString("alert_name", alertType.name());
        bundle.putString("retry", retry.toString());
        firebaseAnalytics.logEvent("alert", bundle);
    }
}
