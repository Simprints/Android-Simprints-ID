package com.simprints.id.data.db.remote

import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.UploadTask
import com.simprints.id.BuildConfig
import com.simprints.id.data.db.remote.adapters.toFirebaseSession
import com.simprints.id.data.models.Session
import com.simprints.id.exceptions.unsafe.FirebaseUninitialisedError
import com.simprints.id.secure.models.Token
import com.simprints.libcommon.Person
import com.simprints.libdata.*
import com.simprints.libdata.models.enums.VERIFY_GUID_EXISTS_RESULT
import com.simprints.libdata.models.firebase.*
import com.simprints.libdata.models.realm.RealmConfig
import com.simprints.libdata.models.realm.rl_Person
import com.simprints.libdata.tools.Constants
import com.simprints.libdata.tools.Routes.*
import com.simprints.libdata.tools.Utils
import com.simprints.libdata.tools.Utils.wrapCallback
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Verification
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import org.json.JSONException
import timber.log.Timber
import java.io.IOException
import java.io.PipedInputStream
import java.io.PipedOutputStream

class FirebaseManager(private val appContext: Context) : RemoteDbManager {

    // Firebase
    private var firebaseApp: FirebaseApp? = null
    private lateinit var projectRef: DatabaseReference

    // Connection listener
    @Volatile
    private var isRemoteConnected: Boolean = false
    private lateinit var connectionDbRef: DatabaseReference
    private lateinit var connectionDispatcher: ValueEventListener
    private var connectionListeners = mutableSetOf<ConnectionListener>()

    // Authentication listener
    @Volatile
    private var signedIn = false
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var authDispatcher: FirebaseAuth.AuthStateListener
    private var authListeners = mutableSetOf<AuthListener>()

    // Lifecycle
    override fun initialiseRemoteDb(projectId: String) {
        firebaseApp =
            try {
                Timber.d("Trying to initialiseDb Firebase app")
                FirebaseApp.initializeApp(appContext, FirebaseOptions.fromResource(appContext), projectId)
            } catch (stateException: IllegalStateException) {
                Timber.d("Firebase app already initialized")
                FirebaseApp.getInstance(projectId)
            }
                .also {
                    firebaseAuth = FirebaseAuth.getInstance(it)
                }

        Utils.forceSync(firebaseApp)
        projectRef = projectRef(firebaseApp, projectId)

        // Initialize connection listener
        connectionListeners = mutableSetOf()
        connectionDispatcher = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val connected = dataSnapshot.getValue(Boolean::class.java)!!

                synchronized(connectionListeners) {
                    if (connected) {
                        Timber.d("Connected")
                        isRemoteConnected = true
                        for (listener in connectionListeners)
                            listener.onConnection()
                    } else {
                        Timber.d("Disconnected")
                        isRemoteConnected = false
                        for (listener in connectionListeners)
                            listener.onDisconnection()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        }
        connectionDbRef = Utils.getDatabase(firebaseApp).getReference(".info/connected")
        connectionDbRef.addValueEventListener(connectionDispatcher)
        Timber.d("Connection listener set")

        // Initialize auth listener
        authListeners = mutableSetOf()
        authDispatcher = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val fbUser = firebaseAuth.currentUser
            if (fbUser != null) {
                //New user != signed in user
                if (fbUser.uid != projectId) {
                    Timber.d("Signed out by other user")
                    firebaseAuth.signOut()
                    signedIn = false
                } else {
                    Timber.d("Signed in")
                    synchronized(authListeners) {
                        for (authListener in authListeners)
                            authListener.onSignIn()
                    }
                    // setSync()
                } //User is signed in
            } else {
                Timber.d("Signed out")
                synchronized(authListeners) {
                    for (authListener in authListeners)
                        authListener.onSignOut()
                }
                signedIn = false
            } //User is signed out
        }
        firebaseAuth.addAuthStateListener(authDispatcher)
        Timber.d("Auth state listener set")
    }

