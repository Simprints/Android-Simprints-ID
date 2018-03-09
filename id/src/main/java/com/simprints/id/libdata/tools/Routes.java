package com.simprints.id.libdata.tools;

import android.support.annotation.NonNull;
import android.util.Base64;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

@SuppressWarnings({"FieldCanBeLocal", "WeakerAccess"})
public class Routes {

    //Prob routes
    private static String projectRef = "projects";
    private static String sessionRef = "sessions";
    private static String idEventRef = "id-events";
    private static String idUpdateRef = "id-events-update";
    private static String vfEventRef = "vf-events";
    private static String refusalRef = "refusal-forms";

    // TODO : Migrate to Firestore
    public static DatabaseReference projectRef(FirebaseApp app, String projectId) {
        return Utils.getDatabase(app).getReference().child(projectRef).child(projectId);
    }

    public static DatabaseReference sessionRef(FirebaseApp app) {
        return Utils.getDatabase(app).getReference().child(sessionRef);
    }

    public static DatabaseReference userRef(FirebaseApp app, String projectId, String userId) {
        return Utils.getDatabase(app).getReference(String.format("/projects/%s%s", projectId, userNode(userId)));
    }

    public static DatabaseReference idEventRef(FirebaseApp app, String projectId) {
        return Utils.getDatabase(app).getReference().child(idEventRef).child(projectId);
    }

    public static DatabaseReference idUpdateRef(String projectId) {
        return Utils.getDatabase(null).getReference().child(idUpdateRef).child(projectId);
    }

    public static DatabaseReference vfEventRef(FirebaseApp app, String projectId) {
        return FirebaseDatabase.getInstance(app).getReference().child(vfEventRef).child(projectId);
    }

    public static DatabaseReference refusalRef(FirebaseApp app) {
        return Utils.getDatabase(app).getReference().child(refusalRef);
    }

    public static DatabaseReference junkRef(FirebaseApp app) {
        return Utils.getDatabase(app).getReference().child("junk");
    }

    public static String patientNode(String patientId) {
        return String.format("/patients/%s", patientId);
    }

    public static String patientsNode() {
        return "/patients";
    }

    public static String userNode(@NonNull String userId) {
        if (userId.matches(".*(\\.|\\[|\\]|#|\\$).*")) {
            return String.format("/users/%s", Base64.encodeToString(userId.getBytes(), Base64.NO_WRAP | Base64.URL_SAFE));
        } else {
            return String.format("/users/%s", userId);
        }
    }

    public static String usersNode() {
        return "/users";
    }

    public static String userPatientListNode(@NonNull String userId, @NonNull String patientId) {
        return String.format("%s/patientList/%s", userNode(userId), patientId);
    }

}
