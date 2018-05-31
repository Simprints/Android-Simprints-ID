package com.simprints.id.sync

import com.google.firebase.FirebaseApp
import com.google.gson.stream.JsonReader
import com.simprints.id.Application
import com.simprints.id.di.DaggerForTests
import com.simprints.id.di.AppModuleForTests
import com.simprints.id.data.DataManager
import com.simprints.id.data.db.DbManagerImpl
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.realm.models.rl_Person
import com.simprints.id.data.db.local.realm.models.rl_SyncInfo
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.db.remote.network.DownSyncParams.Companion.LAST_KNOWN_PATIENT_AT
import com.simprints.id.data.db.remote.network.DownSyncParams.Companion.LAST_KNOWN_PATIENT_ID
import com.simprints.id.data.db.remote.network.PeopleRemoteInterface
import com.simprints.id.data.db.sync.SyncExecutor
import com.simprints.id.data.prefs.loginInfo.LoginInfoManager
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.network.SimApiClient
import com.simprints.id.services.progress.DownloadProgress
import com.simprints.id.services.progress.Progress
import com.simprints.id.services.progress.UploadProgress
import com.simprints.id.services.sync.SyncTaskParameters
import com.simprints.id.shared.anyNotNull
import com.simprints.id.shared.whenever
import com.simprints.id.testUtils.base.RxJavaTest
import com.simprints.id.testUtils.mockServer.assertUrlParam
import com.simprints.id.testUtils.retrofit.createMockBehaviorService
import com.simprints.id.testUtils.roboletric.TestApplication
import com.simprints.id.tools.delegates.lazyVar
import com.simprints.id.tools.json.JsonHelper
import com.simprints.id.tools.utils.PeopleGeneratorUtils
import com.simprints.id.tools.utils.PeopleGeneratorUtils.getRandomPeople
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class)
class SyncTest : RxJavaTest, DaggerForTests() {

    private var mockServer = MockWebServer()
    private lateinit var apiClient: SimApiClient<PeopleRemoteInterface>

    @Inject lateinit var dataManager: DataManager
    @Inject lateinit var remoteDbManagerSpy: RemoteDbManager
    @Inject lateinit var secureDataManager: SecureDataManager
    @Inject lateinit var loginInfoManager: LoginInfoManager

    override var module by lazyVar {
        AppModuleForTests(app,
            remoteDbManagerSpy = true,
            localDbManagerSpy = false)
    }

    @Before
    override fun setUp() {
        FirebaseApp.initializeApp(RuntimeEnvironment.application)
        app = (RuntimeEnvironment.application as TestApplication)
        super.setUp()
        testAppComponent.inject(this)
        dataManager.initialiseDb()

        whenever(remoteDbManagerSpy.getCurrentFirestoreToken()).thenReturn(Single.just(""))

        mockServer.start()
        apiClient = SimApiClient(PeopleRemoteInterface::class.java, PeopleRemoteInterface.baseUrl)
    }

