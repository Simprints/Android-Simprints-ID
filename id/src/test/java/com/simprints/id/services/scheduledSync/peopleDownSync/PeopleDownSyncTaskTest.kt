package com.simprints.id.services.scheduledSync.peopleDownSync

import com.google.firebase.FirebaseApp
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.spy
import com.simprints.id.activities.ShadowAndroidXMultiDex
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.network.PeopleRemoteInterface
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.network.SimApiClient
import com.simprints.id.services.scheduledSync.peopleDownSync.db.DownSyncDao
import com.simprints.id.shared.whenever
import com.simprints.id.testUtils.base.RxJavaTest
import com.simprints.id.testUtils.roboletric.TestApplication
import io.reactivex.Single
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@Suppress("UNCHECKED_CAST")
@RunWith(RobolectricTestRunner::class) //StopShip: fix me
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class PeopleDownSyncTaskTest : RxJavaTest {

    private var mockServer = MockWebServer()
    private lateinit var apiClient: SimApiClient<PeopleRemoteInterface>
    private lateinit var remotePeopleApi: PeopleRemoteInterface
    private lateinit var downSyncDao: DownSyncDao

    private val dbManager: DbManager = mock()
    private val remoteDbManagerSpy: RemoteDbManager = spy()
    private val syncStatusDatabase: SyncStatusDatabase = mock()
    private val loginInfoManager: LoginInfoManager = mock()
    private val preferencesManager: PreferencesManager = mock()

    @Before
    fun setUp() {
        FirebaseApp.initializeApp(RuntimeEnvironment.application)
        dbManager.initialiseDb()

        whenever(remoteDbManagerSpy.getCurrentFirestoreToken()).thenReturn(Single.just(""))

        mockServer.start()
        //setupApi()
    }

//    @Test
//    fun downloadPatientsForGlobalSync_shouldSuccess() {
//        val localDbMock = Mockito.mock(LocalDbManager::class.java)
//
//        val projectIdTest = "projectIDTest"
//        val syncParams = SyncTaskParameters.GlobalSyncTaskParameters(projectIdTest)
//        val nPeopleToDownload = 22000
//        val peopleToDownload = getRandomPeople(nPeopleToDownload).map { fb_Person(it) }.sortedBy { it.updatedAt }
//
//        makeFakeDownloadRequest(peopleToDownload, localDbMock, 3000, syncParams)
//
//        val peopleRequestUrl = mockServer.takeRequest().requestUrl
//        assertPathUrlParam(peopleRequestUrl, projectIdTest)
//        val lastDownloadedPatient = peopleToDownload.last()
//        assertQueryUrlParam(peopleRequestUrl, "lastKnownPatientUpdatedAt", lastDownloadedPatient.updatedAt!!.time) { it?.toLong() }
//        assertQueryUrlParam(peopleRequestUrl, "lastKnownPatientId", lastDownloadedPatient.patientId)
//    }
//
//    @Test
//    fun downloadPatientsForModuleSync_oneModule_shouldSuccess() {
//        val localDbMock = Mockito.mock(LocalDbManager::class.java)
//
//        val projectIdTest = "projectIdTest"
//        val moduleIdTest = "moduleIdTest"
//        val syncParams = SyncTaskParameters.ModuleIdSyncTaskParameters(projectIdTest, setOf(moduleIdTest))
//        val nPeopleToDownload = 22000
//        val peopleToDownload = getRandomPeople(nPeopleToDownload).map { fb_Person(it) }.sortedBy { it.updatedAt }
//
//        makeFakeDownloadRequest(peopleToDownload, localDbMock, 3000, syncParams)
//
//        val peopleRequestUrl = mockServer.takeRequest().requestUrl
//        assertPathUrlParam(peopleRequestUrl, projectIdTest)
//        assertQueryUrlParam(peopleRequestUrl, "moduleId", moduleIdTest)
//        assertQueryUrlParam(peopleRequestUrl, "lastKnownPatientUpdatedAt", peopleToDownload.last().updatedAt!!.time) { it?.toLong() }
//        assertQueryUrlParam(peopleRequestUrl, "lastKnownPatientId", peopleToDownload.last().patientId)
//    }
//
//    @Test
//    fun downloadPatientsForModuleSync_multipleModules_shouldSuccess() {
//        PeopleRemoteInterface.baseUrl = this.mockServer.url("/").toString()
//        val localDbMock = Mockito.mock(LocalDbManager::class.java)
//
//        //Params
//        val projectIdTest = "projectIdTest"
//        val moduleIds = setOf("module1", "module2", "module3")
//        val syncParams = SyncTaskParameters.ModuleIdSyncTaskParameters(projectIdTest, moduleIds)
//        val nPeopleToDownloadForModule = listOf(22000, 26000, 17000)
//        val nPatientsAlreadyInLocalDb = listOf(3000, 500, 1700)
//        val nPeopleToDownloadForProjectIdFromServerForModule = nPeopleToDownloadForModule.zip(nPatientsAlreadyInLocalDb) { a, b -> a + b }
//        val peopleToDownload = nPeopleToDownloadForModule.map { nPeopleToDownload ->
//            getRandomPeople(nPeopleToDownload).map { fb_Person(it) }.sortedBy { it.updatedAt }
//        }
//
//        val testObserver = makeFakeDownloadRequestsForMultipleModules(
//            peopleToDownload,
//            nPeopleToDownloadForProjectIdFromServerForModule,
//            localDbMock,
//            moduleIds.toList(),
//            nPatientsAlreadyInLocalDb,
//            syncParams
//        )
//        testObserver.awaitTerminalEvent()
//
//        testObserver
//            .assertNoErrors()
//            .assertComplete()
//
//        Assert.assertTrue(testObserver.values().containsAll(arrayListOf(
//            DownloadProgress(SyncExecutor.DOWN_BATCH_SIZE_FOR_UPDATING_UI, nPeopleToDownloadForModule.sum()),
//            DownloadProgress(nPeopleToDownloadForModule.sum(), nPeopleToDownloadForModule.sum()))))
//
//        moduleIds.forEach { moduleId ->
//            val peopleCountRequestUrl = mockServer.takeRequest().requestUrl
//            assertPathUrlParam(peopleCountRequestUrl, projectIdTest)
//            assertQueryUrlParam(peopleCountRequestUrl, "moduleId", moduleId)
//        }
//
//        moduleIds.forEachIndexed { index, moduleId ->
//            val peopleRequestUrl = mockServer.takeRequest().requestUrl
//            assertPathUrlParam(peopleRequestUrl, projectIdTest)
//            assertQueryUrlParam(peopleRequestUrl, "moduleId", moduleId)
//            assertQueryUrlParam(peopleRequestUrl, "lastKnownPatientUpdatedAt", peopleToDownload[index].last().updatedAt!!.time) { it?.toLong() }
//            assertQueryUrlParam(peopleRequestUrl, "lastKnownPatientId", peopleToDownload[index].last().patientId)
//        }
//    }
//
//    @Test
//    fun downloadPatientsForUserSync_shouldSuccess() {
//        val localDbMock = Mockito.mock(LocalDbManager::class.java)
//
//        val projectIdTest = "projectIdTest"
//        val userIdTest = "userIdTest"
//        val syncParams = SyncTaskParameters.UserSyncTaskParameters(projectIdTest, userIdTest)
//        val nPeopleToDownload = 22000
//        val peopleToDownload = getRandomPeople(nPeopleToDownload).map { fb_Person(it) }.sortedBy { it.updatedAt }
//
//        makeFakeDownloadRequest(peopleToDownload, localDbMock, 3000, syncParams)
//
//        val peopleRequestUrl = mockServer.takeRequest().requestUrl
//        assertPathUrlParam(peopleRequestUrl, projectIdTest)
//        assertQueryUrlParam(peopleRequestUrl, "userId", userIdTest)
//        assertQueryUrlParam(peopleRequestUrl, "lastKnownPatientUpdatedAt", peopleToDownload.last().updatedAt!!.time) { it?.toLong() }
//        assertQueryUrlParam(peopleRequestUrl, "lastKnownPatientId", peopleToDownload.last().patientId)
//    }
//
//    private fun setupApi() {
//        PeopleRemoteInterface.baseUrl = this.mockServer.url("/").toString()
//        apiClient = SimApiClient(PeopleRemoteInterface::class.java, PeopleRemoteInterface.baseUrl)
//        remotePeopleApi = apiClient.api
//    }
//
//    private fun makeFakeDownloadRequest(
//        peopleToDownload: List<fb_Person>,
//        localDbMock: LocalDbManager,
//        patientsAlreadyInLocalDb: Int,
//        syncParams: SyncTaskParameters) {
//
//        val patientsToDownload = ArrayList(peopleToDownload)
//
//        mockServer.enqueue(mockResponseForDownloadPatients(patientsToDownload))
//
//        mockLocalDbToSavePatientsFromStream(localDbMock)
//
//        whenever(localDbMock.getPeopleCountFromLocal(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull())).thenReturn(Single.just(patientsAlreadyInLocalDb))
//        whenever(localDbMock.getSyncInfoFor(anyNotNull())).thenReturn(Single.create { it.onSuccess(rl_SyncInfo(syncParams.toGroup(), rl_Person(peopleToDownload.last()))) })
//        whenever(localDbMock.updateSyncInfo(anyNotNull())).thenReturn(Completable.complete())
//
//        whenever(preferencesManager.syncGroup).thenReturn(Constants.GROUP.GLOBAL)
//        whenever(preferencesManager.moduleId).thenReturn("")
//
//        whenever(loginInfoManager.getSignedInUserIdOrEmpty()).thenReturn("")
//        whenever(loginInfoManager.getSignedInProjectIdOrEmpty()).thenReturn("")
//
//        whenever(dbManager.remote).thenReturn(remoteDbManagerSpy)
//        whenever(dbManager.remote.getPeopleApiClient()).thenReturn(Single.just(remotePeopleApi))
//
//        whenever(syncStatusDatabase.downSyncStatusModel).thenReturn(mock())
//        syncStatusDatabaseModel = syncStatusDatabase.downSyncStatusModel
//        whenever(syncStatusDatabaseModel.getPeopleToDownSync()).doReturn(25000)
//        doNothing().whenever(syncStatusDatabaseModel).updateLastDownSyncTime(anyLong())
//        doNothing().whenever(syncStatusDatabaseModel).updatePeopleToDownSyncCount(anyInt())
//
//        val sync = PeopleDownSyncTask(remoteDbManagerSpy, dbManager, preferencesManager,
//            loginInfoManager, localDbMock, syncStatusDatabaseModel)
//        sync.syncParams = syncParams
//        sync.execute()
//    }
//
//    private fun makeFakeDownloadRequestsForMultipleModules(
//        peopleToDownloadPerModule: List<List<fb_Person>>,
//        nPatientsForModuleIdFromServer: List<Int>,
//        localDbMock: LocalDbManager,
//        moduleIds: List<String>,
//        patientsAlreadyInLocalDbForModule: List<Int>,
//        syncParams: SyncTaskParameters
//    ): TestObserver<Progress> {
//
//        // Build fake respose for GET patients for each module
//        val patientsToDownloadForEachModule = peopleToDownloadPerModule.map { ArrayList(it) }
//
//        //Mock GET patients-count
//        nPatientsForModuleIdFromServer.forEach {
//            mockServer.enqueue(mockResponseForPatientsCount(it))
//        }
//
//        //Mock GET patients
//        patientsToDownloadForEachModule.forEach {
//            mockServer.enqueue(mockResponseForDownloadPatients(it))
//        }
//
//        //Mock saving patient in Realm
//        mockLocalDbToSavePatientsFromStream(localDbMock)
//
//        //Mock app has already patients in localDb
//        moduleIds.forEachIndexed { index, moduleId ->
//            whenever(localDbMock.getPeopleCountFromLocal(anyNotNull(), anyNotNull(), eq(moduleId), anyNotNull())).thenReturn(Single.just(patientsAlreadyInLocalDbForModule[index]))
//        }
//
//        //Mock app RealmSyncInfo for syncParams
//        moduleIds.forEachIndexed { index, moduleId ->
//            whenever(localDbMock.getSyncInfoFor(anyNotNull(), eq(moduleId))).thenReturn(Single.create { it.onSuccess(rl_SyncInfo(syncParams.toGroup(), rl_Person(peopleToDownloadPerModule[index].last()), null)) })
//        }
//
//        // Mock when trying to save the syncInfo
//        whenever(localDbMock.updateSyncInfo(anyNotNull())).thenReturn(Completable.complete())
//
//        val sync = SyncExecutorMock(DbManagerImpl(localDbMock, remoteDbManagerSpy, secureDataManager, loginInfoManager, preferencesManager, sessionEventsManager, timeHelper, peopleUpSyncMaster), JsonHelper.gson)
//
//        return sync.downloadNewPatients({ false }, syncParams).test()
//    }
//
//    private fun mockLocalDbToSavePatientsFromStream(localDbMock: LocalDbManager) {
//        whenever(localDbMock.savePeopleFromStreamAndUpdateSyncInfo(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull())).thenAnswer { invocation ->
//            Completable.create {
//                val args = invocation.arguments
//                (args[0] as JsonReader).skipValue()
//                val shouldStop = args[3] as (savedPerson: fb_Person) -> Boolean
//                if (shouldStop(fb_Person(PeopleGeneratorUtils.getRandomPerson()))) {
//                    it.onComplete()
//                }
//            }
//        }
//    }
//
//    private fun mockResponseForDownloadPatients(patients: ArrayList<fb_Person>): MockResponse? {
//        val fbPersonJson = JsonHelper.gson.toJson(patients)
//        return MockResponse().let {
//            it.setResponseCode(200)
//            it.setBody(fbPersonJson)
//        }
//    }

    @After
    @Throws
    fun tearDown() {
        mockServer.shutdown()
    }
}
