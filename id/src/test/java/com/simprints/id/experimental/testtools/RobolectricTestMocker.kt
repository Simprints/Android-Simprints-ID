package com.simprints.id.experimental.testtools

import android.content.SharedPreferences
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.anyOrNull
import com.simprints.id.activities.CheckLoginFromIntentActivityTest
import com.simprints.id.data.analytics.eventData.controllers.local.SessionEventsLocalDbManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.network.PeopleRemoteInterface
import com.simprints.id.data.db.remote.project.RemoteProjectManager
import com.simprints.id.data.loginInfo.LoginInfoManagerImpl
import com.simprints.id.data.secure.SecureDataManagerImpl
import com.simprints.id.domain.Project
import com.simprints.id.secure.cryptography.Hasher
import com.simprints.id.shared.anyNotNull
import com.simprints.id.testUtils.roboletric.SHARED_PREFS_FOR_MOCK_FIREBASE_TOKEN_VALID
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.mockwebserver.MockWebServer
import org.mockito.Mockito.doAnswer
import org.mockito.stubbing.Answer

object RobolectricTestMocker {

    fun mockLoadProject(localDbManagerMock: LocalDbManager, remoteProjectManagerMock: RemoteProjectManager): RobolectricTestMocker {
        val project = Project().apply { id = "project id"; name = "project name"; description = "project desc" }
        val projectSettings: JsonObject = JsonObject().apply { addProperty("key", "value") }
        whenever { localDbManagerMock.loadProjectFromLocal(anyNotNull()) } thenReturn Single.just(project)
        whenever { remoteProjectManagerMock.loadProjectFromRemote(anyNotNull()) } thenReturn Single.just(project)
        whenever { localDbManagerMock.saveProjectIntoLocal(anyNotNull()) } thenReturn Completable.complete()
        whenever { remoteProjectManagerMock.loadProjectRemoteConfigSettingsJsonString(anyNotNull()) } thenReturn Single.just<JsonElement>(projectSettings)
        return this
    }

    fun initLogInStateMock(sharedPrefs: SharedPreferences,
                           remoteDbManagerMock: RemoteDbManager): RobolectricTestMocker {

        val answer = Answer<Boolean> {
            sharedPrefs.getBoolean(SHARED_PREFS_FOR_MOCK_FIREBASE_TOKEN_VALID, false)
        }
        doAnswer(answer).`when`(remoteDbManagerMock).isSignedIn(anyNotNull(), anyNotNull())
        whenever { remoteDbManagerMock.getCurrentFirestoreToken() } thenReturn Single.just("")
        whenever { remoteDbManagerMock.signInToRemoteDb(anyNotNull()) } thenReturn Completable.complete()
        return this
    }

    fun setUserLogInState(logged: Boolean,
                          sharedPrefs: SharedPreferences,
                          projectId: String = CheckLoginFromIntentActivityTest.DEFAULT_PROJECT_ID,
                          legacyApiKey: String = CheckLoginFromIntentActivityTest.DEFAULT_LEGACY_API_KEY,
                          userId: String = CheckLoginFromIntentActivityTest.DEFAULT_USER_ID,
                          projectSecret: String = CheckLoginFromIntentActivityTest.DEFAULT_PROJECT_SECRET,
                          realmKey: String = CheckLoginFromIntentActivityTest.DEFAULT_REALM_KEY): RobolectricTestMocker {

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
        return this
    }

    fun setupLocalAndRemoteManagersForApiTesting(mockServer: MockWebServer? = null,
                                                 localDbManagerSpy: LocalDbManager,
                                                 remoteDbManagerSpy: RemoteDbManager,
                                                 sessionEventsLocalDbManagerMock: SessionEventsLocalDbManager): RobolectricTestMocker {

        PeopleRemoteInterface.baseUrl = mockServer?.url("/").toString()
        whenever { localDbManagerSpy.insertOrUpdatePersonInLocal(anyNotNull()) } thenReturn Completable.complete()
        whenever { localDbManagerSpy.loadPersonFromLocal(any()) } thenReturn Single.error(IllegalStateException())
        whenever { localDbManagerSpy.getPeopleCountFromLocal(any(), any(), any(), any()) } thenReturn Single.error(IllegalStateException())

        setupSessionEventsManagerToAvoidRealmCall(sessionEventsLocalDbManagerMock)

        whenever { remoteDbManagerSpy.getCurrentFirestoreToken() } thenReturn Single.just("someToken")
        return this
    }

    fun setupSessionEventsManagerToAvoidRealmCall(sessionEventsLocalDbManagerMock: SessionEventsLocalDbManager): RobolectricTestMocker {
        whenever { sessionEventsLocalDbManagerMock.loadSessions(anyOrNull(), anyOrNull()) } thenReturn Single.error(IllegalStateException())
        whenever { sessionEventsLocalDbManagerMock.insertOrUpdateSessionEvents(any()) } thenReturn Completable.complete()
        return this
    }

    fun tmp() {
        class Something
        class MockedClass {
            fun getSomething(something: Something) = Something()
        }

        val someClassMock: MockedClass = mock()

        whenever { someClassMock.getSomething(any()) } thenReturn Something()

    }
}
