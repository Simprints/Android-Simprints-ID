package com.simprints.id.data.db.remote

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.simprints.id.Application
import com.simprints.id.data.db.LocalDbKeyProvider
import com.simprints.id.data.db.local.LocalDbKey
import com.simprints.id.data.db.remote.adapters.toFirebaseSession
import com.simprints.id.data.db.remote.authListener.RemoteDbAuthListenerManager
import com.simprints.id.data.db.remote.connectionListener.RemoteDbConnectionListenerManager
import com.simprints.id.data.db.remote.enums.VERIFY_GUID_EXISTS_RESULT
import com.simprints.id.data.db.remote.models.*
import com.simprints.id.data.db.remote.adapters.toLocalDbKey
import com.simprints.id.data.db.remote.tools.Routes.*
import com.simprints.id.data.db.remote.tools.Utils
import com.simprints.id.data.db.remote.network.RemoteApiInterface
import com.simprints.id.exceptions.unsafe.CouldNotRetrieveLocalDbKeyError
import com.simprints.id.exceptions.unsafe.DbAlreadyInitialisedError
import com.simprints.id.exceptions.safe.data.db.DownloadingAPersonWhoDoesntExistOnServerException
import com.simprints.id.exceptions.unsafe.RemoteDbNotSignedInError
import com.simprints.id.network.SimApiClient
import com.simprints.id.secure.cryptography.Hasher
import com.simprints.id.secure.models.Tokens
import com.simprints.id.services.sync.SyncTaskParameters
import com.simprints.id.session.Session
import com.simprints.id.tools.extensions.toMap
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
    RemoteDbAuthListenerManager by firebaseAuthListenerManager,
    LocalDbKeyProvider {

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
    // Firebase
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
            val realmKeys = task.result.first().toObject(fs_RealmKeys::class.java)
            result.onSuccess(realmKeys.toLocalDbKey())
        } else {
            result.onError(CouldNotRetrieveLocalDbKeyError.withException(task.exception))
        }
    }

    override fun saveIdentificationInRemote(probe: Person, projectId: String, userId: String, androidId: String, moduleId: String, matchSize: Int, matches: List<Identification>, sessionId: String) {
        idEventRef(legacyFirebaseApp, projectId).push().setValue(fb_IdEvent(probe, projectId, userId, moduleId, matchSize, matches, sessionId).toMap())
    }

    override fun updateIdentificationInRemote(projectId: String, selectedGuid: String, deviceId: String, sessionId: String) {
        idUpdateRef(projectId).push().setValue(fb_IdEventUpdate(projectId, selectedGuid, deviceId, sessionId))
    }

    override fun saveVerificationInRemote(probe: Person, projectId: String, userId: String, androidId: String, moduleId: String, patientId: String, match: Verification?, sessionId: String, guidExistsResult: VERIFY_GUID_EXISTS_RESULT) {
        vfEventRef(legacyFirebaseApp, projectId).push().setValue(fb_VfEvent(probe, projectId, userId, moduleId, patientId, match, sessionId, guidExistsResult).toMap())
    }

    override fun saveRefusalFormInRemote(refusalForm: RefusalForm, projectId: String, userId: String, sessionId: String) {
        refusalRef(legacyFirebaseApp).push().setValue(fb_RefusalForm(refusalForm, projectId, userId, sessionId))
    }

    override fun saveSessionInRemote(session: Session) {
        sessionRef(legacyFirebaseApp).push().setValue(session.toFirebaseSession())
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

    override fun getLocalDbKey(projectId: String): Single<LocalDbKey> =
        getLocalDbKeyFromRemote(projectId)

    // API

    override fun uploadPerson(fbPerson: fb_Person): Completable =
        uploadPeople(arrayListOf(fbPerson))

    override fun uploadPeople(patientsToUpload: ArrayList<fb_Person>): Completable =
        getSyncApi().flatMapCompletable {
            it.uploadPeople(hashMapOf("patients" to patientsToUpload))
                .retry(RETRY_ATTEMPTS_FOR_NETWORK_CALLS)
        }

    override fun downloadPerson(patientId: String, projectId: String): Single<fb_Person> =
        getSyncApi().flatMap {
            it.downloadPeople(patientId, projectId).retry(RETRY_ATTEMPTS_FOR_NETWORK_CALLS)
                .map { if (it.isEmpty())
                    throw DownloadingAPersonWhoDoesntExistOnServerException()
                else it.first()
                }
        }

    override fun getNumberOfPatientsForSyncParams(syncParams: SyncTaskParameters): Single<Int> =
        getSyncApi().flatMap {
            it.peopleCount(syncParams.toMap())
                .retry(RETRY_ATTEMPTS_FOR_NETWORK_CALLS)
                .map { it.count }
        }

    override fun getSyncApi(): Single<RemoteApiInterface> =
        getCurrentFirestoreToken()
            .flatMap {
                Single.just(getApiClient(it))
            }

    private fun getApiClient(authToken: String): RemoteApiInterface =
        SimApiClient(RemoteApiInterface::class.java, RemoteApiInterface.baseUrl, authToken).api

    companion object {
        private const val COLLECTION_LOCAL_DB_KEYS = "localDbKeys"
        private const val PROJECT_ID_FIELD = "projectId"
        private const val LOCAL_DB_KEY_VALUE_NAME = "value"

        private const val RETRY_ATTEMPTS_FOR_NETWORK_CALLS = 5L

        fun getLegacyAppName(): String =
            FirebaseApp.DEFAULT_APP_NAME

        fun getFirestoreAppName(): String =
            "firestore"
    }
}
