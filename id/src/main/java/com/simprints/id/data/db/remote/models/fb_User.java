package com.simprints.id.data.db.remote.models;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.Query;

import java.util.HashMap;
import java.util.Map;

import static com.simprints.id.data.db.remote.tools.Routes.projectRef;
import static com.simprints.id.data.db.remote.tools.Routes.usersNode;


@SuppressWarnings({"WeakerAccess"})
public class fb_User {
    public String userId;
    public String androidId;
    public Map<String, Boolean> patientList;

    public fb_User() {
    }

    public fb_User(String userId, String androidId) {
        this.userId = userId;
        this.androidId = androidId;
    }

    public fb_User(String userId, String androidId, String firstPatientId) {
        this.userId = userId;
        this.androidId = androidId;
        this.patientList = new HashMap<>();
        this.patientList.put(firstPatientId, true);
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("androidId", androidId);
        result.put("patientList", patientList);
        return result;
    }

    @Exclude
    public static Query getUser(FirebaseApp app, String apiKey, String userId) {
        return projectRef(app, apiKey)
                .child(usersNode())
                .orderByChild("userId")
                .equalTo(userId)
                .limitToFirst(1);
    }

    @Exclude
    public static Query getUser(DatabaseReference ref, String userId) {
        return ref
                .child(usersNode())
                .orderByChild("userId")
                .equalTo(userId)
                .limitToFirst(1);
    }
}
