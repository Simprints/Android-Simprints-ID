package com.simprints.id.testUtils.roboletric

import android.content.SharedPreferences
import com.nhaarman.mockito_kotlin.any
import com.simprints.id.Application
import com.simprints.id.activities.CheckLoginFromIntentActivityTest
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.network.PeopleRemoteInterface
import com.simprints.id.data.prefs.loginInfo.LoginInfoManagerImpl
import com.simprints.id.data.secure.SecureDataManagerImpl
import com.simprints.id.data.secure.keystore.KeystoreManager
import com.simprints.id.domain.Project
import com.simprints.id.secure.cryptography.Hasher
import com.simprints.id.shared.anyNotNull
import com.simprints.id.shared.whenever
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.mockwebserver.MockWebServer
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.stubbing.Answer
import org.robolectric.RuntimeEnvironment

const val SHARED_PREFS_FOR_MOCK_FIREBASE_TOKEN_VALID = "SHARED_PREFS_FOR_MOCK_FIREBASE_TOKEN_VALID"
const val SHARED_PREFS_FOR_MOCK_LOCAL_DB_KEY = "SHARED_PREFS_FOR_MOCK_LOCAL_DB_KEY"

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

fun mockLoadProject(localDbManagerMock: LocalDbManager, remoteDbManagerMock: RemoteDbManager) {
    val project = Project().apply { id = "project id"; name = "project name"; description = "project desc" }
    whenever(localDbManagerMock.loadProjectFromLocal(anyNotNull())).thenReturn(Single.just(project))
    whenever(remoteDbManagerMock.loadProjectFromRemote(anyNotNull())).thenReturn(Single.just(project))
    whenever(localDbManagerMock.saveProjectIntoLocal(anyNotNull())).thenReturn(Completable.complete())
}

fun initLogInStateMock(sharedPrefs: SharedPreferences,
                       remoteDbManagerMock: RemoteDbManager) {

    val answer = Answer<Boolean> {
        sharedPrefs.getBoolean(SHARED_PREFS_FOR_MOCK_FIREBASE_TOKEN_VALID, false)
    }
    Mockito.doAnswer(answer).`when`(remoteDbManagerMock).isSignedIn(anyNotNull(), anyNotNull())
    whenever(remoteDbManagerMock.getCurrentFirestoreToken()).thenReturn(Single.just(""))
    whenever(remoteDbManagerMock.signInToRemoteDb(anyNotNull())).thenReturn(Completable.complete())
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

fun setupLocalAndRemoteManagersForApiTesting(mockServer: MockWebServer,
                                             localDbManagerSpy: LocalDbManager,
                                             remoteDbManagerSpy: RemoteDbManager) {
    PeopleRemoteInterface.baseUrl = mockServer.url("/").toString()
    whenever(localDbManagerSpy.insertOrUpdatePersonInLocal(anyNotNull())).thenReturn(Completable.complete())
    whenever(localDbManagerSpy.loadPersonFromLocal(any())).thenReturn(Single.create { it.onError(IllegalStateException()) })

    val app = RuntimeEnvironment.application as Application
    whenever(remoteDbManagerSpy.getCurrentFirestoreToken()).thenReturn(Single.just("someToken"))
}
