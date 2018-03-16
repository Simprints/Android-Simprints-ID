package com.simprints.id.data.db.remote.models;

import android.support.annotation.Nullable;

import com.google.firebase.database.ServerValue;
import com.simprints.id.data.db.remote.tools.Utils;

import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class fb_IdEventUpdate {
    public String selectedId;
    public String projectKey;
    public String androidId;
    public Long date;
    public String sessionId;
    public Map<String, String> serverDate;

    public fb_IdEventUpdate() {
    }

    public fb_IdEventUpdate(@Nullable String apiKey, String selectedId, String androidId, String sessionId) {
        if (apiKey != null)
            this.projectKey = apiKey;

        this.selectedId = selectedId;
        this.androidId = androidId;
        this.date = Utils.now().getTime();
        this.sessionId = sessionId;
        this.serverDate = ServerValue.TIMESTAMP;
    }
}
