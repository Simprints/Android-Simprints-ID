package com.simprints.id.testUtils.roboletric

import android.content.SharedPreferences
import com.nhaarman.mockito_kotlin.any
import com.simprints.id.activities.CheckLoginFromIntentActivityTest
import com.simprints.id.data.analytics.eventData.SessionEventsLocalDbManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.network.PeopleRemoteInterface
import com.simprints.id.data.loginInfo.LoginInfoManagerImpl
import com.simprints.id.data.secure.SecureDataManagerImpl
import com.simprints.id.domain.Project
import com.simprints.id.secure.cryptography.Hasher
import com.simprints.id.shared.anyNotNull
import com.simprints.id.shared.whenever
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.mockwebserver.MockWebServer
import org.mockito.Mockito
import org.mockito.stubbing.Answer

const val SHARED_PREFS_FOR_MOCK_FIREBASE_TOKEN_VALID = "SHARED_PREFS_FOR_MOCK_FIREBASE_TOKEN_VALID"
const val SHARED_PREFS_FOR_MOCK_LOCAL_DB_KEY = "SHARED_PREFS_FOR_MOCK_LOCAL_DB_KEY"

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

fun setupLocalAndRemoteManagersForApiTesting(mockServer: MockWebServer? = null,
                                             localDbManagerSpy: LocalDbManager,
                                             remoteDbManagerSpy: RemoteDbManager,
                                             sessionEventsLocalDbManagerMock: SessionEventsLocalDbManager) {

    PeopleRemoteInterface.baseUrl = mockServer?.url("/").toString()
    whenever(localDbManagerSpy.insertOrUpdatePersonInLocal(anyNotNull())).thenReturn(Completable.complete())
    whenever(localDbManagerSpy.loadPersonFromLocal(any())).thenReturn(Single.create { it.onError(IllegalStateException()) })
    whenever(localDbManagerSpy.getPeopleCountFromLocal(any(), any(), any(), any())).thenReturn(Single.create { it.onError(IllegalStateException()) })

    setupSessionEventsManagerToAvoidRealmCall(sessionEventsLocalDbManagerMock)

    whenever(remoteDbManagerSpy.getCurrentFirestoreToken()).thenReturn(Single.just("someToken"))
}

fun setupSessionEventsManagerToAvoidRealmCall(sessionEventsLocalDbManagerMock: SessionEventsLocalDbManager) {
    whenever(sessionEventsLocalDbManagerMock.loadSessions(any(), any())).thenReturn(Single.create { it.onError(IllegalStateException()) })
    whenever(sessionEventsLocalDbManagerMock.insertOrUpdateSessionEvents(any())).thenReturn(Completable.complete())
}
