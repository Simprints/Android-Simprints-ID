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
import com.simprints.id.exceptions.safe.DifferentProjectInitialisedException
import com.simprints.id.exceptions.safe.DifferentProjectSignedInException
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

class FirebaseManager(private val appContext: Context,
                      firebaseConnectionListenerManager: RemoteDbConnectionListenerManager,
                      firebaseAuthListenerManager: RemoteDbAuthListenerManager) :
    RemoteDbManager,
    RemoteDbConnectionListenerManager by firebaseConnectionListenerManager,
    RemoteDbAuthListenerManager by firebaseAuthListenerManager {

    private var isInitialised = false

    // FirebaseApp Names
    private lateinit var legacyFirebaseAppName: String
    private lateinit var firestoreFirebaseAppName: String

    // FirebaseApp
    private lateinit var legacyFirebaseApp: FirebaseApp
    private lateinit var firestoreFirebaseApp: FirebaseApp

    // FirebaseAuth
    private lateinit var legacyFirebaseAuth: FirebaseAuth
    private lateinit var firestoreFirebaseAuth: FirebaseAuth

    // Routes
    private lateinit var projectRef: DatabaseReference

    // Lifecycle
    override fun initialiseRemoteDb(projectId: String) {
        initialiseLegacyFirebaseApp(projectId)
        initialiseFirestoreFirebaseApp(projectId)
        applyConnectionListeners(legacyFirebaseApp)
        applyAuthListeners(legacyFirebaseAuth, legacyFirebaseAppName)
        isInitialised = true
    }

    private fun initialiseLegacyFirebaseApp(projectId: String) {
        legacyFirebaseAppName = getLegacyAppNameFromProjectId(projectId)
        legacyFirebaseApp = initialiseFirebaseApp(legacyFirebaseAppName)
        legacyFirebaseAuth = initialiseFirebaseAuth(legacyFirebaseApp)

        Utils.forceSync(legacyFirebaseApp)
        projectRef = projectRef(legacyFirebaseApp, projectId)
    }

    private fun initialiseFirestoreFirebaseApp(projectId: String) {
        firestoreFirebaseAppName = getFirestoreAppNameFromProjectId(projectId)
        firestoreFirebaseApp = initialiseFirebaseApp(firestoreFirebaseAppName)
        firestoreFirebaseAuth = initialiseFirebaseAuth(firestoreFirebaseApp)
    }

    private fun initialiseFirebaseApp(appName: String): FirebaseApp =
        try {
            Timber.d("Trying to initialise Firebase app: $appName")
            FirebaseApp.initializeApp(appContext, FirebaseOptions.fromResource(appContext), appName)
        } catch (stateException: IllegalStateException) {
            Timber.d("Firebase app: $appName already initialized")
            FirebaseApp.getInstance(appName)
        }

    private fun initialiseFirebaseAuth(firebaseApp: FirebaseApp): FirebaseAuth =
        FirebaseAuth.getInstance(firebaseApp)

    override fun signInToRemoteDb(projectId: String, token: Token) {
        // TODO : turn into an RxJava Single Observable
        legacyFirebaseAuth.signInWithCustomToken(token.value).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                //emitter.onSuccess(token)
                Timber.d("Firebase Auth signInWithCustomToken successful")
            } else {
                //emitter.onError(FirebaseSignInInWithCustomTokenFailed())
                Timber.d("Firebase Auth signInWithCustomToken failed: ${task.exception}")
            }
        }
    }

    override fun signOutOfRemoteDb(projectId: String) {
        legacyFirebaseAuth.signOut()
        firestoreFirebaseAuth.signOut()

        removeConnectionListeners(legacyFirebaseApp)
        removeAuthListeners(legacyFirebaseAuth)
    }

    @Throws(DifferentProjectInitialisedException::class)
    override fun isRemoteDbInitialized(projectId: String): Boolean {
        return if (isInitialised) {
            if (legacyFirebaseApp.name == getLegacyAppNameFromProjectId(projectId))
                true
            else
                throw DifferentProjectInitialisedException()
        } else false
    }

    @Throws(DifferentProjectSignedInException::class)
    override fun isSignedIn(projectId: String): Boolean {
        return if (isSignedIn) {
            isSignedInUserSameAsProjectId(projectId)
        } else false
    }

    private fun isSignedInUserSameAsProjectId(projectId: String): Boolean {
        val firebaseUser = legacyFirebaseAuth.currentUser
        firebaseUser?.let {
            if (it.uid == getLegacyAppNameFromProjectId(projectId)) {
                return true
            } else throw DifferentProjectSignedInException()
        } ?: return false
    }

    // Data transfer

    override fun getLocalDbKeyFromRemote(): String {
        // TODO("not implemented")
        return ""
    }

    override fun savePersonInRemote(fbPerson: fb_Person, projectId: String) {
        val updates = HashMap<String, Any>()
        updates[patientNode(fbPerson.patientId)] = fbPerson.toMap()

        userRef(legacyFirebaseApp, projectId, fbPerson.userId).addListenerForSingleValueEvent(object : ValueEventListener {
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
        idEventRef(legacyFirebaseApp, projectId).push().setValue(fb_IdEvent(probe, userId, androidId, moduleId, matchSize, matches, sessionId))
    }

    override fun updateIdentificationInRemote(projectId: String, selectedGuid: String, deviceId: String, sessionId: String) {
        idUpdateRef(projectId).push().setValue(fb_IdEventUpdate(projectId, selectedGuid, deviceId, sessionId))
    }

    override fun saveVerificationInRemote(probe: Person, projectId: String, userId: String, androidId: String, moduleId: String, patientId: String, match: Verification?, sessionId: String, guidExistsResult: VERIFY_GUID_EXISTS_RESULT) {
        vfEventRef(legacyFirebaseApp, projectId).push().setValue(fb_VfEvent(probe, userId, androidId, moduleId, patientId, match, sessionId, guidExistsResult))
    }

    override fun saveRefusalFormInRemote(refusalForm: RefusalForm, projectId: String, userId: String, sessionId: String) {
        refusalRef(legacyFirebaseApp).push().setValue(fb_RefusalForm(refusalForm, projectId, userId, sessionId))
    }

    override fun saveSessionInRemote(session: Session) {
        val task = sessionRef(legacyFirebaseApp).push().setValue(session.toFirebaseSession())
        Tasks.await(task)
    }

    override fun getSyncManager(projectId: String): NaiveSyncManager =
        NaiveSyncManager(legacyFirebaseApp, projectId)

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
        legacyFirebaseApp.let { firebaseApp ->
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

    companion object {
        fun getLegacyAppNameFromProjectId(projectId: String): String =
            projectId

        fun getFirestoreAppNameFromProjectId(projectId: String): String =
            projectId + "-fs"
    }
}
