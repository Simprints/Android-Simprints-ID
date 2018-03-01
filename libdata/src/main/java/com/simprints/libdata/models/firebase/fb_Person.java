package com.simprints.libdata.models.firebase;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.simprints.libcommon.Fingerprint;
import com.simprints.libcommon.Person;
import com.simprints.libdata.models.realm.rl_Person;
import com.simprints.libdata.tools.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.simprints.libdata.tools.Routes.patientsNode;
import static com.simprints.libdata.tools.Routes.projectRef;

@SuppressWarnings({"WeakerAccess"})
public class fb_Person {

    public String patientId;
    public String userId;
    public String moduleId;
    public String androidId;
    public Long createdAt;
    private Long serverCreatedAt;
    public List<fb_Fingerprint> fingerprints;

    public fb_Person() {
    }

    public fb_Person(Person person, String userId, String androidId, String moduleId) {
        this.userId = userId;
        this.androidId = androidId;
        this.moduleId = moduleId;
        this.patientId = person.getGuid();
        this.createdAt = Utils.now().getTime();

        this.fingerprints = new ArrayList<>();
        for (Fingerprint print : person.getFingerprints()) {
            fb_Fingerprint fb_print = new fb_Fingerprint(print);
            fingerprints.add(fb_print);
        }
    }

    public fb_Person(rl_Person person) {
        this.userId = person.userId;
        this.androidId = person.androidId;
        this.patientId = person.patientId;
        this.createdAt = person.createdAt;
        this.moduleId = person.moduleId;

        this.fingerprints = new ArrayList<>();
        for (Fingerprint print : person.getLibPerson().getFingerprints()) {
            fb_Fingerprint fb_print = new fb_Fingerprint(print);
            fingerprints.add(fb_print);
        }
    }

    public java.util.Map<String, String> getServerCreatedAt() {
        return ServerValue.TIMESTAMP;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("patientId", patientId);
        result.put("userId", userId);
        result.put("moduleId", moduleId);
        result.put("androidId", androidId);
        result.put("createdAt", createdAt);
        result.put("serverCreatedAt", ServerValue.TIMESTAMP);
        result.put("fingerprints", fingerprints);
        return result;
    }

    @Exclude
    public static Query getByUser(FirebaseApp app, String apiKey, String userId) {
        return projectRef(app, apiKey).child(patientsNode()).orderByChild("userId").equalTo(userId);
    }

    @Exclude
    public static Query getByModuleId(FirebaseApp app, String apiKey, String moduleId) {
        return projectRef(app, apiKey).child(patientsNode()).orderByChild("moduleId").equalTo(moduleId);
    }
}
