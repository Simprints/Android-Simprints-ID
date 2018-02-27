package com.simprints.id.data.db.remote

import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.simprints.id.Application
import com.simprints.id.data.db.local.LocalDbKey
import com.simprints.id.data.db.remote.adapters.toFirebaseSession
import com.simprints.id.data.models.Session
import com.simprints.id.secure.models.Tokens
import com.simprints.libcommon.Person
import com.simprints.libdata.DATA_ERROR
import com.simprints.libdata.DataCallback
import com.simprints.libdata.models.enums.VERIFY_GUID_EXISTS_RESULT
import com.simprints.libdata.models.firebase.*
import com.simprints.libdata.models.realm.rl_Person
import com.simprints.libdata.tools.Routes.*
import com.simprints.libdata.tools.Utils
import com.simprints.libdata.tools.Utils.wrapCallback
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Verification
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
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

    // Lifecycle
    override fun initialiseRemoteDb() {
        initialiseLegacyFirebaseProject()
        initialiseFirestoreFirebaseProject()
        applyConnectionListeners(legacyFirebaseApp)
        applyAuthListeners(getFirebaseAuth(legacyFirebaseApp))
        isInitialised = true
    }

    private fun initialiseLegacyFirebaseProject() {
        legacyFirebaseAppName = getLegacyAppName()
        legacyFirebaseOptions = firebaseOptionsHelper.getLegacyFirebaseOptions()
        legacyFirebaseApp = initialiseFirebaseApp(legacyFirebaseAppName, legacyFirebaseOptions)

        Utils.forceSync(legacyFirebaseApp)
    }

    private fun initialiseFirestoreFirebaseProject() {
        firestoreFirebaseAppName = getFirestoreAppName()
        firestoreFirebaseOptions = firebaseOptionsHelper.getFirestoreFirebaseOptions()
        firestoreFirebaseApp = initialiseFirebaseApp(firestoreFirebaseAppName, firestoreFirebaseOptions)
    }

    private fun initialiseFirebaseApp(appName: String, firebaseOptions: FirebaseOptions): FirebaseApp =
        try {
            Timber.d("Trying to initialise Firebase app: $appName")
            FirebaseApp.initializeApp(appContext, firebaseOptions, appName)
        } catch (stateException: IllegalStateException) {
            Timber.d("Firebase app: $appName already initialized")
            FirebaseApp.getInstance(appName)
        }

    private fun getFirebaseAuth(firebaseApp: FirebaseApp): FirebaseAuth =
        FirebaseAuth.getInstance(firebaseApp)

    override fun signInToRemoteDb(tokens: Tokens): Single<Unit> =
        Singles.zip(signInToDb(legacyFirebaseApp, tokens.legacyToken), signInToDb(firestoreFirebaseApp, tokens.firestoreToken)).map { Unit }

    private fun signInToDb(firebaseApp: FirebaseApp, token: String): Single<Unit> =
        Single.create<Unit> {
            getFirebaseAuth(firebaseApp).signInWithCustomToken(token).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Timber.d("Firebase Auth signInWithCustomToken for ${firebaseApp.name} successful")
                    it.onSuccess(Unit)
                } else {
                    Timber.d("Firebase Auth signInWithCustomToken for ${firebaseApp.name} failed: ${task.exception}")
                    it.onError(task.exception as Throwable)
                }
            }
        }

    override fun signOutOfRemoteDb() {
        getFirebaseAuth(legacyFirebaseApp).signOut()
        getFirebaseAuth(firestoreFirebaseApp).signOut()

        removeConnectionListeners(legacyFirebaseApp)
        removeAuthListeners(getFirebaseAuth(legacyFirebaseApp))
    }

    override fun isRemoteDbInitialized(): Boolean = isInitialised

    override fun isSignedIn(projectId: String, userId: String): Boolean =
        isFirestoreSignedInUserAsExpected(projectId, userId) &&
        isFirebaseSignedInUserAsExpected(projectId)

    private fun isFirestoreSignedInUserAsExpected (projectId: String, userId: String): Boolean {
        val firestoreUser = getFirebaseAuth(firestoreFirebaseApp).currentUser ?: return false
        return firestoreUser.uid == "$projectId.$userId"
    }

    private fun isFirebaseSignedInUserAsExpected (projectId: String): Boolean {
        val firebaseUser = getFirebaseAuth(legacyFirebaseApp).currentUser ?: return false

        // For legacy reason, the firebase user has the projectId(for new projects) or legacyApiKey
        // (for old projects) as uid. Because the legacyApiKey soon will disappear, we try to map it immediately (CheckLogin)
        // to a projectId and use it for any task. In this case, we need the legacyApiKey
        // so we grab through the Application to avoid injecting it through all methods, so it will be easier
        // to get rid of it.
        val legacyApiKey = (appContext as Application).secureDataManager.legacyApiKeyForProjectIdOrEmpty(projectId)
        return firebaseUser.uid == if (legacyApiKey.isNotEmpty()) legacyApiKey else projectId
    }

    // Data transfer
    override fun getLocalDbKeyFromRemote(projectId: String): Single<String> =
        Single.create<String> { resultEmit ->
            val db = FirebaseFirestore.getInstance(firestoreFirebaseApp)
            db.collection(COLLECTION_LOCAL_DB_KEYS)
                .whereEqualTo(PROJECT_ID_FIELD, projectId).get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val document = it.result.first().toObject(LocalDbKey::class.java)
                    resultEmit.onSuccess(document.value)
                } else {
                    resultEmit.onError(it.exception as Throwable)
                }
            }
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

    fun getFirebaseStorageInstance() = FirebaseStorage.getInstance(legacyFirebaseApp)
    override fun getFirebaseLegacyApp(): FirebaseApp = legacyFirebaseApp

    companion object {
        private const val COLLECTION_LOCAL_DB_KEYS: String = "localDbKeys"
        private const val PROJECT_ID_FIELD: String = "projectId"

        fun getLegacyAppName(): String =
            FirebaseApp.DEFAULT_APP_NAME

        fun getFirestoreAppName(): String =
            "firestore"
    }
}
