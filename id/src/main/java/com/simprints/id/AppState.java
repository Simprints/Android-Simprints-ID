package com.simprints.id;

import android.content.Intent;

import com.google.android.gms.common.api.GoogleApiClient;
import com.simprints.libcommon.Session;
import com.simprints.libdata.Data;
import com.simprints.libscanner.Scanner;

import java.util.Calendar;


@SuppressWarnings("unused")
public class AppState {

    private static AppState singleton;

    public static AppState getInstance(){
        if (singleton == null) {
            singleton = new AppState();
        }
        return singleton;
    }


    private Scanner scanner;
    private Data data;
    private Session session;
    private GoogleApiClient googleApiClient;
    private int resultCode;
    private Intent resultData;

    protected AppState() {
        scanner = null;
        data = null;
        session = new Session();
        googleApiClient = null;
        Calendar c = Calendar.getInstance();
        session.setStartTime(c.getTime());
        resultCode = 0;
        resultData = null;
    }


    public boolean isEnrol() { return session.isEnrol(); }

    public void setEnrol(boolean enrol) { session.setEnrol(enrol); }

    public String getApiKey() { return session.getApiKey(); }

    public void setApiKey(String apiKey) { session.setApiKey(apiKey); }

    public String getDeviceId() { return session.getDeviceId(); }

    public void setDeviceId(String deviceId) { session.setDeviceId(deviceId); }

    public String getUserId() { return session.getUserId(); }

    public void setUserId(String userId) { session.setUserId(userId); }

    public String getGuid() { return session.getPersonGuid(); }

    public void setGuid(String guid) { session.setPersonGuid(guid); }

    public Scanner getScanner() { return scanner; }

    public void setScanner(Scanner scanner) { this.scanner = scanner; }

    public String getMacAddress() {
        return session.getMacAddress();
    }

    public void setMacAddress(String macAddress) {
        session.setMacAddress(macAddress);
    }

    public Data getData() { return data; }

    public void setData(Data data) { this.data = data; }

    public GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

    public void setGoogleApiClient(GoogleApiClient googleApiClient) {
        this.googleApiClient = googleApiClient;
    }

    public void setLatitude(String latitude) {
        session.setLatitude(latitude);
    }

    public String getLatitude() {
        return session.getLatitude();
    }

    public void setLongitude(String longitude) {
        session.setLongitude(longitude);
    }

    public String getLongitude() {
        return session.getLongitude();
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public Intent getResultData() {
        return resultData;
    }

    public void setResultData(Intent resultData) {
        this.resultData = resultData;
    }

    public Session getReadyToSendSession() {
        Calendar c = Calendar.getInstance();
        session.setEndTime(c.getTime());
        return session;
    }
}
