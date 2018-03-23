package com.simprints.id.data.db

import com.simprints.id.Application
import com.simprints.id.BuildConfig
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.FirebaseManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.authListener.FirebaseAuthListenerManager
import com.simprints.id.data.db.remote.connectionListener.FirebaseConnectionListenerManager
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.db.sync.SyncApiInterface
import com.simprints.id.network.SimApiClient
import com.simprints.id.testUtils.anyNotNull
import com.simprints.id.testUtils.base.RxJavaTest
import com.simprints.id.testUtils.roboletric.TestApplication
import com.simprints.id.testUtils.whenever
import com.simprints.id.tools.JsonHelper
import com.simprints.id.tools.utils.PeopleGeneratorUtils
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, application = TestApplication::class)
class DbManagerTest : RxJavaTest() {

    private var mockServer = MockWebServer()
    private lateinit var apiClient: SimApiClient<SyncApiInterface>

    @Before
    fun setUp() {
        mockServer.start()
        apiClient = SimApiClient(SyncApiInterface::class.java, SyncApiInterface.baseUrl)
    }

    @Test
    fun savingPerson_shouldSaveThenUpdatePersonLocally() {
        val (dbManager, localDbManager, _) = getDbManagerWithMockedLocalAndRemoteManagersForApiTesting()
        val fakePerson = fb_Person(PeopleGeneratorUtils.getRandomPerson())

        mockServer.enqueue(mockResponseForUploadPatient())
        mockServer.enqueue(mockResponseForDownloadPatient(fakePerson))

        val testObservable = dbManager.savePerson(fakePerson).test()

        testObservable.awaitTerminalEvent()
        testObservable
            .assertNoErrors()
            .assertComplete()

        verify(localDbManager, times(1)).savePersonInLocal(anyNotNull())
        verify(localDbManager, times(1)).updatePersonInLocal(anyNotNull())
    }

    @Test
    fun savingPerson_serverProblemStillSavesPerson() {
        val (dbManager, localDbManager, _) = getDbManagerWithMockedLocalAndRemoteManagersForApiTesting()
        val fakePerson = fb_Person(PeopleGeneratorUtils.getRandomPerson())

        for (i in 0..20) mockServer.enqueue(mockFailingResponse())

        val testObservable = dbManager.savePerson(fakePerson).test()

        testObservable.awaitTerminalEvent()
        testObservable.assertError(Throwable::class.java)

        verify(localDbManager, times(1)).savePersonInLocal(anyNotNull())
        verify(localDbManager, times(0)).updatePersonInLocal(anyNotNull())
    }

    private fun getDbManagerWithMockedLocalAndRemoteManagersForApiTesting(): Triple<DbManager, LocalDbManager, RemoteDbManager> {
        SyncApiInterface.baseUrl = this.mockServer.url("/").toString()
        val localDbManager = spy(LocalDbManager::class.java)
        val mockConnectionListenerManager = mock(FirebaseConnectionListenerManager::class.java)
        val mockAuthListenerManager = mock(FirebaseAuthListenerManager::class.java)
        whenever(localDbManager.savePersonInLocal(anyNotNull())).thenReturn(Completable.complete())
        whenever(localDbManager.updatePersonInLocal(anyNotNull())).thenReturn(Completable.complete())

        val remoteDbManager = spy(FirebaseManager(
            (RuntimeEnvironment.application as Application),
            mockConnectionListenerManager,
            mockAuthListenerManager))
        whenever(remoteDbManager.getCurrentFirestoreToken()).thenReturn(Single.just("someToken"))

        val dbManager = DbManagerImpl(localDbManager, remoteDbManager)
        return Triple(dbManager, localDbManager, remoteDbManager)
    }

    private fun mockFailingResponse(): MockResponse =
        MockResponse().setResponseCode(500)

    private fun mockResponseForUploadPatient(): MockResponse =
        MockResponse().setResponseCode(200)

    private fun mockResponseForDownloadPatient(patient: fb_Person): MockResponse {
        val fbPersonJson = JsonHelper.gson.toJson(patient)

        return MockResponse().let {
            it.setResponseCode(200)
            it.setBody(fbPersonJson)
        }
    }

    @After
    @Throws
    fun tearDown() {
        mockServer.shutdown()
    }
}
