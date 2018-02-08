package com.simprints.libdata;

import android.content.ContentResolver;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.simprints.libcommon.Person;
import com.simprints.libdata.databaseResolvers.CommCareResolver;
import com.simprints.libdata.databaseResolvers.DatabaseResolver;
import com.simprints.libdata.models.Key;
import com.simprints.libdata.models.enums.VERIFY_GUID_EXISTS_RESULT;
import com.simprints.libdata.models.firebase.fb_IdEvent;
import com.simprints.libdata.models.firebase.fb_IdEventUpdate;
import com.simprints.libdata.models.firebase.fb_Person;
import com.simprints.libdata.models.firebase.fb_RefusalForm;
import com.simprints.libdata.models.firebase.fb_Session;
import com.simprints.libdata.models.firebase.fb_User;
import com.simprints.libdata.models.firebase.fb_VfEvent;
import com.simprints.libdata.models.realm.RealmConfig;
import com.simprints.libdata.models.realm.rl_ApiKey;
import com.simprints.libdata.models.realm.rl_Person;
import com.simprints.libdata.models.sql_OLD.MigrationTask;
import com.simprints.libdata.network.NetworkRequestCallback;
import com.simprints.libdata.tools.Constants;
import com.simprints.libdata.tools.Utils;
import com.simprints.libsimprints.Identification;
import com.simprints.libsimprints.RefusalForm;
import com.simprints.libsimprints.Verification;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

import static com.simprints.libdata.tools.Routes.idEventRef;
import static com.simprints.libdata.tools.Routes.idUpdateRef;
import static com.simprints.libdata.tools.Routes.patientNode;
import static com.simprints.libdata.tools.Routes.projectRef;
import static com.simprints.libdata.tools.Routes.refusalRef;
import static com.simprints.libdata.tools.Routes.sessionRef;
import static com.simprints.libdata.tools.Routes.userNode;
import static com.simprints.libdata.tools.Routes.userPatientListNode;
import static com.simprints.libdata.tools.Routes.vfEventRef;
import static com.simprints.libdata.tools.Utils.log;
import static com.simprints.libdata.tools.Utils.wrapCallback;

@SuppressWarnings("unused")
public class DatabaseContext {

    private final static String databaseName = "Simprints.db";
    private final static int databaseVersion = 6;

    private final Context context;
    private final Key key;
    private final String authUrl;
    private final String storageBucketUrl;

    // Realm
    private final Realm realm;

    // Firebase
    private FirebaseApp firebaseApp;
    private final DatabaseReference projectRef;
    private fb_Session session;

    // User listener
    private fb_User user;
    private ValueEventListener userListener;

//    // Connection listener
//    volatile private Boolean isConnected = false;
//    private final DatabaseReference connectionDbRef;
//    private final ValueEventListener connectionDispatcher;
//    private final Set<ConnectionListener> connectionListeners;
//
//    // Authentication listener
//    private volatile boolean signedIn = false;
//    private final FirebaseAuth firebaseAuth;
//    private final FirebaseAuth.AuthStateListener authDispatcher;
//    private final Set<AuthListener> authListeners;


