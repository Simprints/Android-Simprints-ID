package com.simprints.id.tools;

import android.app.Activity;
import android.content.Context;

import com.google.firebase.analytics.FirebaseAnalytics;

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

}
