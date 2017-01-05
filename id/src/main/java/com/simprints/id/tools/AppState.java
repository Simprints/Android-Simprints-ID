package com.simprints.id.tools;

import com.google.android.gms.common.api.GoogleApiClient;
import com.simprints.libcommon.Session;
import com.simprints.libdata.DatabaseContext;
import com.simprints.libscanner.Scanner;

import java.util.Calendar;


@SuppressWarnings("unused")
public class AppState {

    private static AppState singleton;

    public synchronized static AppState getInstance() {
        if (singleton == null) {
            singleton = new AppState();
        }
        return singleton;
    }

    private Scanner scanner;
    private DatabaseContext data;
    private Session session;
    private GoogleApiClient googleApiClient;
    private boolean signedIn;
    private boolean connected;
    private String callingPackage;

    private AppState() {
        scanner = null;
        data = null;
        session = new Session();
        googleApiClient = null;
        Calendar c = Calendar.getInstance();
        session.setStartTime(c.getTime());
    }

    public synchronized boolean isEnrol() {
        return session.isEnrol();
    }

    public synchronized void setEnrol(boolean enrol) {
        session.setEnrol(enrol);
    }

    public synchronized String getApiKey() {
        return session.getApiKey();
    }

    public synchronized void setApiKey(String apiKey) {
        session.setApiKey(apiKey);
    }

    public synchronized String getDeviceId() {
        return session.getDeviceId();
    }

    public synchronized void setDeviceId(String deviceId) {
        session.setDeviceId(deviceId);
    }

    public synchronized String getUserId() {
        return session.getUserId();
    }

    public synchronized void setUserId(String userId) {
        session.setUserId(userId);
    }

    public synchronized String getGuid() {
        return session.getPersonGuid();
    }

    public synchronized void setGuid(String guid) {
        session.setPersonGuid(guid);
    }

    public synchronized Scanner getScanner() {
        return scanner;
    }

    public synchronized void setScanner(Scanner scanner) {
        this.scanner = scanner;
    }

    public synchronized String getMacAddress() {
        return session.getMacAddress();
    }

    public synchronized void setMacAddress(String macAddress) {
        session.setMacAddress(macAddress);
    }

    public synchronized DatabaseContext getData() {
        return data;
    }

    public synchronized void setData(DatabaseContext data) {
        this.data = data;
    }

    public synchronized GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

    public synchronized void setGoogleApiClient(GoogleApiClient googleApiClient) {
        this.googleApiClient = googleApiClient;
    }

    public synchronized void setLatitude(String latitude) {
        session.setLatitude(latitude);
    }

    public synchronized String getLatitude() {
        return session.getLatitude();
    }

    public synchronized void setLongitude(String longitude) {
        session.setLongitude(longitude);
    }

    public synchronized String getLongitude() {
        return session.getLongitude();
    }

    public synchronized Session getReadyToSendSession() {
        Calendar c = Calendar.getInstance();
        session.setEndTime(c.getTime());
        return session;
    }

    public synchronized void setHardwareVersion(short hardwareVersion) {
        session.setHardwareVersion(hardwareVersion);
    }

    public synchronized short getHardwareVersion() {
        return session.getHardwareVersion();
    }

    public synchronized boolean getSignedIn() {
        return this.signedIn;
    }

    public synchronized void setSignedIn(boolean signedIn) {
        this.signedIn = signedIn;
    }

    public synchronized boolean getSConnected() {
        return this.connected;
    }

    public synchronized void setConnected(boolean connected) {
        this.connected = connected;
    }

    public synchronized void setCallingPackage(String callingPackage) {
        this.callingPackage = callingPackage;
    }

    public synchronized String getCallingPackage() {
        return this.callingPackage;
    }
}
