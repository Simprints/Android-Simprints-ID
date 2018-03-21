package com.simprints.id.data.db

import com.simprints.id.BuildConfig
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.db.sync.SyncApiInterface
import com.simprints.id.network.SimApiClient
import com.simprints.id.testUtils.anyNotNull
import com.simprints.id.testUtils.whenever
import com.simprints.id.tools.JsonHelper
import com.simprints.id.tools.base.RxJavaTest
import com.simprints.id.tools.roboletric.TestApplication
import com.simprints.id.tools.utils.FirestoreMigrationUtils
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
        SyncApiInterface.baseUrl = this.mockServer.url("/").toString()
        val mockLocalDbManager = spy(LocalDbManager::class.java)
        val mockRemoteDbManager = mock(RemoteDbManager::class.java)
        whenever(mockLocalDbManager.savePersonInLocal(anyNotNull())).thenReturn(Completable.complete())
        whenever(mockRemoteDbManager.getCurrentFirestoreToken()).thenReturn(Single.just("someToken"))
        whenever(mockLocalDbManager.updatePersonInLocal(anyNotNull())).thenReturn(Completable.complete())
        val fakePerson = fb_Person(FirestoreMigrationUtils.getRandomPerson())

        mockServer.enqueue(mockResponseForUploadPatient())
        mockServer.enqueue(mockResponseForDownloadPatient(fakePerson))

        val dbManager = DbManagerImpl(mockLocalDbManager, mockRemoteDbManager)
        val testObservable = dbManager.savePerson(fakePerson).test()

        testObservable.awaitTerminalEvent()
        testObservable
            .assertNoErrors()
            .assertComplete()

        verify(mockLocalDbManager, times(1)).savePersonInLocal(anyNotNull())
        verify(mockLocalDbManager, times(1)).updatePersonInLocal(anyNotNull())
    }

    @Test
    fun savingPerson_serverProblemStillSavesPerson() {
        SyncApiInterface.baseUrl = this.mockServer.url("/").toString()
        val mockLocalDbManager = spy(LocalDbManager::class.java)
        val mockRemoteDbManager = mock(RemoteDbManager::class.java)
        whenever(mockLocalDbManager.savePersonInLocal(anyNotNull())).thenReturn(Completable.complete())
        whenever(mockRemoteDbManager.getCurrentFirestoreToken()).thenReturn(Single.just("someToken"))
        val fakePerson = fb_Person(FirestoreMigrationUtils.getRandomPerson())

        for (i in 0..20) mockServer.enqueue(mockFailingResponse())

        val dbManager = DbManagerImpl(mockLocalDbManager, mockRemoteDbManager)
        val testObservable = dbManager.savePerson(fakePerson).test()

        testObservable.awaitTerminalEvent()
        testObservable.assertError(Throwable::class.java)

        verify(mockLocalDbManager, times(1)).savePersonInLocal(anyNotNull())
        verify(mockLocalDbManager, times(0)).updatePersonInLocal(anyNotNull())
    }

    private fun mockFailingResponse(): MockResponse? =
        MockResponse().setResponseCode(500)

    private fun mockResponseForUploadPatient(): MockResponse? =
        MockResponse().setResponseCode(200)

    private fun mockResponseForDownloadPatient(patient: fb_Person): MockResponse? {
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
