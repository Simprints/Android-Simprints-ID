package com.simprints.id.testUtils.roboletric

import android.content.SharedPreferences
import com.nhaarman.mockito_kotlin.any
import com.simprints.id.Application
import com.simprints.id.activities.CheckLoginFromIntentActivityTest
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.DbManagerImpl
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.realm.RealmDbManagerImpl
import com.simprints.id.data.db.remote.FirebaseManagerImpl
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.network.PeopleRemoteInterface
import com.simprints.id.data.prefs.loginInfo.LoginInfoManagerImpl
import com.simprints.id.data.secure.SecureDataManagerImpl
import com.simprints.id.data.secure.keystore.KeystoreManager
import com.simprints.id.domain.Project
import com.simprints.id.secure.cryptography.Hasher
import com.simprints.id.shared.anyNotNull
import com.simprints.id.shared.whenever
import com.simprints.id.tools.RandomGeneratorImpl
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.mockwebserver.MockWebServer
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.stubbing.Answer
import org.robolectric.RuntimeEnvironment

const val SHARED_PREFS_FOR_MOCK_FIREBASE_TOKEN_VALID = "SHARED_PREFS_FOR_MOCK_FIREBASE_TOKEN_VALID"
const val SHARED_PREFS_FOR_MOCK_LOCAL_DB_KEY = "SHARED_PREFS_FOR_MOCK_LOCAL_DB_KEY"

fun createMockForLocalDbManager(app: Application) {
    app.localDbManager = Mockito.mock(RealmDbManagerImpl::class.java)
}

fun createMockForRemoteDbManager(app: Application) {
    app.remoteDbManager = Mockito.mock(FirebaseManagerImpl::class.java)
}

fun createMockForSecureDataManager(app: Application) {
    val mockKeyStore = setupFakeKeyStore()
    val secureDataManager = SecureDataManagerImpl(mockKeyStore, app.preferencesManager, RandomGeneratorImpl())
    app.secureDataManager = spy(secureDataManager)
}

fun setupFakeKeyStore(): KeystoreManager = mock(KeystoreManager::class.java).also {
    val encryptAnswer = Answer<String> {
        "enc_" + it.arguments[0] as String
    }
    Mockito.doAnswer(encryptAnswer).`when`(it).encryptString(anyNotNull())

    val decryptAnswer = Answer<String> {
        (it.arguments[0] as String).replace("enc_", "")
    }
    Mockito.doAnswer(decryptAnswer).`when`(it).decryptString(anyNotNull())
}

fun mockLoadProject(app: Application) {
    val project = Project().apply { id = "project id"; name = "project name"; description = "project desc" }
    whenever(app.localDbManager.loadProjectFromLocal(anyNotNull())).thenReturn(Single.just(project))
    whenever(app.remoteDbManager.loadProjectFromRemote(anyNotNull())).thenReturn(Single.just(project))
    whenever(app.localDbManager.saveProjectIntoLocal(anyNotNull())).thenReturn(Completable.complete())
}

fun createMockForDbManager(app: Application) {
    val spy = Mockito.spy(app.dbManager)
    Mockito.doNothing().`when`(spy).initialiseRemoteDb()
    Mockito.doReturn(Completable.complete()).`when`(spy).signIn(anyNotNull(), anyNotNull())
    app.dbManager = spy
}

fun initLogInStateMock(app: Application,
                       sharedPrefs: SharedPreferences) {

    val answer = Answer<Boolean> {
        sharedPrefs.getBoolean(SHARED_PREFS_FOR_MOCK_FIREBASE_TOKEN_VALID, false)
    }
    Mockito.doAnswer(answer).`when`(app.remoteDbManager).isSignedIn(anyNotNull(), anyNotNull())
    whenever(app.remoteDbManager.getCurrentFirestoreToken()).thenReturn(Single.just(""))
}

fun setUserLogInState(logged: Boolean,
                      sharedPrefs: SharedPreferences,
                      projectId: String = CheckLoginFromIntentActivityTest.DEFAULT_PROJECT_ID,
                      legacyApiKey: String = CheckLoginFromIntentActivityTest.DEFAULT_LEGACY_API_KEY,
                      userId: String = CheckLoginFromIntentActivityTest.DEFAULT_USER_ID,
                      projectSecret: String = CheckLoginFromIntentActivityTest.DEFAULT_PROJECT_SECRET,
                      realmKey: String = CheckLoginFromIntentActivityTest.DEFAULT_REALM_KEY) {

    Thread.sleep(1000)
    val editor = sharedPrefs.edit()
    editor.putString(LoginInfoManagerImpl.ENCRYPTED_PROJECT_SECRET, if (logged) projectSecret else "")
    editor.putString(LoginInfoManagerImpl.PROJECT_ID, if (logged) projectId else "")
    editor.putString(LoginInfoManagerImpl.USER_ID, if (logged) userId else "")
    editor.putBoolean(SHARED_PREFS_FOR_MOCK_FIREBASE_TOKEN_VALID, logged)
    editor.putString(SecureDataManagerImpl.SHARED_PREFS_KEY_FOR_REALM_KEY + projectId, if (logged) realmKey else "")
    editor.putString(SecureDataManagerImpl.SHARED_PREFS_KEY_FOR_LEGACY_REALM_KEY + projectId, if (logged) "enc_$legacyApiKey" else "")

    if (!legacyApiKey.isEmpty()) {
        val hashedLegacyApiKey = Hasher().hash(legacyApiKey)
        editor.putString(projectId, if (logged) hashedLegacyApiKey else "")
        editor.putString(hashedLegacyApiKey, if (logged) projectId else "")
    }
    editor.commit()
}

fun getDbManagerWithMockedLocalAndRemoteManagersForApiTesting(mockServer: MockWebServer): Triple<DbManager, LocalDbManager, RemoteDbManager> {
    PeopleRemoteInterface.baseUrl = mockServer.url("/").toString()
    val localDbManager = Mockito.spy(LocalDbManager::class.java)
    whenever(localDbManager.insertOrUpdatePersonInLocal(anyNotNull())).thenReturn(Completable.complete())
    whenever(localDbManager.loadPersonFromLocal(any())).thenReturn(Single.create { it.onError(IllegalStateException()) })

    val app = RuntimeEnvironment.application as Application
    val remoteDbManager = Mockito.spy(FirebaseManagerImpl(RuntimeEnvironment.application as Application))
    whenever(remoteDbManager.getCurrentFirestoreToken()).thenReturn(Single.just("someToken"))

    val dbManager = DbManagerImpl(localDbManager, remoteDbManager, app.secureDataManager, app.loginInfoManager)
    return Triple(dbManager, localDbManager, remoteDbManager)
}
