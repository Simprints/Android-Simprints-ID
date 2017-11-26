package com.simprints.id.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.simprints.id.data.DataManager;
import com.simprints.id.model.ALERT_TYPE;
import com.simprints.id.model.Callout;
import com.simprints.libdata.DatabaseContext;
import com.simprints.libdata.models.firebase.fb_Session;
import com.simprints.libscanner.Scanner;
import com.simprints.libsimprints.Constants;
import com.simprints.libsimprints.Metadata;
import com.simprints.libsimprints.RefusalForm;

import java.util.UUID;


@SuppressWarnings("ConstantConditions")
public class AppState {

    private static AppState singleton;

    public synchronized static AppState getInstance(DataManager dataManager) {
        if (singleton == null) {
            singleton = new AppState(dataManager);
        }
        return singleton;
    }

    public AppState(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    private DataManager dataManager;

    // Callout parameters
    private String apiKey = null;
    private String updateId = null;
    private String verifyId = null;
    private String userId = null;
    private String moduleId = null;
    private String metadataString = null;
    private String resultFormat = null;
    private Metadata metadata = null;
    private String callingPackage = null;
    private String appVersion = null;
    private String personGuid = null;

    // Other attributes
    private String deviceId = null;
    private String phoneModel = null;
    private String macAddress = null;
    private String scannerId = null;
    private short hardwareVersion = -1;
    private String latitude = null;
    private String longitude = null;

    // Handles on scanner, database, firebase analytics, google api, etc.
    private Scanner scanner = null;
    private DatabaseContext data = null;
    private fb_Session session = null;
    private GoogleApiClient googleApiClient = null;
    private boolean signedIn = false;
    private RefusalForm refusalForm = null;


    @SuppressLint("HardwareIds")
    public ALERT_TYPE init(@NonNull Intent intent, @NonNull Context appContext) {

        // Reads intent parameters
        Callout callout = Callout.fromAction(intent.getAction());
        dataManager.setCallout(callout);
        Bundle extras = intent.getExtras();
        if (extras != null) {
            apiKey = extras.getString(Constants.SIMPRINTS_API_KEY);
            updateId = extras.getString(Constants.SIMPRINTS_UPDATE_GUID);
            verifyId = extras.getString(Constants.SIMPRINTS_VERIFY_GUID);
            userId = extras.getString(Constants.SIMPRINTS_USER_ID);
            moduleId = extras.getString(Constants.SIMPRINTS_MODULE_ID);
            metadataString = extras.getString(Constants.SIMPRINTS_METADATA);
            callingPackage = extras.getString(Constants.SIMPRINTS_CALLING_PACKAGE);
            resultFormat = extras.getString(Constants.SIMPRINTS_RESULT_FORMAT);
        }
        if (callout != null) {
            switch (callout) {
                case UPDATE:
                    personGuid = updateId;
                    break;
                case VERIFY:
                    personGuid = verifyId;
                    break;
                case REGISTER:
                    personGuid = UUID.randomUUID().toString();
                    break;
                default:
                    break;
            }
        }

        // Read other local attributes
        deviceId = Settings.Secure.getString(appContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        PackageInfo pInfo;
        try {
            pInfo = appContext.getPackageManager().getPackageInfo(appContext.getPackageName(), 0);
            appVersion = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        try {
            phoneModel = Build.MANUFACTURER
                    + " " + Build.MODEL + " " + Build.VERSION.RELEASE
                    + " " + Build.VERSION_CODES.class.getFields()[Build.VERSION.SDK_INT].getName();
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        // Save attributes to firebase session, whether they are valid or not
        session = new fb_Session(Callout.toString(callout), apiKey, moduleId, userId, personGuid,
                metadataString, deviceId, callingPackage, appVersion, phoneModel);

        // Check parameters
        if (callout == null)
            return ALERT_TYPE.INVALID_INTENT_ACTION;

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

        metadata = null;
        if (metadataString != null && !metadataString.isEmpty())
            try {
                metadata = new Metadata(metadataString);
            } catch (Metadata.InvalidMetadataException e) {
                return ALERT_TYPE.INVALID_METADATA;
            }

        // Save some attributes in shared preferences
        dataManager.setAppKey(apiKey.substring(0, 8));
        dataManager.setLastUserId(userId);

        return null;
    }

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
        this.latitude = latitude;
        this.longitude = longitude;
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

    public void setData(DatabaseContext data) {
        this.data = data;
    }

    void setGoogleApiClient(GoogleApiClient googleApiClient) {
        this.googleApiClient = googleApiClient;
    }

    public void setSignedIn(boolean signedIn) {
        this.signedIn = signedIn;
    }

    public void setRefusalForm(RefusalForm refusalForm) {
        this.refusalForm = refusalForm;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getUserId() {
        return userId;
    }

    public String getModuleId() {
        return moduleId;
    }

    public String getGuid() {
        return personGuid;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public short getHardwareVersion() {
        return hardwareVersion;
    }

    public Scanner getScanner() {
        return scanner;
    }

    public boolean getSignedIn() {
        return signedIn;
    }

    public String getCallingPackage() {
        return callingPackage;
    }

    public String getResultFormat() { return resultFormat; }

    public String getAppVersion() {
        return appVersion;
    }

    public DatabaseContext getData() {
        return data;
    }

    GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

    @Nullable
    public RefusalForm getRefusalForm() {
        return refusalForm;
    }

    public String getSessionId() {
        return session.sessionId;
    }

    public void destroy() {
        singleton = null;
    }

}