    @Test
    fun uploadPeopleInBatches_shouldWorkWithPoorConnection() {
        // TODO : change this test to be deterministic instead of probabilistic
        val localDbManager = Mockito.mock(LocalDbManager::class.java)
        val app = RuntimeEnvironment.application as Application

        val patientsToUpload = getRandomPeople(35)

        whenever(localDbManager.loadPeopleFromLocalRx(toSync = true)).thenReturn(Flowable.fromIterable(patientsToUpload))
        whenever(localDbManager.getPeopleCountFromLocal(toSync = true)).thenReturn(Single.just(patientsToUpload.count()))

        val poorNetworkClientMock: PeopleRemoteInterface = SimApiMock(createMockBehaviorService(apiClient.retrofit, 50, PeopleRemoteInterface::class.java))
        whenever(remoteDbManagerSpy.getPeopleApiClient()).thenReturn(Single.just(poorNetworkClientMock))

        val sync = SyncExecutorMock(DbManagerImpl(localDbManager, remoteDbManagerSpy, secureDataManager, loginInfoManager), JsonHelper.gson)

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

        whenever(localDbManager.loadPeopleFromLocalRx(toSync = true)).thenReturn(Flowable.fromIterable(peopleToUpload))
        whenever(localDbManager.getPeopleCountFromLocal(toSync = true)).thenReturn(Single.just(peopleToUpload.count()))

        val poorNetworkClientMock: PeopleRemoteInterface = SimApiMock(createMockBehaviorService(apiClient.retrofit, 50, PeopleRemoteInterface::class.java))
        whenever(remoteDbManagerSpy.getPeopleApiClient()).thenReturn(Single.just(poorNetworkClientMock))

        val sync = SyncExecutorMock(DbManagerImpl(localDbManager, remoteDbManagerSpy, secureDataManager, loginInfoManager), JsonHelper.gson)

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
        PeopleRemoteInterface.baseUrl = this.mockServer.url("/").toString()
        val localDbMock = Mockito.mock(LocalDbManager::class.java)

        //Params
        val projectIdTest = "projectIDTest"
        val syncParams = SyncTaskParameters.GlobalSyncTaskParameters(projectIdTest)
        val nPeopleToDownload = 22000
        val peopleToDownload = getRandomPeople(nPeopleToDownload).map { fb_Person(it) }.sortedBy { it.updatedAt }

        val testObserver = makeFakeDownloadRequest(
            peopleToDownload,
            25000,
            localDbMock,
            3000,
            syncParams
        )
        testObserver.awaitTerminalEvent()

        testObserver
            .assertNoErrors()
            .assertComplete()

        Assert.assertTrue(testObserver.values().containsAll(arrayListOf(
            DownloadProgress(SyncExecutor.DOWN_BATCH_SIZE_FOR_UPDATING_UI, nPeopleToDownload),
            DownloadProgress(nPeopleToDownload, nPeopleToDownload))))

        val peopleCountRequestUrl = mockServer.takeRequest().requestUrl
        assertUrlParam(peopleCountRequestUrl, "projectId", projectIdTest)

        val peopleRequestUrl = mockServer.takeRequest().requestUrl
        assertUrlParam(peopleRequestUrl, "projectId", projectIdTest)
        val lastDownloadedPatient = peopleToDownload.last()
        assertUrlParam(peopleRequestUrl, LAST_KNOWN_PATIENT_AT, lastDownloadedPatient.updatedAt!!.time, { it?.toLong() })
        assertUrlParam(peopleRequestUrl, LAST_KNOWN_PATIENT_ID, lastDownloadedPatient.patientId)
    }

    @Test
    fun downloadPatientsForModuleSync_shouldSuccess() {
        PeopleRemoteInterface.baseUrl = this.mockServer.url("/").toString()
        val localDbMock = Mockito.mock(LocalDbManager::class.java)

        //Params
        val projectIdTest = "projectIdTest"
        val moduleIdTest = "moduleIdTest"
        val syncParams = SyncTaskParameters.ModuleIdSyncTaskParameters(projectIdTest, moduleIdTest)
        val nPeopleToDownload = 22000
        val peopleToDownload = getRandomPeople(nPeopleToDownload).map { fb_Person(it) }.sortedBy { it.updatedAt }

        val testObserver = makeFakeDownloadRequest(
            peopleToDownload,
            25000,
            localDbMock,
            3000,
            syncParams
        )
        testObserver.awaitTerminalEvent()

        testObserver
            .assertNoErrors()
            .assertComplete()

        Assert.assertTrue(testObserver.values().containsAll(arrayListOf(
            DownloadProgress(SyncExecutor.DOWN_BATCH_SIZE_FOR_UPDATING_UI, nPeopleToDownload),
            DownloadProgress(nPeopleToDownload, nPeopleToDownload))))

        val peopleCountRequestUrl = mockServer.takeRequest().requestUrl
        assertUrlParam(peopleCountRequestUrl, "projectId", projectIdTest)
        assertUrlParam(peopleCountRequestUrl, "moduleId", moduleIdTest)

        val peopleRequestUrl = mockServer.takeRequest().requestUrl
        assertUrlParam(peopleRequestUrl, "projectId", projectIdTest)
        assertUrlParam(peopleRequestUrl, "moduleId", moduleIdTest)
        assertUrlParam(peopleRequestUrl, LAST_KNOWN_PATIENT_AT, peopleToDownload.last().updatedAt!!.time, { it?.toLong() })
        assertUrlParam(peopleRequestUrl, LAST_KNOWN_PATIENT_ID, peopleToDownload.last().patientId)
    }

