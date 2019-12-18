package com.simprints.id.services.scheduledSync.sync.peopleDownSync.workers.downsync

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.core.network.SimApiClient
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.commontesttools.DefaultTestConstants
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.commontesttools.PeopleGeneratorUtils.getRandomPeople
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.commontesttools.di.TestDataModule
import com.simprints.id.data.db.common.FirebaseManagerImpl
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperation
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.PeopleRemoteInterface
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.data.db.person.remote.models.ApiGetPerson
import com.simprints.id.data.db.person.remote.models.fromDomainToGetApi
import com.simprints.id.domain.modality.Modes
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncDownloaderTaskImpl
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.id.tools.TimeHelperImpl
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.common.syntax.assertThrows
import com.simprints.testtools.unit.mockserver.assertPathUrlParam
import com.simprints.testtools.unit.mockserver.assertQueryUrlParam
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.*
import io.reactivex.Single
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import kotlin.math.ceil

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class PeopleDownSyncDownloaderTaskImplTest {

    private val modes = listOf(Modes.FACE, Modes.FINGERPRINT)
    private val projectSyncOp = PeopleDownSyncOperation(
        DefaultTestConstants.DEFAULT_PROJECT_ID,
        null,
        null,
        listOf(Modes.FINGERPRINT),
        null
    )

    private val app = ApplicationProvider.getApplicationContext() as TestApplication

    private var mockServer = MockWebServer()
    private val remoteDbManagerSpy: RemoteDbManager = spyk(FirebaseManagerImpl(mockk()))
    private val personRemoteDataSourceMock: PersonRemoteDataSource = mockk(relaxed = true)
    private val downSyncScopeRepository: PeopleDownSyncScopeRepository = mockk(relaxed = true)

    private val module by lazy {
        TestAppModule(app,
            syncStatusDatabaseRule = DependencyRule.SpyRule)
    }

    private val dataModule by lazy {
        TestDataModule(projectLocalDataSourceRule = DependencyRule.MockkRule)
    }


    @Before
    fun setUp() {
        ShadowLog.stream = System.out

        UnitTestConfig(this, module, dataModule = dataModule).fullSetup()

        every { remoteDbManagerSpy.getCurrentToken() } returns Single.just("")
        mockServer.start()

        PeopleRemoteInterface.baseUrl = this.mockServer.url("/").toString()
        val remotePeopleApi: PeopleRemoteInterface = SimApiClient(PeopleRemoteInterface::class.java, PeopleRemoteInterface.baseUrl).api

        coEvery { personRemoteDataSourceMock.getPeopleApiClient() } returns remotePeopleApi
    }

    @Test
    fun downloadPatientsForGlobalSync_shouldSuccess() {
        runBlocking {
            val nPeopleToDownload = 407
            val nPeopleToDelete = 0

            runDownSyncAndVerifyConditions(nPeopleToDownload, nPeopleToDelete, projectSyncOp)

            val peopleRequestUrl = mockServer.takeRequest().requestUrl
            assertPathUrlParam(peopleRequestUrl, DEFAULT_PROJECT_ID)
        }
    }

    @Test
    fun downloadPatientsForUserSync_shouldSuccess() {
        runBlocking {
            val nPeopleToDownload = 513
            val nPeopleToDelete = 0

            runDownSyncAndVerifyConditions(nPeopleToDownload, nPeopleToDelete, projectSyncOp)

            val peopleRequestUrl = mockServer.takeRequest().requestUrl
            assertPathUrlParam(peopleRequestUrl, DEFAULT_PROJECT_ID)
            assertQueryUrlParam(peopleRequestUrl, "userId", DEFAULT_USER_ID)
        }
    }

    @Test
    fun downloadPatientsForModuleSync_shouldSuccess() {
        runBlocking {
            val nPeopleToDownload = 513
            val nPeopleToDelete = 0

            runDownSyncAndVerifyConditions(nPeopleToDownload, nPeopleToDelete, projectSyncOp)

            val peopleRequestUrl = mockServer.takeRequest().requestUrl
            assertPathUrlParam(peopleRequestUrl, DEFAULT_PROJECT_ID)
            assertQueryUrlParam(peopleRequestUrl, "moduleId", DEFAULT_MODULE_ID)
        }
    }

    @Test
    fun deletePatientsForGlobalSync_shouldSuccess() {
        runBlocking {
            val nPeopleToDownload = 0
            val nPeopleToDelete = 300

            runDownSyncAndVerifyConditions(nPeopleToDownload, nPeopleToDelete, projectSyncOp)

            val peopleRequestUrl = mockServer.takeRequest().requestUrl
            assertPathUrlParam(peopleRequestUrl, DEFAULT_PROJECT_ID)
        }
    }

    @Test
    fun deletePatientsForUserSync_shouldSuccess() {
        runBlocking {
            val nPeopleToDownload = 0
            val nPeopleToDelete = 212

            runDownSyncAndVerifyConditions(nPeopleToDownload, nPeopleToDelete, projectSyncOp)

            val peopleRequestUrl = mockServer.takeRequest().requestUrl
            assertPathUrlParam(peopleRequestUrl, DEFAULT_PROJECT_ID)
            assertQueryUrlParam(peopleRequestUrl, "userId", DEFAULT_USER_ID)
        }
    }

    @Test
    fun deletePatientsForModuleSync_shouldSuccess() {
        runBlocking {
            val nPeopleToDownload = 0
            val nPeopleToDelete = 123

            runDownSyncAndVerifyConditions(nPeopleToDownload, nPeopleToDelete, projectSyncOp)

            val peopleRequestUrl = mockServer.takeRequest().requestUrl
            assertPathUrlParam(peopleRequestUrl, DEFAULT_PROJECT_ID)
            assertQueryUrlParam(peopleRequestUrl, "moduleId", DEFAULT_MODULE_ID)
        }
    }

    @Test
    fun downloadPatients_patientSerializationFails_shouldTriggerOnError() {
        runBlocking {
            val personLocalDataSourceMock = mockk<PersonLocalDataSource>()
            val nPeopleToDownload = 499
            val projectDownSyncOp = PeopleDownSyncOperation.buildProjectOperation(DEFAULT_PROJECT_ID, modes, null)
            val peopleToDownload = prepareResponseForSubScopeForDownloading(projectDownSyncOp, nPeopleToDownload)
            mockServer.enqueue(mockSuccessfulResponseWithIncorrectModels(peopleToDownload))

            val sync = PeopleDownSyncDownloaderTaskImpl(
                personLocalDataSourceMock,
                personRemoteDataSourceMock,
                downSyncScopeRepository,
                TimeHelperImpl())

            assertThrows<Throwable> {
                sync.execute(projectDownSyncOp, mockk(relaxed = true))
            }
        }
    }

    //    @Test
//    fun continueFromAPreviousSync_shouldSuccess() {
//        val nPeopleToDownload = 407
//        val scope = ProjectSyncScope(PROJECT_ID, listOf(Modes.FINGERPRINT))
//        val lastPatientIdFromRoom = "lastPatientId"
//        val lastPatientUpdateAtFromRoom = 123123123L
//
//        val personLocalDataSourceMock = mockk<PersonLocalDataSource>()
//        val syncLocalDataSourceMock = mock<SyncInfoLocalDataSource>()
//        val subScope = scope.toSubSyncScopes().first()
//        doReturnScopeFromBuilder(scope)
//
//        val peopleToDownload = prepareResponseForSubScopeForDownloading(subScope, nPeopleToDownload)
//        mockServer.enqueue(mockSuccessfulResponseForPatients(peopleToDownload))
//
//        mockDbDependencies(
//            syncLocalDataSourceMock,
//            personLocalDataSourceMock,
//            DownSyncStatus(
//                subScope, lastPatientId = lastPatientIdFromRoom,
//                lastPatientUpdatedAt = lastPatientUpdateAtFromRoom,
//                totalToDownload = nPeopleToDownload))
//
//        val sync = DownSyncTaskImpl(personLocalDataSourceMock, syncLocalDataSourceMock, personRemoteDataSourceSpy, TimeHelperImpl(), downSyncDao)
//        sync.execute(subScope).test().awaitAndAssertSuccess()
//
//        val peopleRequestUrl = mockServer.takeRequest().requestUrl
//        assertPathUrlParam(peopleRequestUrl, PROJECT_ID)
//        assertQueryUrlParam(peopleRequestUrl, "lastKnownPatientId", lastPatientIdFromRoom)
//        assertQueryUrlParam(peopleRequestUrl, "lastKnownPatientUpdatedAt", "$lastPatientUpdateAtFromRoom")
//    }
//
//    @Test
//    fun continueFromAPreviousSyncFromRealm_shouldSuccess() {
//        val nPeopleToDownload = 1
//        val scope = DownSyncScope(projectId = PROJECT_ID, userId = null, moduleIds = null)
//        val lastPatientId = "lastPatientId"
//        val lastPatientUpdateAt = Date()
//
//        val personLocalDataSourceMock = mock<PersonLocalDataSource>()
//        val syncLocalDataSourceMock = mock<SyncInfoLocalDataSource>()
//        val subScope = scope.toSubSyncScopes().first()
//        doReturnScopeFromBuilder(scope)
//        mockDbDependencies(syncLocalDataSourceMock, personLocalDataSourceMock, DownSyncStatus(subScope, totalToDownload = nPeopleToDownload))
//
//        whenever(syncLocalDataSourceMock) { load(subScope) } thenReturn
//            DbSyncInfo(scope.group, getRandomPerson(lastPatientId, updateAt = lastPatientUpdateAt).fromDomainToDb(), null)
//
//        val argForInsertOrReplaceDownSyncStatus = argumentCaptor<DownSyncStatus>()
//        whenever(downSyncDao) { insertOrReplaceDownSyncStatus(argForInsertOrReplaceDownSyncStatus.capture()) } thenDoNothing {}
//
//        val peopleToDownload = prepareResponseForSubScopeForDownloading(subScope, nPeopleToDownload)
//        mockServer.enqueue(mockSuccessfulResponseForPatients(peopleToDownload))
//
//        val sync = DownSyncTaskImpl(personLocalDataSourceMock, syncLocalDataSourceMock, personRemoteDataSourceSpy, TimeHelperImpl(), downSyncDao)
//        sync.execute(subScope).test().awaitAndAssertSuccess()
//
//        val peopleRequestUrl = mockServer.takeRequest().requestUrl
//        assertPathUrlParam(peopleRequestUrl, PROJECT_ID)
//        assertQueryUrlParam(peopleRequestUrl, "lastKnownPatientId", lastPatientId)
//        assertQueryUrlParam(peopleRequestUrl, "lastKnownPatientUpdatedAt", "${lastPatientUpdateAt.time}")
//
//        val downSyncStatusAfterRealmMigration = argForInsertOrReplaceDownSyncStatus.firstValue
//        Assert.assertEquals(downSyncStatusAfterRealmMigration.id, downSyncDao.getStatusId(subScope))
//        Assert.assertEquals(downSyncStatusAfterRealmMigration.lastPatientUpdatedAt, lastPatientUpdateAt.time)
//        Assert.assertEquals(downSyncStatusAfterRealmMigration.lastPatientId, lastPatientId)
//        verifyBlockingAtLeast(1, syncLocalDataSourceMock) { delete(anyNotNull()) }
//    }
//
    private suspend fun runDownSyncAndVerifyConditions(
        nPeopleToDownload: Int,
        nPeopleToDelete: Int,
        downSyncOps: PeopleDownSyncOperation,
        lastPatientId: String? = null,
        lastPatientUpdateAt: Long? = null) {

        val personLocalDataSourceMock = mockk<PersonLocalDataSource>(relaxed = true)
        val batchesForSavingInLocal = calculateCorrectNumberOfBatches(nPeopleToDownload)
        val batchesForDeletion = calculateCorrectNumberOfBatches(nPeopleToDelete)
        val batchesForOperations = calculateCorrectNumberOfBatches(nPeopleToDownload + nPeopleToDelete)

        val peopleToDownload = prepareResponseForSubScopeForDownloading(downSyncOps, nPeopleToDownload)
        val peopleToDelete = peopleResponseForSubScopeForDeletion(downSyncOps, nPeopleToDelete)
        mockServer.enqueue(mockSuccessfulResponseForPatients(peopleToDelete.plus(peopleToDownload)))

        val list = mutableListOf<List<Person>>()
        coEvery { personLocalDataSourceMock.insertOrUpdate(capture(list)) } returns Unit

        val syncTask = PeopleDownSyncDownloaderTaskImpl(
            personLocalDataSourceMock,
            personRemoteDataSourceMock,
            downSyncScopeRepository,
            TimeHelperImpl())

        syncTask.execute(downSyncOps, mockk(relaxed = true))

        coVerify(exactly = batchesForSavingInLocal) { personLocalDataSourceMock.insertOrUpdate(any()) }
        coVerify(exactly = batchesForDeletion) { personLocalDataSourceMock.delete(any()) }
        coVerify { downSyncScopeRepository.insertOrUpdate(any()) }

        verifyLastPatientSaveIsTheRightOne(list.flatten(), peopleToDownload)
    }

    private fun verifyLastPatientSaveIsTheRightOne(saved: List<Person>, inResponse: List<ApiGetPerson>) {
        Assert.assertEquals(saved.last().patientId, inResponse.last().id)
        Assert.assertEquals(saved.last().patientId, inResponse.last().id)
    }

    private fun prepareResponseForSubScopeForDownloading(downSyncOp: PeopleDownSyncOperation, nPeople: Int) =
        getRandomPeople(nPeople, downSyncOp, listOf(false)).map { it.fromDomainToGetApi() }.sortedBy { it.updatedAt }

    private fun peopleResponseForSubScopeForDeletion(downSyncOp: PeopleDownSyncOperation, nPeople: Int) =
        getRandomPeople(nPeople, downSyncOp, listOf(false)).map { it.fromDomainToGetApi(true) }.sortedBy { it.updatedAt }

    private fun mockSuccessfulResponseForPatients(patients: List<ApiGetPerson>): MockResponse? {
        val fbPersonJson = JsonHelper.gson.toJson(patients)
        return MockResponse().let {
            it.setResponseCode(200)
            it.setBody(fbPersonJson)
        }
    }

    private fun mockSuccessfulResponseWithIncorrectModels(patients: List<ApiGetPerson>): MockResponse? {
        val fbPersonJson = JsonHelper.gson.toJson(patients)
        val badFbPersonJson = fbPersonJson.replace("id", "id_wrong")
        return MockResponse().let {
            it.setResponseCode(200)
            it.setBody(badFbPersonJson)
        }
    }

    private fun calculateCorrectNumberOfBatches(nPeople: Int) =
        ceil(nPeople.toDouble() / PeopleDownSyncDownloaderTaskImpl.BATCH_SIZE_FOR_DOWNLOADING.toDouble()).toInt()


    @After
    @Throws
    fun tearDown() {
        mockServer.shutdown()
    }
}
