package com.simprints.libdata;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.simprints.libdata.models.firebase.fb_Person;
import com.simprints.libdata.models.firebase.fb_User;
import com.simprints.libdata.models.realm.RealmConfig;
import com.simprints.libdata.models.realm.rl_ApiKey;
import com.simprints.libdata.models.realm.rl_Person;
import com.simprints.libdata.tools.Constants;
import com.simprints.libdata.tools.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import io.realm.Realm;
import io.realm.RealmResults;

import static com.simprints.libdata.tools.Routes.patientNode;
import static com.simprints.libdata.tools.Routes.projectRef;
import static com.simprints.libdata.tools.Routes.userPatientListNode;
import static com.simprints.libdata.tools.Routes.usersNode;
import static com.simprints.libdata.tools.Utils.log;
import static com.simprints.libdata.tools.Utils.wrapCallback;

public class DatabaseSync {

    private final Context context;
    private final String appKey;
    private final DataCallback callback;
    private final Constants.GROUP group;
    private final String userId;

    private FirebaseApp app;
    private String apiKey;
    private DatabaseReference ref;

    public DatabaseSync(@NonNull Context context, @NonNull String appKey, @Nullable DataCallback callback) {
        this.context = context;
        this.appKey = appKey;
        this.callback = wrapCallback("DatabaseSync.sync()", callback);
        this.group = Constants.GROUP.GLOBAL;
        this.userId = null;
    }

    public DatabaseSync(@NonNull Context context, @NonNull String appKey, @Nullable DataCallback callback,
                        @NonNull String userId) {
        this.context = context;
        this.appKey = appKey;
        this.callback = wrapCallback("DatabaseSync.sync()", callback);
        this.group = Constants.GROUP.USER;
        this.userId = userId;
    }

    public void sync() {
        log("DatabaseSync.sync()");

        // Init firebase, realm and validate api key
        if (!readyToSync()) {
            callback.onFailure(DATA_ERROR.SYNC_INTERRUPTED);
            return;
        }

        // Formulate the query to get all the users / all the users in a module / a given user
        Query query;
        switch (group) {
            case GLOBAL:
            case MODULE: // TODO: implement module sync
                query = projectRef(app, apiKey).child(usersNode());
                break;
            case USER:
                query = fb_User.getUser(app, apiKey, userId);
                break;
            default:
                throw new RuntimeException();
        }

        // For each user returned by the query, sync the persons he enrolled
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                // Edge case : no user
                final int nbUsers = (int) dataSnapshot.getChildrenCount();
                if (nbUsers == 0) {
                    callback.onSuccess();
                    return;
                }

                new Thread() {
                    @Override
                    public void run() {
                        final Realm realm = Realm.getInstance(RealmConfig.get(apiKey));
                        final BufferedPersonSaver saver = new BufferedPersonSaver(apiKey);
                        final Semaphore over = new Semaphore(0);

                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            final fb_User user = data.getValue(fb_User.class);
                            syncUser(user, realm, saver);
                        }

                        // This waits for everything to be finished before closing
                        // the realm and calling back
                        saver.flush(new Runnable() {
                            @Override
                            public void run() {
                                over.release();
                            }
                        });
                        over.acquireUninterruptibly();
                        realm.close();
                        callback.onSuccess();
                    }
                }.start();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                log(String.format("Error while getting module or user for sync: %s", databaseError));
                callback.onFailure(DATA_ERROR.SYNC_INTERRUPTED);
            }
        });
    }

    /**
     * Get a grab on the firebase app, get the api key of the signed in user, initialize realm
     * and check the api key.
     *
     * @return True if everything went well and the sync can begin, false else
     */
    private boolean readyToSync() {
        // Check if Firebase app available
        try {
            app = FirebaseApp.getInstance(appKey);
        } catch (IllegalStateException e) {
            log("Unavailable firebase app");
            return false;
        }

        // Sync offline firebase with online firebase
        Utils.forceSync(FirebaseApp.getInstance());
        Utils.forceSync(app);

        // Get if the user is signed in
        FirebaseUser loggedInUser = FirebaseAuth.getInstance(app).getCurrentUser();
        if (loggedInUser == null) {
            log("User not logged in");
            return false;
        }
        apiKey = loggedInUser.getUid();

        Realm.init(context);

        // Validate api key
        Realm realm = Realm.getInstance(RealmConfig.get(apiKey));
        boolean apiKeyIsValid = rl_ApiKey.check(realm, apiKey);
        realm.close();

        if (!apiKeyIsValid) {
            log("Api key not valid");
            return false;
        }

        ref = projectRef(app, apiKey);

        log("Ready");
        return true;
    }

    private void syncUser(@NonNull final fb_User user, @NonNull Realm realm, @NonNull final BufferedPersonSaver saver) {
        Utils.log(String.format("syncing user %s", user.userId));
        // Load all the persons for the specified user from Realm, sorted by guid
        final RealmResults<rl_Person> realmPersons = realm
                .where(rl_Person.class).equalTo("userId", user.userId)
                .findAllSorted("patientId");

        List<String> firebaseGuids;
        if (user.patientList != null) {
            firebaseGuids =  new ArrayList<>(user.patientList.keySet());
            Collections.sort(firebaseGuids);
        } else {
            return;
        }

        int i = 0;
        int j = 0;

        while (i < realmPersons.size() || j < firebaseGuids.size()) {
            if (i >= realmPersons.size()) {
                saver.saveFromFirebase(ref, firebaseGuids.get(j));
                Utils.log(String.format("Firebase -> Realm, patient %s", firebaseGuids.get(j)));
                j++;
            } else if (j >= firebaseGuids.size()) {
                realmToFirebase(realmPersons.get(i), user);
                Utils.log(String.format("Realm -> Firebase, patient %s", realmPersons.get(i).patientId));
                i++;
            } else {
                int cmp = realmPersons.get(i).patientId.compareTo(firebaseGuids.get(j));
                if (cmp < 0) {
                    realmToFirebase(realmPersons.get(i), user);
                    Utils.log(String.format("Realm -> Firebase, patient %s", realmPersons.get(i).patientId));
                    i++;
                } else if (cmp > 0) {
                    saver.saveFromFirebase(ref, firebaseGuids.get(j));
                    Utils.log(String.format("Firebase -> Realm, patient %s", firebaseGuids.get(j)));
                    j++;
                } else {
                    i++;
                    j++;
                }
            }
        }

    }

    private void realmToFirebase(@NonNull rl_Person person, @NonNull fb_User user) {
        // TODO replace userId by encoded userId as node name
        Map<String, Object> updates = new HashMap<>();
        updates.put(patientNode(person.patientId), new fb_Person(person).toMap());
        updates.put(userPatientListNode(user.userId, person.patientId), true);
        ref.updateChildren(updates);
    }
}