    /**
     * Creates a new data object
     *
     * @param apiKey          Api key to use to dialog with the remote and local databases
     * @param context         Application context to use for Volley/ActiveAndroid
     * @param userId          The userId of the person making the call
     * @param androidId       The id of the Android device
     * @param firebaseProject Name of the Firebase project to use
     */
    public DatabaseContext(@NonNull String apiKey, @NonNull String userId, @NonNull String moduleId,
                           @NonNull String androidId, @NonNull Context context,
                           @NonNull String firebaseProject) {
        this.context = context;
        this.key = new Key(apiKey, userId, moduleId, androidId);
        this.authUrl = String.format("https://%s.appspot.com/key", firebaseProject);
        this.storageBucketUrl = String.format("gs://%s-firebase-storage/", firebaseProject);

        // Initialize realm and realm request queue
        Realm.init(context);
        realm = Realm.getInstance(RealmConfig.get(apiKey));


        // Initialize firebase
        String appKey = apiKey.substring(0, 8);
        try {
            FirebaseApp.initializeApp(context, FirebaseOptions.fromResource(context), appKey);
        } catch (IllegalStateException stateException) {
            log("Firebase app already initialized");
        }

        try {
            firebaseApp = FirebaseApp.getInstance(appKey);
        } catch (IllegalStateException stateException) {
            log("Firebase app not initialized");
        }

        // Sync offline firebase with online firebase
        Utils.forceSync(FirebaseApp.getInstance());
        Utils.forceSync(firebaseApp);

        projectRef = projectRef(firebaseApp, apiKey);

        // Initialize user listener
        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(fb_User.class);

                if (user == null) {
                    user = new fb_User(key.userId, key.androidId);
                    user.patientList = new HashMap<>();
                    return;
                }

                if (user.userId == null)
                    user.userId = key.userId;
                if (user.androidId == null)
                    user.androidId = key.androidId;
                if (user.patientList == null)
                    user.patientList = new HashMap<>();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                user = null;
            }
        };

        projectRef.child(userNode(key.userId)).addValueEventListener(userListener);

        // Initialize connection listener
        connectionListeners = new HashSet<>();
        connectionDispatcher = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = dataSnapshot.getValue(Boolean.class);

                synchronized (connectionListeners) {
                    if (connected) {
                        log("Connected");
                        isConnected = true;
                        for (ConnectionListener listener : connectionListeners)
                            listener.onConnection();
                        if (!signedIn) {
                            log("Connected and not signed in, trying to sign in");
                            signIn(null);
                        }
                    } else {
                        log("Disconnected");
                        isConnected = false;
                        for (ConnectionListener listener : connectionListeners)
                            listener.onDisconnection();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        connectionDbRef = Utils.getDatabase(firebaseApp).getReference(".info/connected");
        connectionDbRef.addValueEventListener(connectionDispatcher);
        log("Connection listener set");

        // Initialize auth listener
        authListeners = new HashSet<>();
        authDispatcher = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser fbUser = firebaseAuth.getCurrentUser();
                if (fbUser != null) {
                    //New user != signed in user
                    if (!fbUser.getUid().equals(key.apiKey)) {
                        log("Signed out by other user");
                        firebaseAuth.signOut();
                        signedIn = false;
                    }
                    //User is signed in
                    else {
                        log("Signed in");
                        synchronized (authListeners) {
                            for (AuthListener authListener : authListeners)
                                authListener.onSignIn();
                        }
                        signedIn = true;
                        setSync();
                    }
                }
                //User is signed out
                else {
                    log("Signed out");
                    synchronized (authListeners) {
                        for (AuthListener authListener : authListeners)
                            authListener.onSignOut();
                    }
                    signedIn = false;
                }
            }
        };
        firebaseAuth = FirebaseAuth.getInstance(firebaseApp);
        firebaseAuth.addAuthStateListener(authDispatcher);
        log("Auth state listener set");
    }

//    /**
//     * Register a new connection listener whose onConnection() and onDisconnection() method will be
//     * called whenever a connection or disconnection is detected.
//     *
//     * @param listener The new connection listener
//     */
//    public void registerConnectionListener(@NonNull ConnectionListener listener) {
//        synchronized (connectionListeners) {
//            connectionListeners.add(listener);
//        }
//    }
//
//    /**
//     * @return True iff connected
//     */
//    public boolean isConnected() {
//        return isConnected;
//    }
//
//    /**
//     * Unregister a connection listener.
//     * Un-registering a listener that was not registered succeeds but has no effect.
//     *
//     * @param listener The connection listener
//     */
//    public void unregisterConnectionListener(@NonNull ConnectionListener listener) {
//        synchronized (connectionListeners) {
//            connectionListeners.remove(listener);
//        }
//    }
//
//    /**
//     * Register a new auth listener whose onSignIn() and onSignOut() method will be
//     * called whenever a sign in or sign out is detected.
//     *
//     * @param listener The new auth listener
//     */
//    public void registerAuthListener(@NonNull AuthListener listener) {
//        synchronized (authListeners) {
//            authListeners.add(listener);
//        }
//    }
//
//    /**
//     * Unregister a auth listener.
//     * Un-registering a listener that was not registered succeeds but has no effect.
//     *
//     * @param listener The auth listener
//     */
//    public void unregisterAuthListener(@NonNull AuthListener listener) {
//        synchronized (authListeners) {
//            authListeners.remove(listener);
//        }
//    }


    /**
     * This method is for any potential database updates / migrations that need to happen before
     * making a database context. Most of the time this method will be empty.
     *
     * @param callback The onSuccess() method of this callback is called when the connection is established.
     */
    public void initDatabase(@Nullable DataCallback callback) {
        log("DatabaseContext.initDatabase()");
        DataCallback wrappedCallback = wrapCallback("DatabaseContext.initDatabase()", callback);

        File dbFile = context.getDatabasePath(databaseName);
        if (dbFile.exists()) {
            new MigrationTask(context, databaseName, databaseVersion, wrappedCallback).execute();
        } else {
            wrappedCallback.onSuccess();
        }
    }

