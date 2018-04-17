package com.simprints.id.testUtils.roboletric

import android.content.SharedPreferences
import com.nhaarman.mockito_kotlin.any
import com.simprints.id.Application
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.DbManagerImpl
import com.simprints.id.data.db.ProjectIdProvider
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.realm.RealmDbManagerImpl
import com.simprints.id.data.db.remote.FirebaseManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.network.PeopleRemoteInterface
import com.simprints.id.testUtils.anyNotNull
import com.simprints.id.testUtils.whenever
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.mockwebserver.MockWebServer
import org.mockito.Mockito
import org.mockito.stubbing.Answer
import org.robolectric.RuntimeEnvironment

fun mockLocalDbManager(app: Application) {
    app.localDbManager = Mockito.mock(RealmDbManagerImpl::class.java)
}

fun mockRemoteDbManager(app: Application) {
    app.remoteDbManager = Mockito.mock(FirebaseManager::class.java)
}

fun mockDbManager(app: Application) {
    val spy = Mockito.spy(app.dbManager)
    Mockito.doNothing().`when`(spy).initialiseDb()
    Mockito.doReturn(Completable.complete()).`when`(spy).signIn(anyNotNull(), anyNotNull())
    Mockito.doReturn(Completable.complete()).`when`(spy).getLocalKeyAndSignInToLocal(anyNotNull())
    app.dbManager = spy
}

fun mockIsSignedIn(app: Application, sharedPrefs: SharedPreferences) {
    val answer = Answer<Boolean> {
        sharedPrefs.getBoolean("IS_FIREBASE_TOKEN_VALID", false)
    }
    Mockito.doAnswer(answer).`when`(app.remoteDbManager).isSignedIn(anyNotNull(), anyNotNull())
    whenever(app.remoteDbManager.getCurrentFirestoreToken()).thenReturn(Single.just(""))
}

fun getDbManagerWithMockedLocalAndRemoteManagersForApiTesting(mockServer: MockWebServer): Triple<DbManager, LocalDbManager, RemoteDbManager> {
    PeopleRemoteInterface.baseUrl = mockServer.url("/").toString()
    val localDbManager = Mockito.spy(LocalDbManager::class.java)
    whenever(localDbManager.insertOrUpdatePersonInLocal(anyNotNull())).thenReturn(Completable.complete())
    whenever(localDbManager.loadPersonFromLocal(any())).thenReturn(Single.create { it.onError(IllegalStateException()) })

    val projectIdProvider = Mockito.mock(ProjectIdProvider::class.java).also {
        whenever(it.getSignedInProjectId()).thenReturn(Single.just("some_local_key"))
    }

    val remoteDbManager = Mockito.spy(FirebaseManager(
        (RuntimeEnvironment.application as Application),
        projectIdProvider))
    whenever(remoteDbManager.getCurrentFirestoreToken()).thenReturn(Single.just("someToken"))

    val dbManager = DbManagerImpl(localDbManager, remoteDbManager)
    return Triple(dbManager, localDbManager, remoteDbManager)
}
