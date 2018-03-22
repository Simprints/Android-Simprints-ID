package com.simprints.id.data.db.remote

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.simprints.id.Application
import com.simprints.id.data.db.DATA_ERROR
import com.simprints.id.data.db.DataCallback
import com.simprints.id.data.db.local.LocalDbKey
import com.simprints.id.data.db.local.models.rl_Person
import com.simprints.id.data.db.remote.adapters.toFirebaseSession
import com.simprints.id.data.db.remote.authListener.RemoteDbAuthListenerManager
import com.simprints.id.data.db.remote.connectionListener.RemoteDbConnectionListenerManager
import com.simprints.id.data.db.remote.enums.VERIFY_GUID_EXISTS_RESULT
import com.simprints.id.data.db.remote.models.*
import com.simprints.id.data.db.remote.tools.Routes.*
import com.simprints.id.data.db.remote.tools.Utils
import com.simprints.id.data.db.remote.tools.Utils.wrapCallback
import com.simprints.id.data.db.sync.SyncApiInterface
import com.simprints.id.exceptions.unsafe.CouldNotRetrieveLocalDbKeyError
import com.simprints.id.exceptions.unsafe.DbAlreadyInitialisedError
import com.simprints.id.exceptions.unsafe.RemoteDbNotSignedInError
import com.simprints.id.network.SimApiClient
import com.simprints.id.secure.cryptography.Hasher
import com.simprints.id.secure.models.Tokens
import com.simprints.id.session.Session
import com.simprints.id.tools.JsonHelper
import com.simprints.id.tools.extensions.deviceId
import com.simprints.libcommon.Person
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Verification
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.SingleEmitter
import org.jetbrains.anko.doAsync
import timber.log.Timber