//    /**
//     * Checks the api key with the server.
//     * If we cannot reach the server, the status of the api key won't change
//     *
//     * @param callback The onSuccess() method of this callback is called when the api key is validated
//     *                 by the server, or was previously valid and the server could not be reached
//     *                 <p>
//     *                 In case of failure, the onFailure() method is called with one of the
//     *                 following errors:
//     *                 - UNVERIFIED_API_KEY if the api key was unchecked and the server could not be reached
//     *                 - INVALID_API_KEY if the api key is invalid, or was invalid and the server could not be reached
//     */
//    public void signIn(@Nullable final DataCallback callback) {
//        log("DatabaseContext.signIn()");
//        final DataCallback wrappedCallback = wrapCallback("DatabaseContext.signIn()", callback);
//
//        if (rl_ApiKey.check(realm, key.apiKey)) {
//            key.status = Key.Status.VALID;
//            wrappedCallback.onSuccess();
//        }
//
//
//        key.validate(context, authUrl, realm, new NetworkRequestCallback() {
//            @Override
//            public void onResult(int statusCode) {
//                log(String.format(Locale.UK, "Key validation: status code %d", statusCode));
//                if (statusCode == 200 && key.token != null)
//                    firebaseAuth.signInWithCustomToken(key.token)
//                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                                @Override
//                                public void onComplete(@NonNull Task<AuthResult> task) {
//                                    if (task.isSuccessful()) {
//                                        log("Firebase Auth signInWithCustomToken successful");
//                                    } else {
//                                        log(String.format("Firebase Auth signInWithCustomToken failed: %s", task.getException()));
//                                    }
//                                }
//                            });
//
//                switch (key.status) {
//                    case UNVERIFIED:
//                        wrappedCallback.onFailure(DATA_ERROR.UNVERIFIED_API_KEY);
//                        break;
//                    case VALID:
//                        wrappedCallback.onSuccess();
//                        break;
//                    case INVALID:
//                        wrappedCallback.onFailure(DATA_ERROR.INVALID_API_KEY);
//                        break;
//                }
//            }
//        });
//    }

    public void signOut() {
        firebaseAuth.signOut();
    }

    public static boolean isSignedIn(String appKey) {
        FirebaseApp app;
        try {
            app = FirebaseApp.getInstance(appKey);
        } catch (Exception e) {
            return false;
        }

        return FirebaseAuth.getInstance(app).getCurrentUser() != null;
    }


    /**
     * Saves the specified refusal form
     *
     * @param refusalForm refusal form to save
     * @param sessionId   session ID
     * @return True if the form was successfully saved,
     * False if the apiKey of this database context is invalid and the session could not be saved.
     */
    public boolean saveRefusalForm(@NonNull RefusalForm refusalForm, @NonNull String sessionId) {
        if (!rl_ApiKey.check(realm, key.apiKey))
            return false;

        refusalRef(firebaseApp).push().setValue(new fb_RefusalForm(refusalForm, key.apiKey, user.userId, sessionId));

        return true;
    }

    /**
     * Saves/Update the specified person
     * (two persons are considered the same if they have the same guid)
     *
     * @param person Person to save/update
     * @return True if the person was successfully saved,
     * False if the apiKey of this database context is invalid and the person could not be saved.
     */
    public boolean savePerson(@NonNull final Person person) {
        if (!rl_ApiKey.check(realm, key.apiKey))
            return false;

        fb_Person fbPerson = new fb_Person(person, key);
        new rl_Person(fbPerson).save(realm);

        if (signedIn) {
            Map<String, Object> updates = new HashMap<>();
            updates.put(patientNode(person.getGuid()), fbPerson.toMap());

            if (user == null) {
                updates.put(userPatientListNode(key.userId, person.getGuid()), true);
            } else {
                user.patientList.put(person.getGuid(), true);
                updates.put(userNode(key.userId), user.toMap());
            }

            projectRef.updateChildren(updates);
        }

        return true;
    }

    /**
     * Saves the specified verification event to the local database
     *
     * @param probe            Probe
     * @param match            The verification object
     * @param sessionId        session ID
     * @param guidExistsResult the result on whether the GUID was found or not
     * @return True if the verification event was saved successfully, false if it was not (because
     * the api key is invalid)
     */
    public boolean saveVerification(@NonNull Person probe,
                                    @NonNull String guid,
                                    @Nullable Verification match,
                                    @NonNull String sessionId,
                                    @NonNull VERIFY_GUID_EXISTS_RESULT guidExistsResult) {
        if (!rl_ApiKey.check(realm, key.apiKey))
            return false;

        vfEventRef(firebaseApp, key.apiKey).push().setValue(new fb_VfEvent(probe, key, guid, match, sessionId, guidExistsResult));

        return true;
    }

    /**
     * Saves the specified identification event to the local database
     *
     * @param probe     Probe
     * @param matches   Matches
     * @param sessionId session ID
     * @return True if the identification event was saved successfully, false
     * if it was not (because the api key is invalid)
     */
    public boolean saveIdentification(Person probe, int matchSize, List<Identification> matches, String sessionId) {
        if (!rl_ApiKey.check(realm, key.apiKey))
            return false;

        idEventRef(firebaseApp, key.apiKey).push().setValue(new fb_IdEvent(probe, key, matchSize, matches, sessionId));

        return true;
    }

    public static boolean updateIdentification(@NonNull String apiKey, @NonNull String caseId,
                                               @NonNull String androidId, @NonNull String sessionId) {
        idUpdateRef(apiKey).push().setValue(new fb_IdEventUpdate(apiKey, caseId, androidId, sessionId));
        return true;
    }

    /**
     * Load all the people stored in the local database for the api key of this database
     * context, and add them to the specified list.
     * The list should not be read before getting a successful callback
     *
     * @param loadedPeople List to load people into
     * @param callback     The onSuccess() method of this callback is called when loading finishes
     *                     (its onFailure() method is called with INVALID_API_KEY if the api key
     *                     is not valid, or was not verified)
     */
    public void loadPeople(@NonNull final List<Person> loadedPeople, @NonNull final Constants.GROUP group,
                           @Nullable final DataCallback callback) {
        log("DatabaseContext.loadPeople()");
        final DataCallback wrappedCallback = wrapCallback("DatabaseContext.loadPeople()", callback);

        final Realm mRealm = Realm.getInstance(RealmConfig.get(key.apiKey));

        if (!rl_ApiKey.check(mRealm, key.apiKey)) {
            wrappedCallback.onFailure(DATA_ERROR.INVALID_API_KEY);
            return;
        }

        final RealmResults<rl_Person> request;
        switch (group) {
            case GLOBAL:
                request = mRealm.where(rl_Person.class).findAllAsync();
                break;
            case USER:
                request = mRealm.where(rl_Person.class).equalTo("userId", key.userId).findAllAsync();
                break;
            case MODULE:
                request = mRealm.where(rl_Person.class).equalTo("moduleId", key.moduleId).findAllAsync();
                break;
            default:
                throw new RuntimeException();
        }
        request.addChangeListener(new RealmChangeListener<RealmResults<rl_Person>>() {
            @Override
            public void onChange(RealmResults<rl_Person> results) {
                request.removeChangeListener(this);
                for (rl_Person person : results)
                    loadedPeople.add(person.getLibPerson());
                mRealm.close();
                wrappedCallback.onSuccess();
            }
        });
    }

    /**
     * Load a person stored under a specified guid for the api key of this database context.
     * Look into realm first, then into firebase.
     *
     * @param guid         The guid of the person(s, but should not happen in theory) to load
     * @param loadedPerson List to load the person into
     * @param callback     This callback's onSuccess() method is called when the person is loaded,
     *                     or its onFailure() method is called if the api key is not verified
     *                     or if the person was not found
     */
    public void loadPerson(@NonNull final List<Person> loadedPerson, @NonNull String guid,
                           @NonNull DataCallback callback) {
        log("DatabaseContext.loadPerson()");
        final DataCallback wrappedCallback = wrapCallback("DatabaseContext.loadPerson()", callback);

        if (!rl_ApiKey.check(realm, key.apiKey)) {
            wrappedCallback.onFailure(DATA_ERROR.INVALID_API_KEY);
            return;
        }

        rl_Person rlPerson = realm.where(rl_Person.class).equalTo("patientId", guid).findFirst();
        if (rlPerson != null) {
            loadedPerson.add(rlPerson.getLibPerson());
            wrappedCallback.onSuccess();
            return;
        }

        projectRef.child(patientNode(guid)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                fb_Person fbPerson = dataSnapshot.getValue(fb_Person.class);

                if (fbPerson == null) {
                    wrappedCallback.onFailure(DATA_ERROR.NOT_FOUND);
                } else {
                    loadedPerson.add(new rl_Person(fbPerson).getLibPerson());
                    wrappedCallback.onSuccess();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                wrappedCallback.onFailure(DATA_ERROR.NOT_FOUND);
            }
        });

    }

    /**
     * Reads the internal Realm database and converts it to a JSON file, then uploads the file to
     * Firebase Storage. Designed to run on a background thread.
     *
     * @param fileName  The name of the file to put in Firebase storage
     * @param androidId The android Id so that it can be added to the metadata
     * @param group     The setting from which to copy people (user, module, or global)
     * @param callback  The data callback to execute once successful or failed
     */
    public void recoverRealmDb(@Nullable String fileName,
                               @NonNull String androidId,
                               @NonNull Constants.GROUP group,
                               @NonNull DataCallback callback) {
        log("DatabaseContext.recoverRealmDb()");
        final DataCallback wrappedCallback = wrapCallback("DatabaseContext.recoverRealmDb", callback);

        // Create a new Realm instance - needed since this should rn on a background thread
        final Realm mRealm = Realm.getInstance(RealmConfig.get(key.apiKey));

        if (!rl_ApiKey.check(mRealm, key.apiKey)) {
            wrappedCallback.onFailure(DATA_ERROR.INVALID_API_KEY);
            return;
        }

        final RealmResults<rl_Person> request;
        switch (group) {
            case GLOBAL:
                request = mRealm.where(rl_Person.class).findAllAsync();
                break;
            case USER:
                request = mRealm.where(rl_Person.class).equalTo("userId", key.userId).findAllAsync();
                break;
            case MODULE:
                request = mRealm.where(rl_Person.class).equalTo("moduleId", key.moduleId).findAllAsync();
                break;
            default:
                throw new RuntimeException();
        }

        // Prepare and connect the streams
        final PipedInputStream realmDbInputStream = new PipedInputStream();
        final PipedOutputStream realmDbOutputStream = new PipedOutputStream();

        try {
            realmDbOutputStream.connect(realmDbInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Once the realm request is processed
        request.addChangeListener(new RealmChangeListener<RealmResults<rl_Person>>() {
            @Override
            public void onChange(RealmResults<rl_Person> results) {
                try {
                    request.removeChangeListener(this);
                    // Write the JSON people and make sure it's valid JSON
                    realmDbOutputStream.write("{".getBytes());
                    int count = 0;
                    for (rl_Person person : results) {
                        JSONObject personJson = person.getJsonPerson();
                        String commaString = ",";
                        // We need commas before all entries except the first
                        if (count == 0) {
                            commaString = "";
                        }
                        count++;
                        String nodeString = "\"" + personJson.get("patientId").toString() + "\"" + ":";
                        String personJsonString = commaString + nodeString + person.getJsonPerson().toString();
                        // Send the JSON person to the Output stream
                        realmDbOutputStream.write(personJsonString.getBytes());
                    }
                    realmDbOutputStream.write("}".getBytes());
                    // Once we're finished writing to the output stream we can close it & the realm reference
                    realmDbOutputStream.close();
                    mRealm.close();
                } catch (JSONException e) {
                    e.printStackTrace();
                    wrappedCallback.onFailure(DATA_ERROR.JSON_ERROR);
                } catch (IOException e) {
                    e.printStackTrace();
                    wrappedCallback.onFailure(DATA_ERROR.IO_BUFFER_WRITE_ERROR);
                }

            }
        });

        // Get a reference to [bucket]/recovered-realm-dbs/[projectKey]/[userId]/[db-name].json
        FirebaseStorage storage = FirebaseStorage.getInstance(firebaseApp);
        StorageReference rootRef = storage.getReferenceFromUrl(storageBucketUrl);
        StorageReference recoveredRealmDbsRef = rootRef.child("recovered-realm-dbs");
        StorageReference projectPathRef = recoveredRealmDbsRef.child(key.apiKey);
        StorageReference userPathRef = projectPathRef.child(key.userId);
        StorageReference fileReference = userPathRef.child(fileName != null ? fileName : "recovered-realm-db");

        // Create some metadata to add to the JSON file
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setCustomMetadata("projectKey", key.apiKey)
                .setCustomMetadata("userId", key.userId)
                .setCustomMetadata("androidId", androidId)
                .build();

        // Begin the upload task to the reference using the input stream & metadata
        UploadTask uploadTask = fileReference.putStream(realmDbInputStream, metadata);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Try to close the stream and send the onFailure callback
                e.printStackTrace();
                try {
                    realmDbInputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                wrappedCallback.onFailure(DATA_ERROR.FAILED_TO_UPLOAD);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Try to close the stream and send the onSuccess callback
                try {
                    realmDbInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                wrappedCallback.onSuccess();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                //noinspection VisibleForTests
                log("Realm DB upload progress: " + Long.toString(taskSnapshot.getBytesTransferred()));
            }
        });
    }

    public long getPeopleCount(@NonNull Constants.GROUP group) {
        log("DatabaseContext.getPeopleCount()");

        if (!rl_ApiKey.check(realm, key.apiKey))
            return -1;

        return rl_Person.count(realm, key, group);
    }

    /**
     * @param callback The onSuccess() method of this callback is called when resolution finishes
     */
    public void resolveCommCare(ContentResolver contentResolver, @Nullable final DataCallback callback) {
        log("DatabaseContext.resolveCommCare()");
        final DataCallback wrappedCallback = wrapCallback("DatabaseContext.resolveCommCare()", callback);

        DatabaseResolver cr = new CommCareResolver();
        cr.resolve(contentResolver, key, null, wrappedCallback);
    }

    private void setSync() {
        //projectRef.child(usersNode()).keepSynced(true);
    }

//    /**
//     * Save the specified session to the remote database
//     * @return The corresponding asynchronous task
//     */
//    public Task<Void> saveSession(fb_Session session) {
//        return sessionRef(firebaseApp).push().setValue(session);
//    }


    public void destroy() {
        // Unregister listeners
        projectRef.child(userNode(key.userId)).removeEventListener(userListener);
        firebaseAuth.removeAuthStateListener(authDispatcher);
        connectionDbRef.removeEventListener(connectionDispatcher);

        // Close realm
        realm.close();
    }

    public NaiveSyncManager getNaiveSyncManager() {
        return new NaiveSyncManager(firebaseApp, key.apiKey);
    }


}
