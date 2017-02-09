package com.simprints.id.tools;

import android.support.annotation.Nullable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.simprints.libcommon.RefusalForm;
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
    private String appKey;
    private RefusalForm refusalForm;

    private AppState() {
        scanner = null;
        refusalForm = null;
        data = null;
        session = new Session();
        googleApiClient = null;
        Calendar c = Calendar.getInstance();
        session.setStartTime(c.getTime());
    }

    public boolean isEnrol() {
        return session.isEnrol();
    }

    public void setEnrol(boolean enrol) {
        session.setEnrol(enrol);
    }

    public String getApiKey() {
        return session.getApiKey();
    }

    public void setApiKey(String apiKey) {
        session.setApiKey(apiKey);
    }

    public String getDeviceId() {
        return session.getDeviceId();
    }

    public void setDeviceId(String deviceId) {
        session.setDeviceId(deviceId);
    }

    public String getUserId() {
        return session.getUserId();
    }

    public void setUserId(String userId) {
        session.setUserId(userId);
    }

    public String getGuid() {
        return session.getPersonGuid();
    }

    public void setGuid(String guid) {
        session.setPersonGuid(guid);
    }

    public Scanner getScanner() {
        return scanner;
    }

    public void setScanner(Scanner scanner) {
        this.scanner = scanner;
    }

    public String getMacAddress() {
        return session.getMacAddress();
    }

    public void setMacAddress(String macAddress) {
        session.setMacAddress(macAddress);
    }

    public DatabaseContext getData() {
        return data;
    }

    public void setData(DatabaseContext data) {
        this.data = data;
    }

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

    public Session getReadyToSendSession() {
        Calendar c = Calendar.getInstance();
        session.setEndTime(c.getTime());
        return session;
    }

    public void setHardwareVersion(short hardwareVersion) {
        session.setHardwareVersion(hardwareVersion);
    }

    public short getHardwareVersion() {
        return session.getHardwareVersion();
    }

    public boolean getSignedIn() {
        return this.signedIn;
    }

    public void setSignedIn(boolean signedIn) {
        this.signedIn = signedIn;
    }

    public boolean isConnected() {
        return this.connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public void setCallingPackage(String callingPackage) {
        this.callingPackage = callingPackage;
    }

    public String getCallingPackage() {
        return this.callingPackage;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getAppKey() {
        return this.appKey;
    }

    public void setRefusalForm(RefusalForm refusalForm) {
        this.refusalForm = refusalForm;
    }

    @Nullable
    public RefusalForm getRefusalForm() {
        return this.refusalForm;
    }

    public void destroy() {
        singleton = null;
    }
}
