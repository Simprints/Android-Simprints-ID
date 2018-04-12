package com.simprints.id.sync

import android.content.Context
import com.google.gson.stream.JsonReader
import com.simprints.id.BuildConfig
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.realm.models.rl_SyncInfo
import com.simprints.id.data.db.remote.FirebaseManager
import com.simprints.id.data.db.remote.FirebaseOptionsHelper
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.authListener.RemoteDbAuthListenerManager
import com.simprints.id.data.db.remote.connectionListener.FirebaseConnectionListenerManager
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.db.remote.network.RemoteApiInterface
import com.simprints.id.data.db.sync.SyncExecutor
import com.simprints.id.network.SimApiClient
import com.simprints.id.services.progress.DownloadProgress
import com.simprints.id.services.progress.Progress
import com.simprints.id.services.progress.UploadProgress
import com.simprints.id.services.sync.SyncTaskParameters
import com.simprints.id.testUtils.anyNotNull
import com.simprints.id.testUtils.base.RxJavaTest
import com.simprints.id.testUtils.retrofit.createMockBehaviorService
import com.simprints.id.testUtils.roboletric.TestApplication
import com.simprints.id.testUtils.whenever
import com.simprints.id.tools.json.JsonHelper
import com.simprints.id.tools.utils.PeopleGeneratorUtils
import com.simprints.id.tools.utils.PeopleGeneratorUtils.getRandomPeople
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.*
import java.util.concurrent.atomic.AtomicInteger


@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, application = TestApplication::class)
class SyncTest : RxJavaTest() {

    private var mockServer = MockWebServer()
    private lateinit var apiClient: SimApiClient<RemoteApiInterface>

    private var remoteDbManager: RemoteDbManager = spy(FirebaseManager(mock(Context::class.java),
        mock(FirebaseConnectionListenerManager::class.java),
        mock(RemoteDbAuthListenerManager::class.java),
        mock(FirebaseOptionsHelper::class.java)))

    @Before
    fun setUp() {
        whenever(remoteDbManager.getCurrentFirestoreToken()).thenReturn(Single.just(""))

        mockServer.start()
        apiClient = SimApiClient(RemoteApiInterface::class.java, RemoteApiInterface.baseUrl)
    }

    @Test
    fun uploadPeopleInBatches_shouldWorkWithPoorConnection() {
        // TODO : change this test to be deterministic instead of probabilistic
        val localDbManager = Mockito.mock(LocalDbManager::class.java)

        val patientsToUpload = getRandomPeople(35)
        whenever(localDbManager.loadPeopleFromLocal(toSync = true)).thenReturn(Single.create { it.onSuccess(patientsToUpload) })
        val poorNetworkClientMock: RemoteApiInterface = SimApiMock(createMockBehaviorService(apiClient.retrofit, 50, RemoteApiInterface::class.java))
        whenever(remoteDbManager.getSyncApi()).thenReturn(Single.just(poorNetworkClientMock))

        val sync = SyncExecutorMock(
            localDbManager,
            remoteDbManager,
            JsonHelper.gson)

        val testObserver = sync.uploadNewPatients({ false }, 10).test()
        testObserver.awaitTerminalEvent()

        testObserver
            .assertNoErrors()
            .assertComplete()
            .assertValueSequence(arrayListOf(
                UploadProgress(10, patientsToUpload.size),
                UploadProgress(20, patientsToUpload.size),
                UploadProgress(30, patientsToUpload.size),
                UploadProgress(35, patientsToUpload.size)))
    }

    @Test
    fun uploadPeopleGetInterrupted_shouldStopUploading() {
        val localDbManager = Mockito.mock(LocalDbManager::class.java)
        val peopleToUpload = getRandomPeople(35)
        whenever(localDbManager.loadPeopleFromLocal(toSync = true)).thenReturn(
            Single.create { it.onSuccess(peopleToUpload) }
        )
        val poorNetworkClientMock: RemoteApiInterface = SimApiMock(createMockBehaviorService(apiClient.retrofit, 50, RemoteApiInterface::class.java))
        whenever(remoteDbManager.getSyncApi()).thenReturn(Single.just(poorNetworkClientMock))

        val sync = SyncExecutorMock(
            localDbManager,
            remoteDbManager,
            JsonHelper.gson)

        val count = AtomicInteger(0)
        val testObserver = sync.uploadNewPatients({ count.addAndGet(1) > 2 }, 10).test()

        testObserver.awaitTerminalEvent()

        testObserver
            .assertNoErrors()
            .assertComplete()
            .assertValueCount(1)
    }

