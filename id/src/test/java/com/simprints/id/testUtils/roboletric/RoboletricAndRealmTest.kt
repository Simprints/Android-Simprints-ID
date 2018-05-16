package com.simprints.id.testUtils.roboletric

import android.content.SharedPreferences
import com.nhaarman.mockito_kotlin.any
import com.simprints.id.Application
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.DbManagerImpl
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.realm.RealmDbManagerImpl
import com.simprints.id.data.db.remote.FirebaseManagerImpl
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.network.PeopleRemoteInterface
import com.simprints.id.domain.Project
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
    app.remoteDbManager = Mockito.mock(FirebaseManagerImpl::class.java)
}

fun mockLoadProject(app: Application) {
    val project = Project().apply { id = "project id"; name = "project name"; description = "project desc" }
    whenever(app.localDbManager.loadProjectFromLocal(anyNotNull())).thenReturn(Single.just(project))
    whenever(app.remoteDbManager.loadProjectFromRemote(anyNotNull())).thenReturn(Single.just(project))
    whenever(app.localDbManager.saveProjectIntoLocal(anyNotNull())).thenReturn(Completable.complete())
}

fun mockDbManager(app: Application) {
    val spy = Mockito.spy(app.dbManager)
    Mockito.doNothing().`when`(spy).initialiseDb()
    Mockito.doReturn(Completable.complete()).`when`(spy).signIn(anyNotNull(), anyNotNull())
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

    val app = RuntimeEnvironment.application as Application
    val remoteDbManager = Mockito.spy(FirebaseManagerImpl(RuntimeEnvironment.application as Application))
    whenever(remoteDbManager.getCurrentFirestoreToken()).thenReturn(Single.just("someToken"))

    val dbManager = DbManagerImpl(localDbManager, remoteDbManager, app.secureDataManager, app.loginInfoManager )
    return Triple(dbManager, localDbManager, remoteDbManager)
}
