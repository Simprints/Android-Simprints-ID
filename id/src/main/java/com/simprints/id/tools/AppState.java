package com.simprints.id.tools;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.android.gms.common.api.GoogleApiClient;
import com.simprints.id.data.DataManager;
import com.simprints.id.model.ALERT_TYPE;
import com.simprints.id.model.Callout;
import com.simprints.libdata.models.firebase.fb_Session;
import com.simprints.libscanner.Scanner;
import com.simprints.libsimprints.Constants;
import com.simprints.libsimprints.Metadata;

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

    // Other attributes
    private String macAddress = null;
    private String scannerId = null;
    private short hardwareVersion = -1;

    // Handles on scanner, database, firebase analytics, google api, etc.
    private Scanner scanner = null;
    private fb_Session session = null;
    private GoogleApiClient googleApiClient = null;

    @SuppressLint("HardwareIds")
    public ALERT_TYPE init(@NonNull Intent intent) {

        Callout callout = Callout.NULL;
        String apiKey = "";
        String moduleId = "";
        String userId = "";
        String updateId = "";
        String verifyId = "";
        String patientId = "";
        String callingPackage = "";
        String metadata = "";
        String resultFormat = "";

        // Reads intent parameters
        if (intent.getAction() != null) {
            callout = Callout.fromAction(intent.getAction());
        }

        Bundle extras = intent.getExtras();
        if (extras != null) {
            apiKey = extras.getString(Constants.SIMPRINTS_API_KEY, "");
            updateId = extras.getString(Constants.SIMPRINTS_UPDATE_GUID, "");
            verifyId = extras.getString(Constants.SIMPRINTS_VERIFY_GUID, "");
            userId = extras.getString(Constants.SIMPRINTS_USER_ID, "");
            moduleId = extras.getString(Constants.SIMPRINTS_MODULE_ID, "");
            metadata = extras.getString(Constants.SIMPRINTS_METADATA, "");
            callingPackage = extras.getString(Constants.SIMPRINTS_CALLING_PACKAGE, "");
            resultFormat = extras.getString(Constants.SIMPRINTS_RESULT_FORMAT, "");
        }
        switch (callout) {
            case UPDATE:
                patientId = updateId;
                break;
            case VERIFY:
                patientId = verifyId;
                break;
            case REGISTER:
                patientId = UUID.randomUUID().toString();
                break;
            default:
                break;
        }

        // Save attributes to firebase session, whether they are valid or not
        session = new fb_Session(Callout.toString(callout),
                apiKey,
                moduleId,
                userId,
                patientId,
                metadata,
                dataManager.getDeviceId(),
                callingPackage,
                dataManager.getAppVersionName(),
                dataManager.getDeviceModel() + " " + dataManager.getAndroidSdkVersion());

        // Save session parameters
        dataManager.setCallout(callout);
        dataManager.setApiKey(apiKey);
        dataManager.setModuleId(moduleId);
        dataManager.setUserId(userId);
        dataManager.setPatientId(patientId);
        dataManager.setAppKey(apiKey.substring(0, 8));
        dataManager.setCallingPackage(callingPackage);
        dataManager.setMetadata(metadata);
        dataManager.setResultFormat(resultFormat);
        dataManager.setSessionId(session.sessionId);

        // Check parameters
        if (callout == Callout.NULL)
            return ALERT_TYPE.INVALID_INTENT_ACTION;

        if (apiKey.isEmpty())
            return ALERT_TYPE.MISSING_API_KEY;

        if (apiKey.length() < 8)
            return ALERT_TYPE.INVALID_API_KEY;

        if (userId.isEmpty())
            return ALERT_TYPE.MISSING_USER_ID;

        if (moduleId.isEmpty())
            return ALERT_TYPE.MISSING_MODULE_ID;

        if (callout == Callout.UPDATE && updateId.isEmpty())
            return ALERT_TYPE.MISSING_UPDATE_GUID;

        if (callout == Callout.VERIFY && verifyId.isEmpty())
            return ALERT_TYPE.MISSING_VERIFY_GUID;

        if (!metadata.isEmpty())
            try {
                new Metadata(metadata);
            } catch (Metadata.InvalidMetadataException e) {
                return ALERT_TYPE.INVALID_METADATA;
            }

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
