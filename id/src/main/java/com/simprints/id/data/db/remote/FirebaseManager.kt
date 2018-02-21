package com.simprints.id.data.db.remote

import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.simprints.id.data.db.remote.adapters.toFirebaseSession
import com.simprints.id.data.models.Session
import com.simprints.id.exceptions.safe.DifferentProjectSignedInException
import com.simprints.id.secure.models.Tokens
import com.simprints.libcommon.Person
import com.simprints.libdata.DATA_ERROR
import com.simprints.libdata.DataCallback
import com.simprints.libdata.NaiveSyncManager
import com.simprints.libdata.models.enums.VERIFY_GUID_EXISTS_RESULT
import com.simprints.libdata.models.firebase.*
import com.simprints.libdata.models.realm.rl_Person
import com.simprints.libdata.tools.Routes.*
import com.simprints.libdata.tools.Utils
import com.simprints.libdata.tools.Utils.wrapCallback
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Verification
import timber.log.Timber

class FirebaseManager(private val appContext: Context,
                      firebaseConnectionListenerManager: RemoteDbConnectionListenerManager,
                      firebaseAuthListenerManager: RemoteDbAuthListenerManager,
                      private val firebaseOptionsHelper: FirebaseOptionsHelper = FirebaseOptionsHelper(appContext)) :
    RemoteDbManager,
    RemoteDbConnectionListenerManager by firebaseConnectionListenerManager,
    RemoteDbAuthListenerManager by firebaseAuthListenerManager {

    private var isInitialised = false

    // FirebaseApp Names
    private lateinit var legacyFirebaseAppName: String
    private lateinit var firestoreFirebaseAppName: String

    // FirebaseOptions
    private lateinit var legacyFirebaseOptions: FirebaseOptions
    private lateinit var firestoreFirebaseOptions: FirebaseOptions

    // FirebaseApp
    private lateinit var legacyFirebaseApp: FirebaseApp
    private lateinit var firestoreFirebaseApp: FirebaseApp

    // FirebaseAuth
    private lateinit var legacyFirebaseAuth: FirebaseAuth
    private lateinit var firestoreFirebaseAuth: FirebaseAuth

    // Lifecycle
    override fun initialiseRemoteDb() {
        initialiseLegacyFirebaseProject()
        initialiseFirestoreFirebaseProject()
        applyConnectionListeners(legacyFirebaseApp)
        applyAuthListeners(legacyFirebaseAuth)
        isInitialised = true
    }

    private fun initialiseLegacyFirebaseProject() {
        legacyFirebaseAppName = getLegacyAppName()
        legacyFirebaseOptions = firebaseOptionsHelper.getLegacyFirebaseOptions()
        legacyFirebaseApp = initialiseFirebaseApp(legacyFirebaseAppName, legacyFirebaseOptions)
        legacyFirebaseAuth = initialiseFirebaseAuth(legacyFirebaseApp)

        Utils.forceSync(legacyFirebaseApp)
    }

    private fun initialiseFirestoreFirebaseProject() {
        firestoreFirebaseAppName = getFirestoreAppName()
        firestoreFirebaseOptions = firebaseOptionsHelper.getFirestoreFirebaseOptions()
        firestoreFirebaseApp = initialiseFirebaseApp(firestoreFirebaseAppName, firestoreFirebaseOptions)
        firestoreFirebaseAuth = initialiseFirebaseAuth(firestoreFirebaseApp)
    }

    private fun initialiseFirebaseApp(appName: String, firebaseOptions: FirebaseOptions): FirebaseApp =
        try {
            Timber.d("Trying to initialise Firebase app: $appName")
            FirebaseApp.initializeApp(appContext, firebaseOptions, appName)
        } catch (stateException: IllegalStateException) {
            Timber.d("Firebase app: $appName already initialized")
            FirebaseApp.getInstance(appName)
        }

    private fun initialiseFirebaseAuth(firebaseApp: FirebaseApp): FirebaseAuth =
        FirebaseAuth.getInstance(firebaseApp)

    override fun signInToRemoteDb(tokens: Tokens) {
        signInToLegacyDb(tokens.legacyToken)
        signInToFirestoreDb(tokens.firestoreToken)
    }

    private fun signInToLegacyDb(legacyToken: String) {
        legacyFirebaseAuth.signInWithCustomToken(legacyToken).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Timber.d("Firebase Auth signInWithCustomToken successful")
            } else {
                Timber.d("Firebase Auth signInWithCustomToken failed: ${task.exception}")
            }
        }
    }

    private fun signInToFirestoreDb(firestoreToken: String) {
        firestoreFirebaseAuth.signInWithCustomToken(firestoreToken).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Timber.d("Firebase Auth signInWithCustomToken successful")
            } else {
                Timber.d("Firebase Auth signInWithCustomToken failed: ${task.exception}")
            }
        }
    }

    override fun signOutOfRemoteDb() {
        legacyFirebaseAuth.signOut()
        firestoreFirebaseAuth.signOut()

        removeConnectionListeners(legacyFirebaseApp)
        removeAuthListeners(legacyFirebaseAuth)
    }

    override fun isRemoteDbInitialized(): Boolean = isInitialised

    @Throws(DifferentProjectSignedInException::class)
    override fun isSignedIn(projectId: String, userId: String): Boolean {
        return if (isSignedIn) {
            isSignedInUserAsExpected(projectId, userId)
        } else false
    }

    private fun isSignedInUserAsExpected(projectId: String, userId: String): Boolean {
        val firebaseUser = legacyFirebaseAuth.currentUser
        firebaseUser?.let {
            if (isFirebaseUserAsExpected(it, projectId, userId)) {
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
                projectRef(legacyFirebaseApp, projectId).updateChildren(updates)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    override fun loadPersonFromRemote(destinationList: MutableList<Person>, projectId: String, guid: String, callback: DataCallback) {
        val wrappedCallback = wrapCallback("FirebaseManager.loadPerson()", callback)

        projectRef(legacyFirebaseApp, projectId).child(patientNode(guid)).addListenerForSingleValueEvent(object : ValueEventListener {
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

    fun getFirebaseStorageInstance() = FirebaseStorage.getInstance(legacyFirebaseApp)

    companion object {

        fun getLegacyAppName(): String =
            FirebaseApp.DEFAULT_APP_NAME

        fun getFirestoreAppName(): String =
            "firestore"

        fun isFirebaseUserAsExpected(firebaseUser: FirebaseUser, projectId: String, userId: String): Boolean =
            firebaseUser.uid == getFirebaseUid(projectId, userId)

        private fun getFirebaseUid(projectId: String, userId: String): String =
            "$projectId.$userId"
    }
}
