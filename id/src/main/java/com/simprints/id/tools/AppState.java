package com.simprints.id.tools;

import android.support.annotation.NonNull;

import com.google.android.gms.common.api.GoogleApiClient;
import com.simprints.libdata.models.firebase.fb_Session;
import com.simprints.libscanner.Scanner;


@SuppressWarnings("ConstantConditions")
public class AppState {

    private static AppState singleton;

    public synchronized static AppState getInstance() {
        if (singleton == null) {
            singleton = new AppState();
        }
        return singleton;
    }

    public AppState() {
    }

    // Other attributes
    private String macAddress = null;
    private String scannerId = null;
    private short hardwareVersion = -1;

    // Handles on scanner, database, firebase analytics, google api, etc.
    private Scanner scanner = null;
    private fb_Session session = null;
    private GoogleApiClient googleApiClient = null;

    public void setScanner(Scanner scanner) {
        this.scanner = scanner;
    }

    public void setMacAddress(@NonNull String macAddress) {
        this.macAddress = macAddress;
        session.saveMacAddress(macAddress);
    }

    public void setHardwareVersion(short hardwareVersion) {
        this.hardwareVersion = hardwareVersion;
        session.saveHardwareVersion(hardwareVersion);
    }

    public void setScannerId(@NonNull String scannerId) {
        this.scannerId = scannerId;
        session.saveScannerId(scannerId);
    }

    public void setPosition(@NonNull String latitude, @NonNull String longitude) {
        session.savePosition(latitude, longitude);
    }

    public void logLoadEnd() {
        session.logLoadEndTime();
    }

    public void logMainStart() {
        session.logMainStartTime();
    }

    public void logMatchStart() {
        session.logMatchStartTime();
    }

    public void logSessionEnd() {
        session.logSessionEndTime();
    }

    void setGoogleApiClient(GoogleApiClient googleApiClient) {
        this.googleApiClient = googleApiClient;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public short getHardwareVersion() {
        return hardwareVersion;
    }

    public String getScannerId() {
        return scannerId;
    }

    public Scanner getScanner() {
        return scanner;
    }

    GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

    public void destroy() {
        singleton = null;
    }

}
