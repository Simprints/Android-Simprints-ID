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
import com.simprints.id.data.db.remote.adapters.toFirebaseSession
import com.simprints.id.data.models.Session
import com.simprints.id.exceptions.safe.DifferentProjectInitialisedException
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

    override fun signInToRemoteDb(projectId: String, tokens: Tokens) {
        // TODO : turn into an RxJava Single Observable
        legacyFirebaseAuth.signInWithCustomToken(tokens.legacyToken).addOnCompleteListener { task ->
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

    fun getFirebaseStorageInstance() = FirebaseStorage.getInstance(legacyFirebaseApp)

    companion object {
        fun getLegacyAppNameFromProjectId(projectId: String): String =
            projectId

        fun getFirestoreAppNameFromProjectId(projectId: String): String =
            projectId + "-fs"
    }
}
