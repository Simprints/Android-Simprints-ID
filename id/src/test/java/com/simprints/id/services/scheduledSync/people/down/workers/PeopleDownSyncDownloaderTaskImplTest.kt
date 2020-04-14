package com.simprints.id.services.scheduledSync.people.down.workers

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.network.BaseUrlProvider
import com.simprints.core.network.SimApiClientFactory
import com.simprints.core.tools.json.JsonHelper
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
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperationFactoryImpl
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperationResult.DownSyncState.COMPLETE
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.person.remote.PeopleRemoteInterface
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.data.db.person.remote.PipeSeparatorWrapperForURLListParam
import com.simprints.id.data.db.person.remote.models.ApiGetPerson
import com.simprints.id.data.db.person.remote.models.ApiModes
import com.simprints.id.data.db.person.remote.models.fromDomainToGetApi
import com.simprints.id.domain.modality.Modes
import com.simprints.id.exceptions.safe.sync.SyncCloudIntegrationException
import com.simprints.id.services.scheduledSync.people.master.internal.PeopleSyncCache
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.id.tools.TimeHelperImpl
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.common.syntax.assertThrows
import com.simprints.testtools.unit.mockserver.assertPathUrlParam
import com.simprints.testtools.unit.mockserver.assertQueryUrlParam
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.kotlintest.shouldThrow
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.math.ceil


