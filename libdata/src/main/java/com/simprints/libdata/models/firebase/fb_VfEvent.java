package com.simprints.libdata.models.firebase;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.database.ServerValue;
import com.simprints.libcommon.Person;
import com.simprints.libdata.models.Key;
import com.simprints.libdata.models.enums.VERIFY_GUID_EXISTS_RESULT;
import com.simprints.libdata.tools.Utils;
import com.simprints.libsimprints.Verification;

import java.util.Map;

@SuppressWarnings({"unused", "WeakerAccess"})
public class fb_VfEvent {
    public fb_Person ProbePerson;
    public String userId;
    public String sessionId;
    public Long date;
    public String guid;
    public String guidExistsResult;
    public float confidence;
    public Map<String, String> serverDate;

    public fb_VfEvent() {
    }

    public fb_VfEvent(@NonNull Person probe,
                      @NonNull Key key,
                      @NonNull String guid,
                      @Nullable Verification verification,
                      @NonNull String sessionId,
                      @NonNull VERIFY_GUID_EXISTS_RESULT guidExistsResult) {
        this.ProbePerson = new fb_Person(probe, key);
        this.userId = key.userId;
        this.guid = guid;
        this.date = Utils.now().getTime();
        this.sessionId = sessionId;
        this.serverDate = ServerValue.TIMESTAMP;
        this.guidExistsResult = guidExistsResult.toString();
        if (verification != null) {
            this.confidence = verification.getConfidence();
        }
    }
}