    @Test
    fun downloadPatientsForUserSync_shouldSuccess() {
        PeopleRemoteInterface.baseUrl = this.mockServer.url("/").toString()
        val localDbMock = Mockito.mock(LocalDbManager::class.java)

        //Params
        val projectIdTest = "projectIdTest"
        val userIdTest = "userIdTest"
        val syncParams = SyncTaskParameters.UserSyncTaskParameters(projectIdTest, userIdTest)
        val nPeopleToDownload = 22000
        val peopleToDownload = getRandomPeople(nPeopleToDownload).map { fb_Person(it) }.sortedBy { it.updatedAt }

        val testObserver = makeFakeDownloadRequest(
            peopleToDownload,
            25000,
            localDbMock,
            3000,
            syncParams
        )
        testObserver.awaitTerminalEvent()

        testObserver
            .assertNoErrors()
            .assertComplete()

        Assert.assertTrue(testObserver.values().containsAll(arrayListOf(
            DownloadProgress(SyncExecutor.DOWN_BATCH_SIZE_FOR_UPDATING_UI, nPeopleToDownload),
            DownloadProgress(nPeopleToDownload, nPeopleToDownload))))

        val peopleCountRequestUrl = mockServer.takeRequest().requestUrl
        assertUrlParam(peopleCountRequestUrl, "projectId", projectIdTest)
        assertUrlParam(peopleCountRequestUrl, "userId", userIdTest)

        val peopleRequestUrl = mockServer.takeRequest().requestUrl
        assertUrlParam(peopleRequestUrl, "projectId", projectIdTest)
        assertUrlParam(peopleRequestUrl, "userId", userIdTest)
        assertUrlParam(peopleRequestUrl, LAST_KNOWN_PATIENT_AT, peopleToDownload.last().updatedAt!!.time, { it?.toLong() })
        assertUrlParam(peopleRequestUrl, LAST_KNOWN_PATIENT_ID, peopleToDownload.last().patientId)
    }

    private fun makeFakeDownloadRequest(
        peopleToDownload: List<fb_Person>,
        nPatientsForProjectIdFromServer: Int,
        localDbMock: LocalDbManager,
        patientsAlreadyInLocalDb: Int,
        syncParams: SyncTaskParameters): TestObserver<Progress> {

        //Build fake response for GET patients
        val patientsToDownload = ArrayList(peopleToDownload)

        //Mock GET patients-count
        mockServer.enqueue(mockResponseForPatientsCount(nPatientsForProjectIdFromServer))

        //Mock GET patients
        mockServer.enqueue(mockResponseForDownloadPatients(patientsToDownload))

        //Mock saving patient in Realm
        mockLocalDbToSavePatientsFromStream(localDbMock)

        //Mock app has already patients in localDb
        whenever(localDbMock.getPeopleCountFromLocal(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull())).thenReturn(Single.just(patientsAlreadyInLocalDb))

        //Mock app RealmSyncInfo for syncParams
        whenever(localDbMock.getSyncInfoFor(anyNotNull())).thenReturn(Single.create { it.onSuccess(rl_SyncInfo(syncParams.toGroup(), rl_Person(peopleToDownload.last()))) })

        val sync = SyncExecutorMock(DbManagerImpl(localDbMock, remoteDbManagerSpy, secureDataManager, loginInfoManager), JsonHelper.gson)

        return sync.downloadNewPatients({ false }, syncParams).test()
    }

    private fun mockLocalDbToSavePatientsFromStream(localDbMock: LocalDbManager) {
        whenever(localDbMock.savePeopleFromStreamAndUpdateSyncInfo(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull())).thenAnswer({ invocation ->
            Completable.create {
                val args = invocation.arguments
                (args[0] as JsonReader).skipValue()
                val shouldStop = args[3] as (savedPerson: fb_Person) -> Boolean
                if (shouldStop(fb_Person(PeopleGeneratorUtils.getRandomPerson()))) {
                    it.onComplete()
                }
            }
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
