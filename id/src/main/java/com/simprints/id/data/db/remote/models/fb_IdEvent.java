package com.simprints.id.data.db.remote.models;

import com.google.firebase.database.ServerValue;
import com.simprints.libcommon.Person;
import com.simprints.id.data.db.remote.tools.Utils;
import com.simprints.libsimprints.Identification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unused", "WeakerAccess"})
public class fb_IdEvent {
    public fb_Person ProbePerson;
    public String userId;
    public int matchSize;
    public String sessionId;
    public Long date;
    public List<fb_Match> fbMatches;
    public Map<String, String> serverDate;

    public fb_IdEvent() {
    }

    public fb_IdEvent(Person probe, String projectId, String userId, String androidId, String moduleId, int matchSize, List<Identification> identifications, String sessionId) {
        this.ProbePerson = new fb_Person(probe, projectId, userId, moduleId);
        this.userId = userId;
        this.matchSize = matchSize;
        this.date = Utils.now().getTime();
        this.sessionId = sessionId;
        this.serverDate = ServerValue.TIMESTAMP;

        this.fbMatches = new ArrayList<>();
        for (Identification id : identifications) {
            fbMatches.add(new fb_Match(id));
        }
    }
}


