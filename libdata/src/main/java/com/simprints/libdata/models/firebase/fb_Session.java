package com.simprints.libdata.models.firebase;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static com.simprints.libdata.tools.Routes.sessionRef;
import static com.simprints.libdata.tools.Utils.log;

@SuppressWarnings({"WeakerAccess", "unused"})
public class fb_Session {

    public String sessionId;
    public DatabaseReference sessionNodeRef = null;

    public String callout;
    public String apiKey;
    public String moduleId;
    public String userId;
    public String personGuid;
    public String metadata;
    public String deviceId;
    public String callingPackage;
    public String appVersion;
    public String phoneModel;

    public String macAddress;
    public String scannerId;
    public int hardwareVersion;

    public String latitude;
    public String longitude;

    public Long sessionStartTime;
    public Map<String, String> serverSessionStartTime;

    public Long loadEndTime;
    public Map<String, String> serverloadEndTime;

    public Long mainStartTime;
    public Map<String, String> serverMainStartTime;

    public Long matchStartTime;
    public Map<String, String> serverMatchStartTime;

    public Long sessionEndTime;
    public Map<String, String> serverSessionEndTime;

    public fb_Session() {
    }

    public fb_Session(@Nullable String callout, @Nullable String apiKey, @Nullable String moduleId,
                      @Nullable String userId, @Nullable String personGuid, @Nullable String metadata,
                      @Nullable String deviceId, @Nullable String callingPackage, @Nullable String appVersion,
                      @Nullable String phoneModel)
    {
        this.sessionId = UUID.randomUUID().toString();

        this.callout = nullToEmpty(callout);
        this.apiKey = nullToEmpty(apiKey);
        this.moduleId = nullToEmpty(moduleId);
        this.userId = nullToEmpty(userId);
        this.personGuid = nullToEmpty(personGuid);
        this.metadata = nullToEmpty(metadata);
        this.deviceId = nullToEmpty(deviceId);
        this.callingPackage = nullToEmpty(callingPackage);
        this.appVersion = nullToEmpty(appVersion);
        this.phoneModel = nullToEmpty(phoneModel);

        sessionStartTime = dateToLong(Calendar.getInstance().getTime());
        serverSessionStartTime = ServerValue.TIMESTAMP;

        // Initial save
        initialSave();
    }

    private void initialSave() {
        try {
            FirebaseApp firebaseApp;
            if (this.apiKey.length() >= 8)
                firebaseApp = FirebaseApp.getInstance(this.apiKey.substring(0, 8));
            else
                firebaseApp = FirebaseApp.getInstance();
            final DatabaseReference ref = sessionRef(firebaseApp).push();
            Task<Void> task = ref.setValue(this);
            task.addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    sessionNodeRef = ref;
                }
            });
            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    log("Saving session failed, will try again later");
                }
            });
        } catch (IllegalStateException stateException) {
            log("Firebase app not initialized, cannot save session, will try later");
            sessionNodeRef = null;
        }
    }

    public void saveMacAddress(@NonNull String macAddress) {
        if (sessionNodeRef == null)
            initialSave();

        this.macAddress = macAddress;
        if (sessionNodeRef != null)
            sessionNodeRef.child("macAddress").setValue(macAddress);
    }

    public void saveScannerId(@NonNull String scannerId) {
        if (sessionNodeRef == null)
            initialSave();

        this.scannerId = scannerId;
        if (sessionNodeRef != null)
            sessionNodeRef.child("scannerId").setValue(scannerId);
    }

    public void saveHardwareVersion(int hardwareVersion) {
        if (sessionNodeRef == null)
            initialSave();

        this.hardwareVersion = hardwareVersion;
        if (sessionNodeRef != null)
            sessionNodeRef.child("hardwareVersion").setValue(hardwareVersion);
    }

    public void savePosition(@NonNull String latitude, @NonNull String longitude) {
        if (sessionNodeRef == null)
            initialSave();

        this.latitude = latitude;
        this.longitude = longitude;
        if (sessionNodeRef != null) {
            sessionNodeRef.child("latitude").setValue(latitude);
            sessionNodeRef.child("longitude").setValue(longitude);
        }
    }

    public void logLoadEndTime() {
        if (sessionNodeRef == null)
            initialSave();

        loadEndTime = dateToLong(Calendar.getInstance().getTime());
        serverloadEndTime = ServerValue.TIMESTAMP;
        if (sessionNodeRef != null) {
            sessionNodeRef.child("loadEndTime").setValue(loadEndTime);
            sessionNodeRef.child("serverloadEndTime").setValue(serverloadEndTime);
        }
    }

    public void logMainStartTime() {
        if (sessionNodeRef == null)
            initialSave();

        mainStartTime = dateToLong(Calendar.getInstance().getTime());
        serverMainStartTime = ServerValue.TIMESTAMP;
        if (sessionNodeRef != null) {
            sessionNodeRef.child("mainStartTime").setValue(mainStartTime);
            sessionNodeRef.child("serverMainStartTime").setValue(serverMainStartTime);
        }
    }

    public void logMatchStartTime() {
        if (sessionNodeRef == null)
            initialSave();

        matchStartTime = dateToLong(Calendar.getInstance().getTime());
        serverMatchStartTime = ServerValue.TIMESTAMP;
        if (sessionNodeRef != null) {
            sessionNodeRef.child("matchStartTime").setValue(matchStartTime);
            sessionNodeRef.child("serverMatchStartTime").setValue(serverMatchStartTime);
        }
    }

    public void logSessionEndTime() {
        if (sessionNodeRef == null)
            initialSave();

        sessionEndTime = dateToLong(Calendar.getInstance().getTime());
        serverSessionEndTime = ServerValue.TIMESTAMP;
        if (sessionNodeRef != null) {
            sessionNodeRef.child("sessionEndTime").setValue(sessionEndTime);
            sessionNodeRef.child("serverSessionEndTime").setValue(serverSessionEndTime);
        }
    }

    @NonNull
    private String nullToEmpty(@Nullable String value) {
        return value != null ? value : "";
    }

    @NonNull
    private Long dateToLong(@Nullable Date date) {
        return date != null ? date.getTime() : 0;
    }
}