    @Test
    fun downloadPatientsForGlobalSync_shouldSuccess() {
        RemoteApiInterface.baseUrl = this.mockServer.url("/").toString()
        val localDbMock = Mockito.mock(LocalDbManager::class.java)

        //Params
        val projectIdTest = "projectIDTest"
        val syncParams = SyncTaskParameters.GlobalSyncTaskParameters(projectIdTest)
        val nPatientsToDownload = 22000
        val lastSyncTime = Date()

        val testObserver = makeFakeDownloadRequest(
            nPatientsToDownload,
            25000,
            localDbMock,
            3000,
            syncParams,
            lastSyncTime
        )
        testObserver.awaitTerminalEvent()

        testObserver
            .assertNoErrors()
            .assertComplete()

        Assert.assertTrue(testObserver.values().containsAll(arrayListOf(
            DownloadProgress(SyncExecutor.UPDATE_UI_BATCH_SIZE, nPatientsToDownload),
            DownloadProgress(nPatientsToDownload, nPatientsToDownload))))

        val patientsCountRequest = mockServer.takeRequest().requestUrl
        Assert.assertEquals(projectIdTest, patientsCountRequest.queryParameter("projectId"))

        val patientsRequest = mockServer.takeRequest().requestUrl
        Assert.assertEquals(projectIdTest, patientsRequest.queryParameter("projectId"))
        Assert.assertEquals(lastSyncTime.time, patientsRequest.queryParameter("updatedAfter")?.toLong())
    }

    @Test
    fun downloadPatientsForModuleSync_shouldSuccess() {
        RemoteApiInterface.baseUrl = this.mockServer.url("/").toString()
        val localDbMock = Mockito.mock(LocalDbManager::class.java)

        //Params
        val projectIdTest = "projectIdTest"
        val moduleIdTest = "moduleIdTest"
        val syncParams = SyncTaskParameters.ModuleIdSyncTaskParameters(projectIdTest, moduleIdTest)
        val nPatientsToDownload = 22000
        val lastSyncTime = Date()

        val testObserver = makeFakeDownloadRequest(
            nPatientsToDownload,
            25000,
            localDbMock,
            3000,
            syncParams,
            lastSyncTime
        )
        testObserver.awaitTerminalEvent()

        testObserver
            .assertNoErrors()
            .assertComplete()

        Assert.assertTrue(testObserver.values().containsAll(arrayListOf(
            DownloadProgress(SyncExecutor.UPDATE_UI_BATCH_SIZE, nPatientsToDownload),
            DownloadProgress(nPatientsToDownload, nPatientsToDownload))))

        val patientsCountRequest = mockServer.takeRequest()
        Assert.assertEquals(projectIdTest, patientsCountRequest.requestUrl.queryParameter("projectId"))
        Assert.assertEquals(moduleIdTest, patientsCountRequest.requestUrl.queryParameter("moduleId"))

        val patientsRequest = mockServer.takeRequest()
        Assert.assertEquals(projectIdTest, patientsRequest.requestUrl.queryParameter("projectId"))
        Assert.assertEquals(moduleIdTest, patientsCountRequest.requestUrl.queryParameter("moduleId"))
        Assert.assertEquals(lastSyncTime.time, patientsRequest.requestUrl.queryParameter("updatedAfter")?.toLong())
    }