    override fun signInToRemoteDb(projectId: String, token: Token) {
        // TODO : turn into an RxJava Single Observable
        signedIn = true
        firebaseAuth.signInWithCustomToken(token.value).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                //emitter.onSuccess(token)
                Timber.d("Firebase Auth signInWithCustomToken successful")
            } else {
                //emitter.onError(FirebaseSigninInWithCustomTokenFailed())
                Timber.d("Firebase Auth signInWithCustomToken failed: ${task.exception}")
            }
        }
    }

    override fun signOutOfRemoteDb(projectId: String) {
        firebaseAuth.signOut()

        // Unregister listeners
        firebaseAuth.removeAuthStateListener(authDispatcher)
        connectionDbRef.removeEventListener(connectionDispatcher)
    }

    override fun isRemoteDbInitialized(projectId: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isSignedIn(projectId: String): Boolean =
        signedIn

    override fun isRemoteConnected(): Boolean =
        isRemoteConnected

    override fun registerRemoteAuthListener(authListener: AuthListener) {
        return synchronized(authListeners) {
            authListeners.add(authListener)
        }
    }

    override fun unregisterRemoteAuthListener(authListener: AuthListener) {
        return synchronized(authListeners) {
            authListeners.remove(authListener)
        }
    }

    override fun registerRemoteConnectionListener(connectionListener: ConnectionListener) {
        synchronized(connectionListeners) {
            connectionListeners.add(connectionListener)
        }
    }

    override fun unregisterRemoteConnectionListener(connectionListener: ConnectionListener) {
        synchronized(connectionListeners) {
            connectionListeners.remove(connectionListener)
        }
    }

    // Data transfer

    override fun getLocalDbKeyFromRemote(): String {
        // TODO("not implemented")
        return ""
    }

    override fun savePersonInRemote(fbPerson: fb_Person, projectId: String) {
        val updates = HashMap<String, Any>()
        updates[patientNode(fbPerson.patientId)] = fbPerson.toMap()

        userRef(firebaseApp, projectId, fbPerson.userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue<fb_User>(fb_User::class.java)
                if (user == null) {
                    updates[userNode(fbPerson.userId)] = fb_User(fbPerson.userId, fbPerson.androidId, fbPerson.patientId)
                } else {
                    updates[userPatientListNode(fbPerson.userId, fbPerson.patientId)] = true
                }
                projectRef.updateChildren(updates)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    override fun loadPersonFromRemote(destinationList: MutableList<Person>, guid: String, callback: DataCallback) {
        val wrappedCallback = wrapCallback("FirebaseManager.loadPerson()", callback)

        projectRef.child(patientNode(guid)).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val fbPerson = dataSnapshot.getValue(fb_Person::class.java)

                if (fbPerson == null) {
                    wrappedCallback.onFailure(DATA_ERROR.NOT_FOUND)
                } else {
                    destinationList.add(rl_Person(fbPerson).libPerson)
                    wrappedCallback.onSuccess()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                wrappedCallback.onFailure(DATA_ERROR.NOT_FOUND)
            }
        })
    }

    override fun saveIdentificationInRemote(probe: Person, projectId: String, userId: String, androidId: String, moduleId: String, matchSize: Int, matches: List<Identification>, sessionId: String) {
        idEventRef(firebaseApp, projectId).push().setValue(fb_IdEvent(probe, userId, androidId, moduleId, matchSize, matches, sessionId))
    }

    override fun updateIdentificationInRemote(projectId: String, selectedGuid: String, deviceId: String, sessionId: String) {
        idUpdateRef(projectId).push().setValue(fb_IdEventUpdate(projectId, selectedGuid, deviceId, sessionId))
    }

    override fun saveVerificationInRemote(probe: Person, projectId: String, userId: String, androidId: String, moduleId: String, patientId: String, match: Verification?, sessionId: String, guidExistsResult: VERIFY_GUID_EXISTS_RESULT) {
        vfEventRef(firebaseApp, projectId).push().setValue(fb_VfEvent(probe, userId, androidId, moduleId, patientId, match, sessionId, guidExistsResult))
    }

    override fun saveRefusalFormInRemote(refusalForm: RefusalForm, projectId: String, userId: String, sessionId: String) {
        refusalRef(firebaseApp).push().setValue(fb_RefusalForm(refusalForm, projectId, userId, sessionId))
    }

    override fun saveSessionInRemote(session: Session) {
        val task = sessionRef(firebaseApp).push().setValue(session.toFirebaseSession())
        Tasks.await(task)
    }

    override fun getSyncManager(projectId: String): NaiveSyncManager {
        firebaseApp?.let { firebaseApp ->
            return NaiveSyncManager(firebaseApp, projectId)
        } ?: throw FirebaseUninitialisedError()
    }

    override fun recoverLocalDbSendToRemote(projectId: String, userId: String, androidId: String, moduleId: String, group: Constants.GROUP, callback: DataCallback) {
        // TODO : factor into a class
        Timber.d("FirebaseManager.recoverRealmDb()")
        val wrappedCallback = wrapCallback("FirebaseManager.recoverRealmDb", callback)

        val storageBucketUrl = "gs://${BuildConfig.GCP_PROJECT}-firebase-storage/"

        // Create a new Realm instance - needed since this should rn on a background thread
        val mRealm = Realm.getInstance(RealmConfig.get(projectId))

        val request = when (group) {
            Constants.GROUP.GLOBAL -> mRealm.where(rl_Person::class.java).findAllAsync()
            Constants.GROUP.USER -> mRealm.where(rl_Person::class.java).equalTo("userId", userId).findAllAsync()
            Constants.GROUP.MODULE -> mRealm.where(rl_Person::class.java).equalTo("moduleId", moduleId).findAllAsync()
        }

        // Prepare and connect the streams
        val realmDbInputStream = PipedInputStream()
        val realmDbOutputStream = PipedOutputStream()

        try {
            realmDbOutputStream.connect(realmDbInputStream)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // Once the realm request is processed
        request.addChangeListener(object : RealmChangeListener<RealmResults<rl_Person>> {
            override fun onChange(results: RealmResults<rl_Person>) {
                try {
                    request.removeChangeListener(this)
                    // Write the JSON people and make sure it's valid JSON
                    realmDbOutputStream.write("{".toByteArray())
                    for ((count, person) in results.withIndex()) {
                        val personJson = person.jsonPerson
                        var commaString = ","
                        // We need commas before all entries except the first
                        if (count == 0) {
                            commaString = ""
                        }
                        val nodeString = "\"" + personJson.get("patientId").toString() + "\"" + ":"
                        val personJsonString = commaString + nodeString + person.jsonPerson.toString()
                        // Send the JSON person to the Output stream
                        realmDbOutputStream.write(personJsonString.toByteArray())
                    }
                    realmDbOutputStream.write("}".toByteArray())
                    // Once we're finished writing to the output stream we can close it & the realm reference
                    realmDbOutputStream.close()
                    mRealm.close()
                } catch (e: JSONException) {
                    e.printStackTrace()
                    wrappedCallback.onFailure(DATA_ERROR.JSON_ERROR)
                } catch (e: IOException) {
                    e.printStackTrace()
                    wrappedCallback.onFailure(DATA_ERROR.IO_BUFFER_WRITE_ERROR)
                }
            }
        })

        // Get a reference to [bucket]/recovered-realm-dbs/[projectKey]/[userId]/[db-name].json
        firebaseApp?.let { firebaseApp ->
            val storage = FirebaseStorage.getInstance(firebaseApp)
            val rootRef = storage.getReferenceFromUrl(storageBucketUrl)
            val recoveredRealmDbsRef = rootRef.child("recovered-realm-dbs")
            val projectPathRef = recoveredRealmDbsRef.child(projectId)
            val userPathRef = projectPathRef.child(userId)
            val fileReference = userPathRef.child("recovered-realm-db")

            // Create some metadata to add to the JSON file
            val metadata = StorageMetadata.Builder()
                .setCustomMetadata("projectId", projectId)
                .setCustomMetadata("userId", userId)
                .setCustomMetadata("androidId", androidId)
                .build()

            // Begin the upload task to the reference using the input stream & metadata
            val uploadTask = fileReference.putStream(realmDbInputStream, metadata)
            uploadTask.addOnFailureListener { e: Exception ->
                // Try to close the stream and send the onFailure callback
                e.printStackTrace()
                try {
                    realmDbInputStream.close()
                } catch (e1: IOException) {
                    e1.printStackTrace()
                }

                wrappedCallback.onFailure(DATA_ERROR.FAILED_TO_UPLOAD)
            }.addOnSuccessListener {
                    // Try to close the stream and send the onSuccess callback
                    try {
                        realmDbInputStream.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    wrappedCallback.onSuccess()
                }.addOnProgressListener { taskSnapshot: UploadTask.TaskSnapshot ->
                    Timber.d("Realm DB upload progress: ${taskSnapshot.bytesTransferred}")
                }
        } ?: throw FirebaseUninitialisedError()
    }
}
