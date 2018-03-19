package com.simprints.id.sync

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.simprints.id.BuildConfig
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.RealmSyncInfo
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.db.sync.NaiveSync
import com.simprints.id.data.db.sync.SyncApiInterface
import com.simprints.id.network.SimApiClient
import com.simprints.id.services.sync.SyncTaskParameters
import com.simprints.id.testUtils.anyNotNull
import com.simprints.id.testUtils.whenever
import com.simprints.id.tools.JsonHelper
import com.simprints.id.tools.base.RxJavaTest
import com.simprints.id.tools.retrofit.createMockBehaviorService
import com.simprints.id.tools.roboletric.TestApplication
import com.simprints.id.tools.utils.FirestoreMigationUtils.getRandomPeople
import com.simprints.libcommon.DownloadProgress
import com.simprints.libcommon.Progress
import com.simprints.libcommon.UploadProgress
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, application = TestApplication::class)
class SyncTest : RxJavaTest() {

    private var mockServer = MockWebServer()
    private lateinit var apiClient: SimApiClient<SyncApiInterface>

    @Before
    fun setUp() {
        mockServer.start()
        apiClient = SimApiClient(SyncApiInterface::class.java, SyncApiInterface.baseUrl)
    }

    @Test
    fun uploadPeopleInBatches_shouldWorkWithPoorConnection() {

        val localDbManager = Mockito.mock(LocalDbManager::class.java)
        val patientsToUpload = getRandomPeople(35)
        whenever(localDbManager.getPeopleFromLocal(toSync = true)).thenReturn(patientsToUpload)

        val sync = NaiveSyncTest(
            SimApiMock(createMockBehaviorService(apiClient.retrofit, 50, SyncApiInterface::class.java)),
            localDbManager,
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
        val patientsToUpload = getRandomPeople(35)
        whenever(localDbManager.getPeopleFromLocal(toSync = true)).thenReturn(patientsToUpload)

        val sync = NaiveSyncTest(
            SimApiMock(createMockBehaviorService(apiClient.retrofit, 50, SyncApiInterface::class.java)),
            localDbManager,
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
    fun uploadSingleBatchOfPeople_shouldWorkWithPoorConnection() {

        val sync = NaiveSyncTest(
            SimApiMock(createMockBehaviorService(apiClient.retrofit, 50, SyncApiInterface::class.java)),
            Mockito.mock(LocalDbManager::class.java),
            JsonHelper.gson)

        val patients = getRandomPeople(3)
        val testObserver = sync.uploadPatientsBatch(ArrayList(patients.map { fb_Person(it) })).test()
        testObserver.awaitTerminalEvent()
        testObserver
            .assertNoErrors()
            .assertComplete()
            .assertValue { it == patients.size }
    }

    @Test
    fun uploadSingleBatchOfPeopleFail_shouldThrowAnError() {

        val sync = NaiveSyncTest(
            SimApiMock(createMockBehaviorService(apiClient.retrofit, 100, SyncApiInterface::class.java)),
            Mockito.mock(LocalDbManager::class.java),
            JsonHelper.gson)

        val patients = getRandomPeople(3)
        val testObserver = sync.uploadPatientsBatch(ArrayList(patients.map { fb_Person(it) })).test()
        testObserver.awaitTerminalEvent()
        testObserver.assertError { true }
    }

    @Test
    fun downloadPatientsForProjectId_shouldSucceed() {
        SyncApiInterface.baseUrl = this.mockServer.url("/").toString()
        val localDbMock = Mockito.mock(LocalDbManager::class.java)

        //Params
        val projectIDTest = "projectIDTest"
        val nPatientsToDownload = 22000
        val lastSyncTime = Date()

        val testObserver = makeFakeDownloadRequest(
            nPatientsToDownload,
            25000,
            localDbMock,
            3000,
            SyncTaskParameters.GlobalSyncTaskParameters(projectIDTest),
            lastSyncTime
        )
        testObserver.awaitTerminalEvent()

        testObserver
            .assertNoErrors()
            .assertComplete()
            .values().containsAll(arrayListOf(
                DownloadProgress(NaiveSync.UPDATE_UI_BATCH_SIZE, nPatientsToDownload),
                DownloadProgress(nPatientsToDownload, nPatientsToDownload)))

        val patientsCountRequest = mockServer.takeRequest()
        Assert.assertEquals(projectIDTest, patientsCountRequest.requestUrl.queryParameter("projectId"))

        val patientsRequest = mockServer.takeRequest()
        Assert.assertEquals(projectIDTest, patientsRequest.requestUrl.queryParameter("projectId"))
        Assert.assertEquals(lastSyncTime.time, patientsRequest.requestUrl.queryParameter("lastSync")?.toLong())
    }

    private fun makeFakeDownloadRequest(
        nPatientsToDownload: Int,
        nPatientsForProjectIdFromServer: Int,
        localDbMock: LocalDbManager,
        patientsAlreadyInLocalDb: Int,
        syncParams: SyncTaskParameters.GlobalSyncTaskParameters,
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
        `when`(localDbMock.getPeopleFromLocal(anyString(), anyString(), anyString(), anyString(), anyBoolean())).thenReturn(getRandomPeople(patientsAlreadyInLocalDb))

        //Mock app RealmSyncInfo for syncParams
        `when`(localDbMock.getSyncInfoFor(anyNotNull())).thenReturn(RealmSyncInfo(syncParams.toGroup().ordinal, lastSyncTime))

        val sync = NaiveSyncTest(
                SimApiClient(SyncApiInterface::class.java, SyncApiInterface.baseUrl).api,
                localDbMock,
                JsonHelper.gson)

        return sync.downloadNewPatients({ false }, syncParams).test()
    }

    private fun mockLocalDbToSavePatientsFromStream(localDbMock: LocalDbManager) {
        `when`(localDbMock.savePeopleFromStream(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull())).thenAnswer({ invocation ->
            val args = invocation.arguments
            (args[0] as JsonReader).skipValue()
            val shouldStop = args[3] as () -> Boolean
            shouldStop()
        })
    }

    private fun mockResponseForPatientsCount(count: Int): MockResponse? {
        return MockResponse().let {
            it.setResponseCode(200)
            it.setBody("$count")
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
    @Throws fun tearDown() {
        mockServer.shutdown()
    }
}

class NaiveSyncTest(api: SyncApiInterface,
                    localDbManager: LocalDbManager,
                    gson: Gson) : NaiveSync(api, localDbManager, gson, "") {

    public override fun uploadPatientsBatch(patientsToUpload: ArrayList<fb_Person>): Single<Int> {
        return super.uploadPatientsBatch(patientsToUpload)
    }

    public override fun uploadNewPatients(isInterrupted: () -> Boolean, batchSize: Int): Observable<Progress> {
        return super.uploadNewPatients(isInterrupted, batchSize)
    }

    public override fun downloadNewPatients(isInterrupted: () -> Boolean, syncParams: SyncTaskParameters): Observable<Progress> {
        return super.downloadNewPatients(isInterrupted, syncParams)
    }
}