class FirebaseManager(private val appContext: Context,
                      firebaseConnectionListenerManager: RemoteDbConnectionListenerManager,
                      firebaseAuthListenerManager: RemoteDbAuthListenerManager,
                      private val firebaseOptionsHelper: FirebaseOptionsHelper = FirebaseOptionsHelper(appContext)) :
    RemoteDbManager,
    RemoteDbConnectionListenerManager by firebaseConnectionListenerManager,
    RemoteDbAuthListenerManager by firebaseAuthListenerManager {

    private var isInitialised = false

    // FirebaseApp
    private lateinit var legacyFirebaseApp: FirebaseApp
    private lateinit var firestoreFirebaseApp: FirebaseApp

    // Lifecycle
    override fun initialiseRemoteDb() {
        if (isInitialised) throw DbAlreadyInitialisedError()
        initialiseLegacyFirebaseProject()
        initialiseFirestoreFirebaseProject()
        attachConnectionListeners(legacyFirebaseApp)
        attachAuthListeners(getFirebaseAuth(legacyFirebaseApp))
        isInitialised = true
    }

    private fun initialiseLegacyFirebaseProject() {
        val legacyFirebaseAppName = getLegacyAppName()
        val legacyFirebaseOptions = firebaseOptionsHelper.getLegacyFirebaseOptions()
        legacyFirebaseApp = initialiseFirebaseApp(legacyFirebaseAppName, legacyFirebaseOptions)

        Utils.forceSync(legacyFirebaseApp)
    }

    private fun initialiseFirestoreFirebaseProject() {
        val firestoreFirebaseAppName = getFirestoreAppName()
        val firestoreFirebaseOptions = firebaseOptionsHelper.getFirestoreFirebaseOptions()
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

    override fun signInToRemoteDb(tokens: Tokens): Completable =
        Completable.mergeArray(
            signInToDb(legacyFirebaseApp, tokens.legacyToken),
            signInToDb(firestoreFirebaseApp, tokens.firestoreToken))

    private fun signInToDb(firebaseApp: FirebaseApp, token: String): Completable =
        Completable.create {
            getFirebaseAuth(firebaseApp).signInWithCustomToken(token).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Timber.d("Firebase Auth signInWithCustomToken for ${firebaseApp.name} successful")
                    it.onComplete()
                } else {
                    Timber.d("Firebase Auth signInWithCustomToken for ${firebaseApp.name} failed: ${task.exception}")
                    it.onError(task.exception as Throwable)
                }
            }
        }

    override fun signOutOfRemoteDb() {
        getFirebaseAuth(legacyFirebaseApp).signOut()
        getFirebaseAuth(firestoreFirebaseApp).signOut()

        detachConnectionListeners(legacyFirebaseApp)
        detachAuthListeners(getFirebaseAuth(legacyFirebaseApp))
    }

    override fun isRemoteDbInitialized(): Boolean = isInitialised

    override fun isSignedIn(projectId: String, userId: String): Boolean =
        isFirestoreSignedInUserAsExpected(projectId, userId) &&
            isLegacySignedInUserAsExpected(projectId)

    private fun isFirestoreSignedInUserAsExpected(projectId: String, userId: String): Boolean {
        val firestoreUser = getFirebaseAuth(firestoreFirebaseApp).currentUser ?: return false
        return firestoreUser.uid == "$projectId.$userId"
    }

    private fun isLegacySignedInUserAsExpected(projectId: String): Boolean {
        val firebaseUser = getFirebaseAuth(legacyFirebaseApp).currentUser ?: return false

        // For legacy reason, the firebase user has the projectId(for new projects) or legacyApiKey
        // (for old projects) as uid. Because the legacyApiKey soon will disappear, we try to map it immediately (CheckLogin)
        // to a projectId and use it for any task. In this case, we need the legacyApiKey
        // so we grab through the Application to avoid injecting it through all methods, so it will be easier
        // to get rid of it.
        val hashedLegacyApiKey = (appContext as Application).secureDataManager.getHashedLegacyProjectIdForProjectIdOrEmpty(projectId)
        return if (hashedLegacyApiKey.isNotEmpty()) {
            Hasher().hash(firebaseUser.uid) == hashedLegacyApiKey
        } else {
            firebaseUser.uid == projectId
        }
    }

    // Data transfer
    override fun getLocalDbKeyFromRemote(projectId: String): Single<LocalDbKey> =
        Single.create<LocalDbKey> { result ->
            val db = FirebaseFirestore.getInstance(firestoreFirebaseApp)
            db.collection(COLLECTION_LOCAL_DB_KEYS)
                .whereEqualTo(PROJECT_ID_FIELD, projectId)
                .get()
                .addOnCompleteListener {
                    handleGetLocalDbKeyTaskComplete(it, result)
                }
        }

    private fun handleGetLocalDbKeyTaskComplete(task: Task<QuerySnapshot>, result: SingleEmitter<LocalDbKey>) {
        if (task.isSuccessful) {
            val localDbKeyValue = task.result.first().getBlob(LOCAL_DB_KEY_VALUE_NAME).toBytes()
            result.onSuccess(LocalDbKey(localDbKeyValue))
        } else {
            result.onError(CouldNotRetrieveLocalDbKeyError.withException(task.exception))
        }
    }

    override fun savePersonInRemote(fbPerson: fb_Person): Completable {
        // TODO : Implement sending the person to our own custom end point
        val projectId = fbPerson.projectId
        val updates = mutableMapOf<String, Any>(patientNode(fbPerson.patientId) to fbPerson.toMap())

        userRef(legacyFirebaseApp, projectId, fbPerson.userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(fb_User::class.java)
                if (user == null) {
                    updates[userNode(fbPerson.userId)] = fb_User(fbPerson.userId, appContext.deviceId, fbPerson.patientId)
                } else {
                    updates[userPatientListNode(fbPerson.userId, fbPerson.patientId)] = true
                }
                projectRef(legacyFirebaseApp, projectId).updateChildren(updates)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
        return Completable.complete()
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
        idEventRef(legacyFirebaseApp, projectId).push().setValue(fb_IdEvent(probe, projectId, userId, androidId, moduleId, matchSize, matches, sessionId))
    }

    override fun updateIdentificationInRemote(projectId: String, selectedGuid: String, deviceId: String, sessionId: String) {
        idUpdateRef(projectId).push().setValue(fb_IdEventUpdate(projectId, selectedGuid, deviceId, sessionId))
    }

    override fun saveVerificationInRemote(probe: Person, projectId: String, userId: String, androidId: String, moduleId: String, patientId: String, match: Verification?, sessionId: String, guidExistsResult: VERIFY_GUID_EXISTS_RESULT) {
        vfEventRef(legacyFirebaseApp, projectId).push().setValue(fb_VfEvent(probe, projectId, userId, androidId, moduleId, patientId, match, sessionId, guidExistsResult))
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

    override fun getCurrentFirestoreToken(): Single<String> = Single.create {
        getFirebaseLegacyApp().getToken(false)
            .addOnSuccessListener { result ->
                // Firebase callbacks return on main thread, so the emits
                // will be in the same thread.
                doAsync {
                    val token = result.token
                    if (token == null) {
                        throw RemoteDbNotSignedInError()
                    } else {
                        it.onSuccess(token)
                    }
                }
            }
            .addOnFailureListener { e -> it.onError(e) }
    }

    override fun getSyncApi(): Single<SyncApiInterface> =
        getCurrentFirestoreToken()
            .flatMap { token: String ->
                Single.just(SimApiClient(SyncApiInterface::class.java, SyncApiInterface.baseUrl, token).api)
            }

    override fun uploadPeopleBatch(patientsToUpload: ArrayList<fb_Person>): Completable =
        getSyncApi().flatMapCompletable {
            it.upSync(JsonHelper.gson.toJson(mapOf("patients" to patientsToUpload)))
                .retry(RETRY_ATTEMPTS_FOR_NETWORK_CALLS)
        }

    override fun downloadPatient(patientId: String): Single<fb_Person> =
        getSyncApi().flatMap {
            it.getPatient(patientId).retry(RETRY_ATTEMPTS_FOR_NETWORK_CALLS)
        }

    companion object {
        private const val COLLECTION_LOCAL_DB_KEYS = "localDbKeys"
        private const val PROJECT_ID_FIELD = "projectId"
        private const val RETRY_ATTEMPTS_FOR_NETWORK_CALLS = 5L

        private const val LOCAL_DB_KEY_VALUE_NAME = "value"

        fun getLegacyAppName(): String =
            FirebaseApp.DEFAULT_APP_NAME

        fun getFirestoreAppName(): String =
            "firestore"
    }
}