    @Test
    fun downloadPatientsForUserSync_shouldSuccess() {
        RemoteApiInterface.baseUrl = this.mockServer.url("/").toString()
        val localDbMock = Mockito.mock(LocalDbManager::class.java)

        //Params
        val projectIdTest = "projectIdTest"
        val userIdTest = "userIdTest"
        val syncParams = SyncTaskParameters.UserSyncTaskParameters(projectIdTest, userIdTest)
        val nPatientsToDownload = 22000
        val lastSyncTime = Date()

        val testObserver = makeFakeDownloadRequest(
            nPatientsToDownload,
            25000,
            localDbMock,
            3000,
            syncParams,
            lastSyncTime
        )
        testObserver.awaitTerminalEvent()

        testObserver
            .assertNoErrors()
            .assertComplete()

        Assert.assertTrue(testObserver.values().containsAll(arrayListOf(
            DownloadProgress(SyncExecutor.UPDATE_UI_BATCH_SIZE, nPatientsToDownload),
            DownloadProgress(nPatientsToDownload, nPatientsToDownload))))

        val patientsCountRequest = mockServer.takeRequest()
        Assert.assertEquals(projectIdTest, patientsCountRequest.requestUrl.queryParameter("projectId"))
        Assert.assertEquals(userIdTest, patientsCountRequest.requestUrl.queryParameter("userId"))

        val patientsRequest = mockServer.takeRequest()
        Assert.assertEquals(projectIdTest, patientsRequest.requestUrl.queryParameter("projectId"))
        Assert.assertEquals(userIdTest, patientsCountRequest.requestUrl.queryParameter("userId"))
        Assert.assertEquals(lastSyncTime.time, patientsRequest.requestUrl.queryParameter("updatedAfter")?.toLong())
    }

    private fun makeFakeDownloadRequest(
        nPatientsToDownload: Int,
        nPatientsForProjectIdFromServer: Int,
        localDbMock: LocalDbManager,
        patientsAlreadyInLocalDb: Int,
        syncParams: SyncTaskParameters,
        lastSyncTime: Date): TestObserver<Progress> {

        //Build fake response for GET patients
        val patientsToDownload = ArrayList(getRandomPeople(nPatientsToDownload).map { fb_Person(it) })

        //Mock GET patients-count
        mockServer.enqueue(mockResponseForPatientsCount(nPatientsForProjectIdFromServer))

        //Mock GET patients
        mockServer.enqueue(mockResponseForDownloadPatients(patientsToDownload))

        //Mock saving patient in Realm
        mockLocalDbToSavePatientsFromStream(localDbMock)

        //Mock app has already patients in localDb
        whenever(localDbMock.loadPeopleFromLocal(any(), any(), any(), any())).thenReturn(Single.create { it.onSuccess(getRandomPeople(patientsAlreadyInLocalDb)) })
        whenever(localDbMock.getPeopleCountFromLocal(any(), any(), any(), any())).thenReturn(Single.create { it.onSuccess(patientsAlreadyInLocalDb) })

        //Mock app RealmSyncInfo for syncParams
        whenever(localDbMock.getSyncInfoFor(anyNotNull())).thenReturn(Single.create { it.onSuccess(rl_SyncInfo(syncParams.toGroup().ordinal, lastSyncTime)) })

        val sync = SyncExecutorMock(
            localDbMock,
            remoteDbManager,
            JsonHelper.gson)

        return sync.downloadNewPatients({ false }, syncParams).test()
    }

    private fun mockLocalDbToSavePatientsFromStream(localDbMock: LocalDbManager) {
        whenever(localDbMock.savePeopleFromStreamAndUpdateSyncInfo(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull())).thenAnswer({ invocation ->
            val args = invocation.arguments
            (args[0] as JsonReader).skipValue()
            val shouldStop = args[3] as (savedPerson: fb_Person) -> Boolean
            shouldStop(fb_Person(PeopleGeneratorUtils.getRandomPerson()))
        })
    }

    private fun mockResponseForPatientsCount(count: Int): MockResponse? {
        return MockResponse().let {
            it.setResponseCode(200)
            it.setBody("{\"count\": $count}")
        }
    }

    private fun mockResponseForDownloadPatients(patients: ArrayList<fb_Person>): MockResponse? {
        val fbPersonJson = JsonHelper.gson.toJson(patients)

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
