package com.simprints.libdata.models.realm;

import android.support.annotation.NonNull;

import com.simprints.libcommon.Fingerprint;
import com.simprints.libcommon.Person;
import com.simprints.libdata.models.firebase.fb_Fingerprint;
import com.simprints.libdata.models.firebase.fb_Person;
import com.simprints.libdata.tools.Constants;
import com.simprints.libsimprints.FingerIdentifier;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import timber.log.Timber;


@SuppressWarnings({"WeakerAccess", "SynchronizationOnLocalVariableOrMethodParameter"})
public class rl_Person extends RealmObject {
    @PrimaryKey
    public String patientId;
    public String userId;
    public String androidId;
    public String moduleId;
    public Long createdAt;
    public RealmList<rl_Fingerprint> fingerprints;

    public rl_Person() {
    }

    public rl_Person(fb_Person person) {
        this.patientId = person.patientId;
        this.userId = person.userId;
        this.createdAt = person.createdAt;
        this.androidId = person.androidId;
        this.moduleId = person.moduleId;
        this.fingerprints = new RealmList<>();

        if (person.fingerprints != null) {
            for (fb_Fingerprint print : person.fingerprints) {
                rl_Fingerprint rlPrint = new rl_Fingerprint(print, this);

                if (rlPrint.template != null)
                    fingerprints.add(rlPrint);
            }
        }
    }

    public Person getLibPerson() {
        List<Fingerprint> prints = new ArrayList<>();
        for (rl_Fingerprint print : fingerprints) {
            try {
                prints.add(new Fingerprint(FingerIdentifier.values()[print.fingerId], print.template));
            } catch (IllegalArgumentException arg) {
                Timber.tag("FINGERPRINT").d("FAILED");
            }
        }
        return new Person(patientId, prints);
    }


    public JSONObject getJsonPerson() throws JSONException {
        JSONArray jsonArrayPrints = new JSONArray();
        for (rl_Fingerprint print : fingerprints) {

            int fingerIdInt = print.fingerId;
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
            jsonPrint.put("qualityScore", print.qualityScore);
            jsonPrint.put("template", com.simprints.libcommon.Utils.byteArrayToBase64(print.template));

            jsonArrayPrints.put(jsonPrint);
        }

        JSONObject jsonPerson = new JSONObject();

        jsonPerson.put("androidId", androidId);
        jsonPerson.put("createdAt", createdAt);
        jsonPerson.put("fingerprints", jsonArrayPrints);
        jsonPerson.put("moduleId", moduleId);
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

    public static long count(@NonNull Realm realm, @NonNull String userId, @NonNull String moduleId, @NonNull final Constants.GROUP group) {
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
}
