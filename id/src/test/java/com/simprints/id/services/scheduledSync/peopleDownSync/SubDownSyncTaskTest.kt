package com.simprints.id.services.scheduledSync.peopleDownSync

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.simprints.core.network.SimApiClient
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.commontesttools.PeopleGeneratorUtils.getRandomPeople
import com.simprints.id.commontesttools.PeopleGeneratorUtils.getRandomPerson
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.commontesttools.di.TestDataModule
import com.simprints.id.data.db.syncinfo.local.models.DbSyncInfo
import com.simprints.id.data.db.person.local.models.toRealmPerson
import com.simprints.id.data.db.syncstatus.downsyncinfo.DownSyncDao
import com.simprints.id.data.db.syncstatus.downsyncinfo.DownSyncStatus
import com.simprints.id.data.db.syncstatus.downsyncinfo.getStatusId
import com.simprints.id.data.db.common.FirebaseManagerImpl
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.person.remote.models.ApiGetPerson
import com.simprints.id.data.db.person.remote.models.toApiGetPerson
import com.simprints.id.data.db.person.remote.PeopleRemoteInterface
import com.simprints.id.data.db.person.remote.PersonRemoteDataSource
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.data.db.person.local.PersonLocalDataSource
import com.simprints.id.data.db.project.local.ProjectLocalDataSource
import com.simprints.id.data.db.syncinfo.local.SyncInfoLocalDataSource
import com.simprints.id.exceptions.safe.data.db.NoSuchDbSyncInfoException
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilder
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.tasks.DownSyncTaskImpl
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.id.tools.TimeHelperImpl
import com.simprints.testtools.common.syntax.*
import com.simprints.testtools.unit.mockserver.assertPathUrlParam
import com.simprints.testtools.unit.mockserver.assertQueryUrlParam
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import java.util.*
import javax.inject.Inject
import kotlin.math.ceil

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class SubDownSyncTaskTest {

    private val app = ApplicationProvider.getApplicationContext() as TestApplication

    private var mockServer = MockWebServer()
    private lateinit var apiClient: SimApiClient<PeopleRemoteInterface>
    private lateinit var remotePeopleApi: PeopleRemoteInterface

    @Inject lateinit var syncScopeBuilderSpy: SyncScopesBuilder

    private val remoteDbManagerSpy: RemoteDbManager = spy(FirebaseManagerImpl(mock()))
    private val personRemoteDataSourceSpy: PersonRemoteDataSource = spy()
    private val downSyncDao: DownSyncDao = mock()

    private val module by lazy {
        TestAppModule(app,
            syncScopesBuilderRule = DependencyRule.MockRule,
            syncStatusDatabaseRule = DependencyRule.SpyRule)
    }

    private val dataModule by lazy {
        TestDataModule(projectLocalDataSourceRule = DependencyRule.MockRule)
    }


    @Before
    fun setUp() {
        ShadowLog.stream = System.out

        UnitTestConfig(this, module, dataModule = dataModule).fullSetup()

        whenever(remoteDbManagerSpy.getCurrentToken()).thenReturn(Single.just(""))
        mockServer.start()
        setupApi()
        whenever(personRemoteDataSourceSpy.getPeopleApiClient()).thenReturn(Single.just(remotePeopleApi))
    }

    @Test
    fun downloadPatientsForGlobalSync_shouldSuccess() {
        val nPeopleToDownload = 407
        val scope = SyncScope(projectId = PROJECT_ID, userId = null, moduleIds = null)

        runDownSyncAndVerifyConditions(nPeopleToDownload, scope)

        val peopleRequestUrl = mockServer.takeRequest().requestUrl
        assertPathUrlParam(peopleRequestUrl, PROJECT_ID)
    }

    @Test
    fun downloadPatientsForModuleSync_shouldSuccess() {
        val nPeopleToDownload = 513
        val scope = SyncScope(projectId = PROJECT_ID, userId = USER_ID, moduleIds = null)

        runDownSyncAndVerifyConditions(nPeopleToDownload, scope)

        val peopleRequestUrl = mockServer.takeRequest().requestUrl
        assertPathUrlParam(peopleRequestUrl, PROJECT_ID)
        assertQueryUrlParam(peopleRequestUrl, "userId", USER_ID)
    }

    @Test
    fun downloadPatientsForUserSync_shouldSuccess() {
        val nPeopleToDownload = 513
        val scope = SyncScope(projectId = PROJECT_ID, userId = null, moduleIds = setOf(MODULE_ID))

        runDownSyncAndVerifyConditions(nPeopleToDownload, scope)

        val peopleRequestUrl = mockServer.takeRequest().requestUrl
        assertPathUrlParam(peopleRequestUrl, PROJECT_ID)
        assertQueryUrlParam(peopleRequestUrl, "moduleId", MODULE_ID)
    }

    @Test
    fun downloadPatients_patientSerializationFails_shouldTriggerOnError() {
        val personLocalDataSourceMock = mock<PersonLocalDataSource>()
        val syncLocalDataSourceMock = mock<SyncInfoLocalDataSource>()
        val nPeopleToDownload = 499
        val scope = SyncScope(projectId = "projectIDTest", userId = null, moduleIds = null)
        val subScope = scope.toSubSyncScopes().first()
        doReturnScopeFromBuilder(scope)
        val peopleToDownload = prepareResponseForSubScope(subScope, nPeopleToDownload)
        mockServer.enqueue(mockSuccessfulResponseWithIncorrectModels(peopleToDownload))

        mockDbDependencies(syncLocalDataSourceMock, personLocalDataSourceMock, DownSyncStatus(subScope, totalToDownload = nPeopleToDownload))

        val sync = DownSyncTaskImpl(personLocalDataSourceMock, syncLocalDataSourceMock, personRemoteDataSourceSpy, TimeHelperImpl(), downSyncDao)
        val testObserver = sync.execute(subScope).test()
        testObserver.awaitTerminalEvent()
        testObserver.assertError { true }
    }

    @Test
    fun continueFromAPreviousSync_shouldSuccess() {
        val nPeopleToDownload = 407
        val scope = SyncScope(projectId = PROJECT_ID, userId = null, moduleIds = null)
        val lastPatientIdFromRoom = "lastPatientId"
        val lastPatientUpdateAtFromRoom = 123123123L

        val personLocalDataSourceMock = mock<PersonLocalDataSource>()
        val syncLocalDataSourceMock = mock<SyncInfoLocalDataSource>()
        val subScope = scope.toSubSyncScopes().first()
        doReturnScopeFromBuilder(scope)

        val peopleToDownload = prepareResponseForSubScope(subScope, nPeopleToDownload)
        mockServer.enqueue(mockSuccessfulResponseForDownloadPatients(peopleToDownload))

        mockDbDependencies(
            syncLocalDataSourceMock,
            personLocalDataSourceMock,
            DownSyncStatus(
                subScope, lastPatientId = lastPatientIdFromRoom,
                lastPatientUpdatedAt = lastPatientUpdateAtFromRoom,
                totalToDownload = nPeopleToDownload))

        val sync = DownSyncTaskImpl(personLocalDataSourceMock, syncLocalDataSourceMock, personRemoteDataSourceSpy, TimeHelperImpl(), downSyncDao)
        sync.execute(subScope).test().awaitAndAssertSuccess()

        val peopleRequestUrl = mockServer.takeRequest().requestUrl
        assertPathUrlParam(peopleRequestUrl, PROJECT_ID)
        assertQueryUrlParam(peopleRequestUrl, "lastKnownPatientId", lastPatientIdFromRoom)
        assertQueryUrlParam(peopleRequestUrl, "lastKnownPatientUpdatedAt", "$lastPatientUpdateAtFromRoom")
    }

    @Test
    fun continueFromAPreviousSyncFromRealm_shouldSuccess() {
        val nPeopleToDownload = 1
        val scope = SyncScope(projectId = PROJECT_ID, userId = null, moduleIds = null)
        val lastPatientId = "lastPatientId"
        val lastPatientUpdateAt = Date()

        val personLocalDataSourceMock = mock<PersonLocalDataSource>()
        val syncLocalDataSourceMock = mock<SyncInfoLocalDataSource>()
        val subScope = scope.toSubSyncScopes().first()
        doReturnScopeFromBuilder(scope)
        mockDbDependencies(syncLocalDataSourceMock, personLocalDataSourceMock, DownSyncStatus(subScope, totalToDownload = nPeopleToDownload))

        whenever(syncLocalDataSourceMock) { load(subScope) } thenReturn
            DbSyncInfo(scope.group, getRandomPerson(lastPatientId, updateAt = lastPatientUpdateAt).toRealmPerson(), null)

        val argForInsertOrReplaceDownSyncStatus = argumentCaptor<DownSyncStatus>()
        whenever(downSyncDao) { insertOrReplaceDownSyncStatus(argForInsertOrReplaceDownSyncStatus.capture()) } thenDoNothing {}

        val peopleToDownload = prepareResponseForSubScope(subScope, nPeopleToDownload)
        mockServer.enqueue(mockSuccessfulResponseForDownloadPatients(peopleToDownload))

        val sync = DownSyncTaskImpl(personLocalDataSourceMock, syncLocalDataSourceMock, personRemoteDataSourceSpy, TimeHelperImpl(), downSyncDao)
        sync.execute(subScope).test().awaitAndAssertSuccess()

        val peopleRequestUrl = mockServer.takeRequest().requestUrl
        assertPathUrlParam(peopleRequestUrl, PROJECT_ID)
        assertQueryUrlParam(peopleRequestUrl, "lastKnownPatientId", lastPatientId)
        assertQueryUrlParam(peopleRequestUrl, "lastKnownPatientUpdatedAt", "${lastPatientUpdateAt.time}")

        val downSyncStatusAfterRealmMigration = argForInsertOrReplaceDownSyncStatus.firstValue
        Assert.assertEquals(downSyncStatusAfterRealmMigration.id, downSyncDao.getStatusId(subScope))
        Assert.assertEquals(downSyncStatusAfterRealmMigration.lastPatientUpdatedAt, lastPatientUpdateAt.time)
        Assert.assertEquals(downSyncStatusAfterRealmMigration.lastPatientId, lastPatientId)
        verifyBlockingAtLeast(1, syncLocalDataSourceMock) { delete(anyNotNull()) }
    }

    private fun runDownSyncAndVerifyConditions(
        nPeopleToDownload: Int,
        scope: SyncScope,
        lastPatientId: String? = null,
        lastPatientUpdateAt: Long? = null) {

        val personLocalDataSourceMock = mock<PersonLocalDataSource>()
        val syncLocalDataSourceMock = mock<SyncInfoLocalDataSource>()
        val batches = calculateCorrectNumberOfBatches(nPeopleToDownload)
        val subScope = scope.toSubSyncScopes().first()
        doReturnScopeFromBuilder(scope)
        val peopleToDownload = prepareResponseForSubScope(subScope, nPeopleToDownload)
        mockServer.enqueue(mockSuccessfulResponseForDownloadPatients(peopleToDownload))
        mockDbDependencies(
            syncLocalDataSourceMock,
            personLocalDataSourceMock,
            DownSyncStatus(subScope,
                totalToDownload = nPeopleToDownload,
                lastPatientId = lastPatientId,
                lastPatientUpdatedAt = lastPatientUpdateAt))

        val argForInsertOrUpdateInLocalDb = argumentCaptor<List<Person>>()
        wheneverOnSuspend(personLocalDataSourceMock) { insertOrUpdate(argForInsertOrUpdateInLocalDb.capture()) } thenOnBlockingReturn Unit

        val argForUpdateLastPatientIdInRoom = argumentCaptor<String>()
        whenever(downSyncDao) { updateLastPatientId(anyString(), argForUpdateLastPatientIdInRoom.capture()) } thenDoNothing {}

        val sync = DownSyncTaskImpl(personLocalDataSourceMock, syncLocalDataSourceMock, personRemoteDataSourceSpy, TimeHelperImpl(), downSyncDao)
        sync.execute(subScope).test().awaitAndAssertSuccess()

        verifyBlockingExactly(batches, personLocalDataSourceMock) { insertOrUpdate(anyNotNull()) }
        verifyExactly(batches, downSyncDao) { updateLastPatientId(anyString(), anyString()) }
        verifyExactly(batches + 1, downSyncDao) { updateLastSyncTime(anyString(), anyLong()) }
        verifyLastPatientSaveIsTheRightOne(argForInsertOrUpdateInLocalDb.allValues.last(), peopleToDownload)
    }

    private fun verifyLastPatientSaveIsTheRightOne(saved: List<Person>, inResponse: List<ApiGetPerson>) {
        Assert.assertEquals(saved.last().patientId, inResponse.last().id)
        Assert.assertEquals(saved.last().patientId, inResponse.last().id)
    }

    private fun doReturnScopeFromBuilder(scope: SyncScope) {
        whenever(syncScopeBuilderSpy) { buildSyncScope() } thenReturn scope
    }

    private fun prepareResponseForSubScope(subSyncScope: SubSyncScope, nPeople: Int) =
        getRandomPeople(nPeople, subSyncScope, listOf(false)).map { it.toApiGetPerson() }.sortedBy { it.updatedAt }

    private fun setupApi() {
        PeopleRemoteInterface.baseUrl = this.mockServer.url("/").toString()
        apiClient = SimApiClient(PeopleRemoteInterface::class.java, PeopleRemoteInterface.baseUrl)
        remotePeopleApi = apiClient.api
    }

    private fun mockDbDependencies(
        syncInfoLocalDataSourceMock: SyncInfoLocalDataSource,
        personLocalDataSourceMock: PersonLocalDataSource,
        downSyncStatus: DownSyncStatus? = null) {

        doNothingForInsertPeopleToLocalDb(personLocalDataSourceMock)
        doNothingForRealmMigration(syncInfoLocalDataSourceMock)
        doReturnDownSyncStatusFromRoom(downSyncStatus)
        doNothingForDownStatusInRoom()
    }

    private fun doReturnDownSyncStatusFromRoom(downSyncStatus: DownSyncStatus?) {
        downSyncStatus?.let {
            whenever(downSyncDao.getDownSyncStatusForId(anyString())).thenReturn(downSyncStatus)
        }
    }

    private fun doNothingForDownStatusInRoom() {
        whenever(downSyncDao) { updateLastSyncTime(anyString(), anyLong()) } thenDoNothing {}
        whenever(downSyncDao) { updatePeopleToDownSync(anyString(), anyInt()) } thenDoNothing {}
        whenever(downSyncDao) { updateLastPatientId(anyString(), anyString()) } thenDoNothing {}
    }


    private fun doNothingForRealmMigration(syncInfoLocalDataSourceMock: SyncInfoLocalDataSource) {
        wheneverOnSuspend(syncInfoLocalDataSourceMock) { load(anyNotNull()) } thenOnBlockingThrow NoSuchDbSyncInfoException::class.java
    }

    private fun doNothingForInsertPeopleToLocalDb(personLocalDataSourceMock: PersonLocalDataSource) {
        wheneverOnSuspend(personLocalDataSourceMock) { insertOrUpdate(anyNotNull()) } thenOnBlockingReturn Unit
    }

    private fun mockSuccessfulResponseForDownloadPatients(patients: List<ApiGetPerson>): MockResponse? {
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

    private fun calculateCorrectNumberOfBatches(nPeopleToDownload: Int) =
        ceil(nPeopleToDownload.toDouble() / DownSyncTaskImpl.BATCH_SIZE_FOR_DOWNLOADING.toDouble()).toInt()


    @After
    @Throws
    fun tearDown() {
        mockServer.shutdown()
    }

    companion object {
        private const val PROJECT_ID = "projectIDTest"
        private const val MODULE_ID = "moduleId"
        private const val USER_ID = "userId"
    }
}
