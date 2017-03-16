package com.simprints.id.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.simprints.id.model.ALERT_TYPE;
import com.simprints.id.model.Callout;
import com.simprints.libcommon.RefusalForm;
import com.simprints.libcommon.Session;
import com.simprints.libdata.DatabaseContext;
import com.simprints.libscanner.Scanner;
import com.simprints.libsimprints.Constants;
import com.simprints.libsimprints.Metadata;

import java.util.Calendar;
import java.util.UUID;


@SuppressWarnings("ConstantConditions")
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
    private Analytics analytics;
    private boolean signedIn;
    private String callingPackage;
    private RefusalForm refusalForm;
    private Callout callout;

    private AppState() {
        scanner = null;
        data = null;
        session = new Session();
        googleApiClient = null;
        analytics = null;
        refusalForm = null;
        callout = null;
        Calendar c = Calendar.getInstance();
        session.setStartTime(c.getTime());
    }

    @SuppressLint("HardwareIds")
    public ALERT_TYPE init(Intent intent, Context appContext) {
        analytics = Analytics.getInstance(appContext);
        // Open bundle
        Bundle extras = intent.getExtras();
        if (extras == null || extras.isEmpty())
            return ALERT_TYPE.MISSING_API_KEY;

        String action = intent.getAction();
        if (action == null || action.isEmpty())
            return ALERT_TYPE.INVALID_INTENT_ACTION;
        callout = Callout.fromAction(action);
        analytics.setLogin(callout);

        // Read all bundle fields
        String apiKey = extras.getString(Constants.SIMPRINTS_API_KEY);
        String updateId = extras.getString(Constants.SIMPRINTS_UPDATE_GUID);
        String verifyId = extras.getString(Constants.SIMPRINTS_VERIFY_GUID);
        String userId = extras.getString(Constants.SIMPRINTS_USER_ID);
        String moduleId = extras.getString(Constants.SIMPRINTS_MODULE_ID);
        String metadataString = extras.getString(Constants.SIMPRINTS_METADATA);
        callingPackage = extras.getString(Constants.SIMPRINTS_CALLING_PACKAGE);

        // Check parameters
        if (apiKey == null || apiKey.isEmpty())
            return ALERT_TYPE.MISSING_API_KEY;

        if (apiKey.length() < 8)
            return ALERT_TYPE.INVALID_API_KEY;

        if (userId == null || userId.isEmpty())
            return ALERT_TYPE.MISSING_USER_ID;

        if (moduleId == null || moduleId.isEmpty())
            return ALERT_TYPE.MISSING_MODULE_ID;

        if (callout == Callout.UPDATE && (updateId == null || updateId.isEmpty()))
            return ALERT_TYPE.MISSING_UPDATE_GUID;

        if (callout == Callout.VERIFY && (verifyId == null || verifyId.isEmpty()))
            return ALERT_TYPE.MISSING_VERIFY_GUID;

        Metadata metadata = null;
        if (metadataString != null && !metadataString.isEmpty())
            try {
                metadata = new Metadata(metadataString);
            } catch (Metadata.InvalidMetadataException e) {
                return ALERT_TYPE.INVALID_METADATA;
            }

        // Set attributes accordingly
        new SharedPref(appContext).setAppKeyString(apiKey.substring(0, 8));
        new SharedPref(appContext).setLastUserIdString(userId);
        session.setApiKey(apiKey);
        session.setUserId(userId);
        session.setModuleId(moduleId);
        analytics.setUser(userId, apiKey);

        if (callout == Callout.UPDATE)
            session.setPersonGuid(updateId);
        if (callout == Callout.VERIFY)
            session.setPersonGuid(verifyId);
        if (callout == Callout.REGISTER)
            session.setPersonGuid(UUID.randomUUID().toString());

        session.setMetadata(metadata);

        // Set other attributes
        String deviceId = Settings.Secure.getString(appContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        session.setDeviceId(deviceId);
        analytics.setDeviceId(deviceId);

        return null;
    }

    public Callout getCallout() {
        return this.callout;
    }

    public String getApiKey() {
        return session.getApiKey();
    }

    public String getDeviceId() {
        return session.getDeviceId();
    }

    public String getUserId() {
        return session.getUserId();
    }

    public String getModuleId() {
        return session.getModuleId();
    }

    public String getGuid() {
        return session.getPersonGuid();
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
        analytics.setScannerMac(macAddress);
    }

    public DatabaseContext getData() {
        return data;
    }

    public void setData(DatabaseContext data) {
        this.data = data;
    }

    GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

    void setGoogleApiClient(GoogleApiClient googleApiClient) {
        this.googleApiClient = googleApiClient;
    }

    void setLatitude(String latitude) {
        session.setLatitude(latitude);
    }

    void setLongitude(String longitude) {
        session.setLongitude(longitude);
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

    public String getCallingPackage() {
        return this.callingPackage;
    }

    public void setRefusalForm(RefusalForm refusalForm) {
        this.refusalForm = refusalForm;
    }

    @Nullable
    public RefusalForm getRefusalForm() {
        return this.refusalForm;
    }

    public String getSessionId() {
        return this.session.getSessionId();
    }

    public void destroy() {
        singleton = null;
    }

}