@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class PeopleDownSyncDownloaderTaskImplTest {

    val builder = PeopleDownSyncOperationFactoryImpl()
    private val modes = listOf(Modes.FACE, Modes.FINGERPRINT)
    private val projectSyncOp = PeopleDownSyncOperation(
        DEFAULT_PROJECT_ID,
        null,
        null,
        listOf(Modes.FINGERPRINT),
        null
    )

    private val userSyncOp = PeopleDownSyncOperation(
        DEFAULT_PROJECT_ID,
        DEFAULT_USER_ID,
        null,
        listOf(Modes.FINGERPRINT),
        null
    )

    private val moduleSyncOp = PeopleDownSyncOperation(
        DEFAULT_PROJECT_ID,
        null,
        DEFAULT_MODULE_ID,
        listOf(Modes.FINGERPRINT),
        null
    )

    private val uniqueWorkerId = "uniqueWorkerId"
    private val app = ApplicationProvider.getApplicationContext() as TestApplication

    private var mockServer = MockWebServer()
    private val peopleInFakeDb = mutableListOf<List<Person>>()
    private val syncOpsInFakeDb = mutableListOf<PeopleDownSyncOperation>()

    private lateinit var remoteDbManagerSpy: RemoteDbManager
    @RelaxedMockK lateinit var personRemoteDataSourceMock: PersonRemoteDataSource
    @RelaxedMockK lateinit var downSyncScopeRepository: PeopleDownSyncScopeRepository
    @RelaxedMockK lateinit var personLocalDataSourceMock: PersonLocalDataSource
    @RelaxedMockK lateinit var peopleSyncCache: PeopleSyncCache
    @MockK lateinit var mockBaseUrlProvider: BaseUrlProvider

    private val module by lazy {
        TestAppModule(app,
            syncStatusDatabaseRule = DependencyRule.SpyRule)
    }

    private val dataModule by lazy {
        TestDataModule(projectLocalDataSourceRule = DependencyRule.MockkRule)
    }


    @Before
    fun setUp() {
        UnitTestConfig(this, module, dataModule = dataModule).fullSetup()
        MockKAnnotations.init(this, relaxUnitFun = true)

        remoteDbManagerSpy = spyk(FirebaseManagerImpl(mockk()))
        coEvery { remoteDbManagerSpy.getCurrentToken() } returns ""

        coEvery { personLocalDataSourceMock.insertOrUpdate(capture(peopleInFakeDb)) } returns Unit
        coEvery { downSyncScopeRepository.insertOrUpdate(capture(syncOpsInFakeDb)) } returns Unit

        mockServer.start()

        every { mockBaseUrlProvider.getApiBaseUrl() } returns mockServer.url("/").toString()

        val remotePeopleApi = SimApiClientFactory(
            mockBaseUrlProvider,
            "deviceId"
        ).build<PeopleRemoteInterface>().api

        coEvery { personRemoteDataSourceMock.getPeopleApiClient() } returns remotePeopleApi
    }

    @Test
    fun downloadPatientsForGlobalSync_shouldSuccess() {
        runBlocking {
            val nPeopleToDownload = 407
            val nPeopleToDelete = 0

            runDownSyncAndVerifyConditions(nPeopleToDownload, nPeopleToDelete, projectSyncOp)

            val peopleRequestUrl = mockServer.takeRequest().requestUrl
                ?: throw Throwable("No requests done")
            assertPathUrlParam(peopleRequestUrl, DEFAULT_PROJECT_ID)
        }
    }

    @Test
    fun downloadPatientsForUserSync_shouldSuccess() {
        runBlocking {
            val nPeopleToDownload = 513
            val nPeopleToDelete = 0

            runDownSyncAndVerifyConditions(nPeopleToDownload, nPeopleToDelete, userSyncOp)

            val peopleRequestUrl = mockServer.takeRequest().requestUrl
                ?: throw Throwable("No requests done")
            assertPathUrlParam(peopleRequestUrl, DEFAULT_PROJECT_ID)
            assertQueryUrlParam(peopleRequestUrl, "userId", DEFAULT_USER_ID)
        }
    }

    @Test
    fun downloadPatientsForModuleSync_shouldSuccess() {
        runBlocking {
            val nPeopleToDownload = 513
            val nPeopleToDelete = 0

            runDownSyncAndVerifyConditions(nPeopleToDownload, nPeopleToDelete, moduleSyncOp)

            val peopleRequestUrl = mockServer.takeRequest().requestedUrl()
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

            val peopleRequestUrl = mockServer.takeRequest().requestedUrl()
            assertPathUrlParam(peopleRequestUrl, DEFAULT_PROJECT_ID)
        }
    }

    @Test
    fun deletePatientsForUserSync_shouldSuccess() {
        runBlocking {
            val nPeopleToDownload = 0
            val nPeopleToDelete = 212

            runDownSyncAndVerifyConditions(nPeopleToDownload, nPeopleToDelete, userSyncOp)

            val peopleRequestUrl = mockServer.takeRequest().requestedUrl()
            assertPathUrlParam(peopleRequestUrl, DEFAULT_PROJECT_ID)
            assertQueryUrlParam(peopleRequestUrl, "userId", DEFAULT_USER_ID)
        }
    }

    @Test
    fun deletePatientsForModuleSync_shouldSuccess() {
        runBlocking {
            val nPeopleToDownload = 0
            val nPeopleToDelete = 123

            runDownSyncAndVerifyConditions(nPeopleToDownload, nPeopleToDelete, moduleSyncOp)

            val peopleRequestUrl = mockServer.takeRequest().requestedUrl()
            assertPathUrlParam(peopleRequestUrl, DEFAULT_PROJECT_ID)
            assertQueryUrlParam(peopleRequestUrl, "moduleId", DEFAULT_MODULE_ID)
        }
    }

    @Test
    fun downloadPatients_patientSerializationFails_shouldTriggerOnError() {
        runBlocking {
            val nPeopleToDownload = 499
            val projectDownSyncOp = builder.buildProjectSyncOperation(DEFAULT_PROJECT_ID, modes, null)
            val peopleToDownload = prepareResponseForDownSyncOperation(projectDownSyncOp, nPeopleToDownload)
            mockServer.enqueue(mockSuccessfulResponseWithIncorrectModels(peopleToDownload))

            val sync = PeopleDownSyncDownloaderTaskImpl(
                personLocalDataSourceMock,
                personRemoteDataSourceMock,
                downSyncScopeRepository,
                peopleSyncCache,
                TimeHelperImpl())

            assertThrows<Throwable> {
                sync.execute(projectDownSyncOp, uniqueWorkerId, mockk(relaxed = true))
            }
        }
    }

    @Test
    fun continueFromAPreviousSync_shouldSuccess() {
        runBlocking {
            val nPeopleToDownload = 0
            val nPeopleToDelete = 123

            runDownSyncAndVerifyConditions(nPeopleToDownload, nPeopleToDelete, moduleSyncOp)

            val peopleRequestUrl = mockServer.takeRequest().requestedUrl()
            assertPathUrlParam(peopleRequestUrl, DEFAULT_PROJECT_ID)
            assertQueryUrlParam(peopleRequestUrl, "moduleId", DEFAULT_MODULE_ID)
        }
    }


    @Test
    fun downSyncRequestFailsDueToIntegrationIssue_shouldImmediatelyFail() {
        runBlocking {
            mockServer.enqueue(MockResponse().setResponseCode(404))
            val syncTask = PeopleDownSyncDownloaderTaskImpl(
                personLocalDataSourceMock,
                personRemoteDataSourceMock,
                downSyncScopeRepository,
                peopleSyncCache,
                TimeHelperImpl())

            shouldThrow<SyncCloudIntegrationException> {
                syncTask.execute(projectSyncOp, uniqueWorkerId, mockk(relaxed = true))
            }
            assertThat(mockServer.requestCount).isEqualTo(1)
        }
    }


    @Test
    fun downSyncRequestFailsDueToNetworkIssue_shouldRetry() {
        runBlocking {
            val clientMock = mockClientToThrowFirstAndThenExecuteNetworkCall()
            coEvery { personRemoteDataSourceMock.getPeopleApiClient() } returns clientMock

            runDownSyncAndVerifyConditions(100, 0, projectSyncOp)

            assertThat(mockServer.requestCount).isEqualTo(1)
            coVerify(exactly = 2) { clientMock.downSync(any(), any(), any(), any(), any(), any()) }
        }
    }

    @Test
    fun downSyncRequestFailsDueToMalformedJson_shouldSaveTheWellFormedElements() {
        runBlocking {
            val mockMalformedResponse = buildMalformedResponse(4)
            mockServer.enqueue(mockMalformedResponse)

            val syncTask = PeopleDownSyncDownloaderTaskImpl(
                personLocalDataSourceMock,
                personRemoteDataSourceMock,
                downSyncScopeRepository,
                peopleSyncCache,
                TimeHelperImpl())

            shouldThrow<Throwable> {
                syncTask.execute(projectSyncOp, uniqueWorkerId, mockk(relaxed = true))
            }
            assertThat(mockServer.requestCount).isEqualTo(1)
            coVerify(exactly = 1) {
                personLocalDataSourceMock.insertOrUpdate(match {
                    assertThat(it).hasSize(3)
                    true
                })
            }
        }
    }

    private fun buildMalformedResponse(malformedIndex: Int): MockResponse {
        val response = mockSuccessfulResponseForPatients(prepareResponseForDownSyncOperation(projectSyncOp, 10))
        val bodyString = String(response.getBody()?.readByteArray() ?: byteArrayOf())
        val elements = bodyString.split("projectId")
        val malformedElement = elements[malformedIndex - 1].replaceFirst("userId", "userId\"")
        val newElements = (elements.subList(0, malformedIndex) + malformedElement + elements.subList(malformedIndex, 9)).joinToString(separator = "projectId")
        response.setBody(newElements)
        return response
    }

    private fun mockClientToThrowFirstAndThenExecuteNetworkCall(): PeopleRemoteInterface {
        val remotePeopleApi = SimApiClientFactory(
            mockBaseUrlProvider,
            "deviceId"
        ).build<PeopleRemoteInterface>().api
        return mockk {
            coEvery { downSync(any(), any(), any(), any(), any(), any()) } throws Throwable("Network issue") coAndThen {
                remotePeopleApi.downSync(
                    args[0] as String,
                    args[1] as String?,
                    args[2] as String?,
                    args[3] as String?,
                    args[4] as Long?,
                    args[5] as PipeSeparatorWrapperForURLListParam<ApiModes>)
            }
        }
    }


    private suspend fun runDownSyncAndVerifyConditions(
        nPeopleToDownload: Int,
        nPeopleToDelete: Int,
        downSyncOps: PeopleDownSyncOperation) {

        val batchesForSavingInLocal = calculateCorrectNumberOfBatches(nPeopleToDownload)
        val batchesForDeletion = calculateCorrectNumberOfBatches(nPeopleToDelete)

        val peopleToDownload = prepareResponseForDownSyncOperation(downSyncOps, nPeopleToDownload)
        val peopleToDelete = peopleResponseForSubScopeForDeletion(downSyncOps, nPeopleToDelete)
        val opsStream = peopleToDelete.plus(peopleToDownload)

        mockServer.enqueue(mockSuccessfulResponseForPatients(opsStream))

        val syncTask = PeopleDownSyncDownloaderTaskImpl(
            personLocalDataSourceMock,
            personRemoteDataSourceMock,
            downSyncScopeRepository,
            peopleSyncCache,
            TimeHelperImpl())

        syncTask.execute(downSyncOps, uniqueWorkerId, mockk(relaxed = true))

        coVerify(exactly = batchesForSavingInLocal) { personLocalDataSourceMock.insertOrUpdate(any()) }
        coVerify(exactly = batchesForDeletion) { personLocalDataSourceMock.delete(any()) }

        val lastPatient = opsStream.lastOrNull()
        val lastDownSyncOpResult = syncOpsInFakeDb.last()
        with(lastDownSyncOpResult) {
            assertThat(projectId).isEqualTo(downSyncOps.projectId)
            assertThat(userId).isEqualTo(downSyncOps.userId)
            assertThat(moduleId).isEqualTo(downSyncOps.moduleId)
            assertThat(lastResult?.state).isEqualTo(COMPLETE)
            assertThat(lastResult?.lastPatientId).isEqualTo(lastPatient?.id)
            assertThat(lastResult?.lastPatientUpdatedAt).isEqualTo(lastPatient?.updatedAt?.time)
            assertThat(lastResult?.lastSyncTime).isNotNull()
        }

        if (nPeopleToDownload > 0) {
            verifyLastPatientSaveIsTheRightOne(peopleInFakeDb.flatten(), peopleToDownload)
        }
    }


    private fun verifyLastPatientSaveIsTheRightOne(saved: List<Person>, inResponse: List<ApiGetPerson>) {
        Assert.assertEquals(saved.last().patientId, inResponse.last().id)
        Assert.assertEquals(saved.last().patientId, inResponse.last().id)
    }

    private fun prepareResponseForDownSyncOperation(downSyncOp: PeopleDownSyncOperation, nPeople: Int) =
        getRandomPeople(nPeople, downSyncOp, listOf(false)).map { it.fromDomainToGetApi() }.sortedBy { it.updatedAt }

    private fun peopleResponseForSubScopeForDeletion(downSyncOp: PeopleDownSyncOperation, nPeople: Int) =
        getRandomPeople(nPeople, downSyncOp, listOf(false)).map { it.fromDomainToGetApi(true) }.sortedBy { it.updatedAt }

    private fun mockSuccessfulResponseForPatients(patients: List<ApiGetPerson>): MockResponse {
        val fbPersonJson = JsonHelper.gson.toJson(patients)
        return MockResponse().let {
            it.setResponseCode(200)
            it.setBody(fbPersonJson)
        }
    }

    private fun mockSuccessfulResponseWithIncorrectModels(patients: List<ApiGetPerson>): MockResponse {
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

fun RecordedRequest.requestedUrl() = this.requestUrl ?: throw Throwable("No request done")
