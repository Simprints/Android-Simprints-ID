package com.simprints.id.services.scheduledSync.peopleDownSync

import com.google.firebase.FirebaseApp
import com.nhaarman.mockito_kotlin.*
import com.simprints.id.activities.ShadowAndroidXMultiDex
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.room.DownSyncStatus
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.db.remote.models.toFirebasePerson
import com.simprints.id.data.db.remote.network.PeopleRemoteInterface
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.Constants
import com.simprints.id.network.SimApiClient
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.tasks.DownSyncTaskImpl
import com.simprints.id.shared.PeopleGeneratorUtils.getRandomPeople
import com.simprints.id.shared.anyNotNull
import com.simprints.id.shared.whenever
import com.simprints.id.testUtils.base.RxJavaTest
import com.simprints.id.testUtils.mockServer.assertPathUrlParam
import com.simprints.id.testUtils.mockServer.assertQueryUrlParam
import com.simprints.id.testUtils.roboletric.TestApplication
import com.simprints.id.tools.TimeHelperImpl
import com.simprints.id.tools.json.JsonHelper
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.math.ceil

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class PeopleDownSyncTaskTest : RxJavaTest {

    private var mockServer = MockWebServer()
    private lateinit var apiClient: SimApiClient<PeopleRemoteInterface>
    private lateinit var remotePeopleApi: PeopleRemoteInterface

    private val remoteDbManagerSpy: RemoteDbManager = spy()
    private val syncStatusDatabase: SyncStatusDatabase = mock()
    private val loginInfoManager: LoginInfoManager = mock()
    private val preferencesManager: PreferencesManager = mock()

    @Before
    fun setUp() {
        FirebaseApp.initializeApp(RuntimeEnvironment.application)

        whenever(remoteDbManagerSpy.getCurrentFirestoreToken()).thenReturn(Single.just(""))

        mockServer.start()
        setupApi()
    }

    @Test
    fun downloadPatientsForGlobalSync_shouldSuccess() {
        val localDbMock = Mockito.mock(LocalDbManager::class.java)

        val projectIdTest = "projectIDTest"
        val subSyncScope = SubSyncScope(projectId = projectIdTest, userId = null, moduleId = null)
        val nPeopleToDownload = 407
        val peopleToDownload = getRandomPeople(nPeopleToDownload).map { it.toFirebasePerson() }.sortedBy { it.updatedAt }

        mockServer.enqueue(mockSuccessfulResponseForDownloadPatients(peopleToDownload))
        val testObserver = makeFakeDownloadRequest(peopleToDownload, localDbMock, subSyncScope)
        testObserver.awaitTerminalEvent()
        testObserver.assertNoErrors()

        val peopleRequestUrl = mockServer.takeRequest().requestUrl
        assertPathUrlParam(peopleRequestUrl, projectIdTest)
        verify(localDbMock, times(calculateCorrectNumberOfBatches(nPeopleToDownload))).insertOrUpdatePeopleInLocal(anyNotNull())
    }

    @Test
    fun downloadPatientsForModuleSync_shouldSuccess() {
        val localDbMock = Mockito.mock(LocalDbManager::class.java)

        val projectIdTest = "projectIdTest"
        val moduleIdTest = "moduleIdTest"
        val subSyncScope = SubSyncScope(projectId = projectIdTest, userId = null, moduleId = moduleIdTest)
        val nPeopleToDownload = 513
        val peopleToDownload = getRandomPeople(nPeopleToDownload).map { it.toFirebasePerson() }.sortedBy { it.updatedAt }

        mockServer.enqueue(mockSuccessfulResponseForDownloadPatients(peopleToDownload))
        val testObserver = makeFakeDownloadRequest(peopleToDownload, localDbMock, subSyncScope)
        testObserver.awaitTerminalEvent()
        testObserver.assertNoErrors()

        val peopleRequestUrl = mockServer.takeRequest().requestUrl
        assertPathUrlParam(peopleRequestUrl, projectIdTest)
        assertQueryUrlParam(peopleRequestUrl, "moduleId", moduleIdTest)
        verify(localDbMock, times(calculateCorrectNumberOfBatches(nPeopleToDownload))).insertOrUpdatePeopleInLocal(anyNotNull())
    }

    @Test
    fun downloadPatientsForUserSync_shouldSuccess() {
        val localDbMock = Mockito.mock(LocalDbManager::class.java)

        val projectIdTest = "projectIdTest"
        val userIdTest = "userIdTest"
        val subSyncScope = SubSyncScope(projectId = projectIdTest, userId = userIdTest, moduleId = null)
        val nPeopleToDownload = 789
        val peopleToDownload = getRandomPeople(nPeopleToDownload).map { it.toFirebasePerson() }.sortedBy { it.updatedAt }

        mockServer.enqueue(mockSuccessfulResponseForDownloadPatients(peopleToDownload))
        val testObserver = makeFakeDownloadRequest(peopleToDownload, localDbMock, subSyncScope)
        testObserver.awaitTerminalEvent()
        testObserver.assertNoErrors()
        testObserver.assertError { true }

        val peopleRequestUrl = mockServer.takeRequest().requestUrl
        assertPathUrlParam(peopleRequestUrl, projectIdTest)
        assertQueryUrlParam(peopleRequestUrl, "userId", userIdTest)
        verify(localDbMock, times(calculateCorrectNumberOfBatches(nPeopleToDownload))).insertOrUpdatePeopleInLocal(anyNotNull())
    }

    @Test
    fun downloadPatients_patientSerializationFails_shouldTriggerOnError() {
        val localDbMock = Mockito.mock(LocalDbManager::class.java)

        val projectIdTest = "projectIDTest"
        val subSyncScope = SubSyncScope(projectId = projectIdTest, userId = null, moduleId = null)
        val nPeopleToDownload = 499
        val peopleToDownload = getRandomPeople(nPeopleToDownload).map { it.toFirebasePerson() }.sortedBy { it.updatedAt }

        mockServer.enqueue(mockSuccessfulResponseWithIncorrectModels(peopleToDownload))
        val testObserver = makeFakeDownloadRequest(peopleToDownload, localDbMock, subSyncScope)
        testObserver.awaitTerminalEvent()
        testObserver.assertError { true }
    }

    private fun setupApi() {
        PeopleRemoteInterface.baseUrl = this.mockServer.url("/").toString()
        apiClient = SimApiClient(PeopleRemoteInterface::class.java, PeopleRemoteInterface.baseUrl)
        remotePeopleApi = apiClient.api
    }

    private fun makeFakeDownloadRequest(
        peopleToDownload: List<fb_Person>,
        localDbMock: LocalDbManager,
        subSyncScope: SubSyncScope): TestObserver<Void> {

        whenever(localDbMock.insertOrUpdatePeopleInLocal(anyNotNull())).doReturn(Completable.complete())

        whenever(preferencesManager.syncGroup).thenReturn(Constants.GROUP.GLOBAL)
        whenever(preferencesManager.moduleId).thenReturn("")

        whenever(loginInfoManager.getSignedInUserIdOrEmpty()).thenReturn("")
        whenever(loginInfoManager.getSignedInProjectIdOrEmpty()).thenReturn("")

        whenever(remoteDbManagerSpy.getPeopleApiClient()).thenReturn(Single.just(remotePeopleApi))

        whenever(syncStatusDatabase.downSyncDao).thenReturn(mock())
        val syncStatusDatabaseModel = syncStatusDatabase.downSyncDao
        whenever(syncStatusDatabaseModel.getDownSyncStatusForId(anyString())).doReturn(DownSyncStatus(subSyncScope.projectId, totalToDownload = peopleToDownload.size))
        doNothing().whenever(syncStatusDatabaseModel).updateLastSyncTime(anyString(), anyLong())
        doNothing().whenever(syncStatusDatabaseModel).updatePeopleToDownSync(anyString(), anyInt())

        val sync = DownSyncTaskImpl(localDbMock, remoteDbManagerSpy, TimeHelperImpl(), syncStatusDatabase)
        return sync.execute(subSyncScope).test()
    }

    private fun mockSuccessfulResponseForDownloadPatients(patients: List<fb_Person>): MockResponse? {
        val fbPersonJson = JsonHelper.gson.toJson(patients)
        return MockResponse().let {
            it.setResponseCode(200)
            it.setBody(fbPersonJson)
        }
    }

    private fun mockSuccessfulResponseWithIncorrectModels(patients: List<fb_Person>): MockResponse? {
        val fbPersonJson = JsonHelper.gson.toJson(patients)
        val badFbPersonJson = fbPersonJson.replace("fingerprints", "fungerprints")
        return MockResponse().let {
            it.setResponseCode(200)
            it.setBody(badFbPersonJson)
        }
    }

    private fun calculateCorrectNumberOfBatches(nPeopleToDownload: Int) =
        ceil(nPeopleToDownload.toDouble() / DownSyncTaskImpl.BATCH_SIZE_FOR_DOWNLOADING.toDouble()).toInt()

    @After
    @Throws
    fun tearDown() {
        mockServer.shutdown()
    }
}
