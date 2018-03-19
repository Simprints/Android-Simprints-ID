package com.simprints.id.data.db.local.models;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.simprints.id.data.db.remote.models.fb_Fingerprint;
import com.simprints.id.data.db.remote.models.fb_Person;
import com.simprints.id.domain.Constants;
import com.simprints.libcommon.Fingerprint;
import com.simprints.libcommon.Person;
import com.simprints.libsimprints.FingerIdentifier;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import timber.log.Timber;


@SuppressWarnings({"WeakerAccess", "SynchronizationOnLocalVariableOrMethodParameter"})
public class rl_Person extends RealmObject {
    @PrimaryKey
    @SerializedName("id")
    public String patientId;
    public String projectId;
    public String userId;
    public String moduleId;
    public Date createdAt;
    public Date updatedAt;
    public boolean toSync;
    public RealmList<rl_Fingerprint> fingerprints;

    public rl_Person() {
    }

    public rl_Person(fb_Person person) {
        this.patientId = person.getPatientId();
        this.userId = person.getUserId();
        this.createdAt = person.getCreatedAt();
        this.moduleId = person.getModuleId();
        this.projectId = person.getModuleId();
        this.fingerprints = new RealmList<>();
        this.toSync = false;

        for (fb_Fingerprint print : person.getAllFingerprints()) {
            rl_Fingerprint rlPrint = new rl_Fingerprint(print);

            if (rlPrint.getTemplate() != null)
                fingerprints.add(rlPrint);
        }
    }

    public static long count(
        @NonNull Realm realm,
        @NonNull String userId,
        @NonNull String moduleId,
        @NonNull final Constants.GROUP group) {

        switch (group) {
            case GLOBAL:
                return realm.where(rl_Person.class).count();
            case USER:
                return realm.where(rl_Person.class).equalTo("userId", userId).count();
            case MODULE:
                return realm.where(rl_Person.class).equalTo("moduleId", moduleId).count();
            default:
                throw new RuntimeException();
        }
    }

    public static rl_Person get(@NonNull Realm realm, @NonNull String patientId) {
        return realm.where(rl_Person.class).equalTo("patientId", patientId).findFirst();
    }

    public Person getLibPerson() {
        List<Fingerprint> prints = new ArrayList<>();
        for (rl_Fingerprint print : fingerprints) {
            try {
                prints.add(new Fingerprint(FingerIdentifier.values()[print.getFingerId()], print.getTemplate()));
            } catch (IllegalArgumentException arg) {
                Timber.tag("FINGERPRINT").d("FAILED");
            }
        }
        return new Person(patientId, prints);
    }

    public JSONObject getJsonPerson() throws JSONException {
        JSONArray jsonArrayPrints = new JSONArray();
        for (rl_Fingerprint print : fingerprints) {

            int fingerIdInt = print.getFingerId();
            String fingerIdString = null;
            switch (fingerIdInt) {
                case 0:
                    fingerIdString = "RIGHT_5TH_FINGER";
                    break;
                case 1:
                    fingerIdString = "RIGHT_4TH_FINGER";
                    break;
                case 2:
                    fingerIdString = "RIGHT_3RD_FINGER";
                    break;
                case 3:
                    fingerIdString = "RIGHT_INDEX_FINGER";
                    break;
                case 4:
                    fingerIdString = "RIGHT_THUMB";
                    break;
                case 5:
                    fingerIdString = "LEFT_THUMB";
                    break;
                case 6:
                    fingerIdString = "LEFT_INDEX_FINGER";
                    break;
                case 7:
                    fingerIdString = "LEFT_3RD_FINGER";
                    break;
                case 8:
                    fingerIdString = "LEFT_4TH_FINGER";
                    break;
                case 9:
                    fingerIdString = "LEFT_5TH_FINGER";
                    break;

            }

            JSONObject jsonPrint = new JSONObject();
            jsonPrint.put("fingerId", fingerIdString);
            jsonPrint.put("qualityScore", print.getQualityScore());
            jsonPrint.put("template", com.simprints.libcommon.Utils.byteArrayToBase64(print.getTemplate()));

            jsonArrayPrints.put(jsonPrint);
        }

        JSONObject jsonPerson = new JSONObject();

        jsonPerson.put("createdAt", createdAt);
        jsonPerson.put("fingerprints", jsonArrayPrints);
        jsonPerson.put("moduleId", moduleId);
        jsonPerson.put("projectId", projectId);
        jsonPerson.put("patientId", patientId);
        jsonPerson.put("userId", userId);


        return jsonPerson;
    }

    public void save(@NonNull Realm realm) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(rl_Person.this);
            }
        });
    }
}
