package com.simprints.libdata.models.firebase;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.database.ServerValue;
import com.simprints.libsimprints.RefusalForm;

import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class fb_RefusalForm {
    public String reason;
    public String otherText;
    public String apiKey;
    public String userId;
    public String sessionId;
    public Map<String, String> serverTimestamp;

    public fb_RefusalForm() {
    }

    public fb_RefusalForm(@NonNull RefusalForm refusalForm, @NonNull String apiKey,
                          @NonNull String userId, @NonNull String sessionId) {
        this.reason = nullToEmpty(refusalForm.getReason());
        this.otherText = nullToEmpty(refusalForm.getExtra());
        this.apiKey = apiKey;
        this.userId = userId;
        this.sessionId = sessionId;
        this.serverTimestamp = ServerValue.TIMESTAMP;
    }

    @NonNull
    private String nullToEmpty(@Nullable String value) {
        return value != null ? value : "";
    }
}
