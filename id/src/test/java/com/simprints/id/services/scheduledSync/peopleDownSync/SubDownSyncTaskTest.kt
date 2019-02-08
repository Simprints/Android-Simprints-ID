package com.simprints.id.services.scheduledSync.peopleDownSync

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockito_kotlin.*
import com.simprints.id.activities.ShadowAndroidXMultiDex
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.realm.models.rl_SyncInfo
import com.simprints.id.data.db.local.realm.models.toRealmPerson
import com.simprints.id.data.db.local.room.DownSyncDao
import com.simprints.id.data.db.local.room.DownSyncStatus
import com.simprints.id.data.db.local.room.getStatusId
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.db.remote.models.toFirebasePerson
import com.simprints.id.data.db.remote.network.PeopleRemoteInterface
import com.simprints.id.data.db.remote.people.RemotePeopleManager
import com.simprints.id.testUtils.di.AppModuleForTests
import com.simprints.id.testUtils.di.DaggerForUnitTests
import com.simprints.id.domain.Person
import com.simprints.id.exceptions.safe.data.db.NoSuchRlSessionInfoException
import com.simprints.id.network.SimApiClient
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilder
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.tasks.DownSyncTaskImpl
import com.simprints.id.shared.DependencyRule
import com.simprints.id.shared.PeopleGeneratorUtils.getRandomPeople
import com.simprints.id.shared.PeopleGeneratorUtils.getRandomPerson
import com.simprints.testframework.common.syntax.anyNotNull
import com.simprints.id.shared.testTools.extensions.awaitAndAssertSuccess
import com.simprints.testframework.common.syntax.whenever
import com.simprints.testframework.unit.reactive.RxJavaTest
import com.simprints.testframework.unit.mockserver.assertPathUrlParam
import com.simprints.testframework.unit.mockserver.assertQueryUrlParam
import com.simprints.id.testUtils.roboletric.TestApplication
import com.simprints.id.tools.TimeHelperImpl
import com.simprints.id.tools.delegates.lazyVar
import com.simprints.id.tools.json.JsonHelper
import com.simprints.id.testUtils.roboletric.RobolectricDaggerTestConfig
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
import java.util.*
import javax.inject.Inject
import kotlin.math.ceil


@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class SubDownSyncTaskTest : DaggerForUnitTests(), RxJavaTest {

    private var mockServer = MockWebServer()
    private lateinit var apiClient: SimApiClient<PeopleRemoteInterface>
    private lateinit var remotePeopleApi: PeopleRemoteInterface

    @Inject lateinit var syncScopeBuilderSpy: SyncScopesBuilder

    private val remoteDbManagerSpy: RemoteDbManager = spy()
    private val remotePeopleManagerSpy: RemotePeopleManager = spy()
    private val downSyncDao: DownSyncDao = mock()

    override var module: AppModuleForTests by lazyVar {
        AppModuleForTests(app,
            syncScopesBuilderRule = DependencyRule.MockRule,
            syncStatusDatabaseRule = DependencyRule.SpyRule)
    }

    @Before
    fun setUp() {
        RobolectricDaggerTestConfig(this).setupAllAndFinish()

        whenever(remoteDbManagerSpy.getCurrentFirestoreToken()).thenReturn(Single.just(""))
        mockServer.start()
        setupApi()
        whenever(remotePeopleManagerSpy.getPeopleApiClient()).thenReturn(Single.just(remotePeopleApi))
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
        val localDbMock = Mockito.mock(LocalDbManager::class.java)
        val nPeopleToDownload = 499
        val scope = SyncScope(projectId = "projectIDTest", userId = null, moduleIds = null)
        val subScope = scope.toSubSyncScopes().first()
        doReturnScopeFromBuilder(scope)
        val peopleToDownload = prepareResponseForSubScope(subScope, nPeopleToDownload)
        mockServer.enqueue(mockSuccessfulResponseWithIncorrectModels(peopleToDownload))

        mockDbDependencies(localDbMock, DownSyncStatus(subScope, totalToDownload = nPeopleToDownload))

        val sync = DownSyncTaskImpl(localDbMock, remotePeopleManagerSpy, TimeHelperImpl(), downSyncDao)
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

        val localDbMock = Mockito.mock(LocalDbManager::class.java)
        val subScope = scope.toSubSyncScopes().first()
        doReturnScopeFromBuilder(scope)

        val peopleToDownload = prepareResponseForSubScope(subScope, nPeopleToDownload)
        mockServer.enqueue(mockSuccessfulResponseForDownloadPatients(peopleToDownload))

        mockDbDependencies(localDbMock, DownSyncStatus(
            subScope, lastPatientId = lastPatientIdFromRoom,
            lastPatientUpdatedAt = lastPatientUpdateAtFromRoom,
            totalToDownload = nPeopleToDownload))

        val sync = DownSyncTaskImpl(localDbMock, remotePeopleManagerSpy, TimeHelperImpl(), downSyncDao)
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

        val localDbMock = Mockito.mock(LocalDbManager::class.java)
        val subScope = scope.toSubSyncScopes().first()
        doReturnScopeFromBuilder(scope)
        mockDbDependencies(localDbMock, DownSyncStatus(subScope, totalToDownload = nPeopleToDownload))

        whenever(localDbMock.getRlSyncInfo(subScope)).thenReturn(Single.just(
            rl_SyncInfo(scope.group, getRandomPerson(lastPatientId, updateAt = lastPatientUpdateAt).toRealmPerson(), null)))

        val argForInsertOrReplaceDownSyncStatus = argumentCaptor<DownSyncStatus>()
        doNothing().whenever(downSyncDao).insertOrReplaceDownSyncStatus(argForInsertOrReplaceDownSyncStatus.capture())

        val peopleToDownload = prepareResponseForSubScope(subScope, nPeopleToDownload)
        mockServer.enqueue(mockSuccessfulResponseForDownloadPatients(peopleToDownload))

        val sync = DownSyncTaskImpl(localDbMock, remotePeopleManagerSpy, TimeHelperImpl(), downSyncDao)
        sync.execute(subScope).test().awaitAndAssertSuccess()

        val peopleRequestUrl = mockServer.takeRequest().requestUrl
        assertPathUrlParam(peopleRequestUrl, PROJECT_ID)
        assertQueryUrlParam(peopleRequestUrl, "lastKnownPatientId", lastPatientId)
        assertQueryUrlParam(peopleRequestUrl, "lastKnownPatientUpdatedAt", "${lastPatientUpdateAt.time}")

        val downSyncStatusAfterRealmMigration = argForInsertOrReplaceDownSyncStatus.firstValue
        Assert.assertEquals(downSyncStatusAfterRealmMigration.id, downSyncDao.getStatusId(subScope))
        Assert.assertEquals(downSyncStatusAfterRealmMigration.lastPatientUpdatedAt, lastPatientUpdateAt.time)
        Assert.assertEquals(downSyncStatusAfterRealmMigration.lastPatientId, lastPatientId)
        verify(localDbMock, atLeast(1)).deleteSyncInfo(anyNotNull())
    }

    private fun runDownSyncAndVerifyConditions(
        nPeopleToDownload: Int,
        scope: SyncScope,
        lastPatientId: String? = null,
        lastPatientUpdateAt: Long? = null) {

        val localDbMock = Mockito.mock(LocalDbManager::class.java)
        val batches = calculateCorrectNumberOfBatches(nPeopleToDownload)
        val subScope = scope.toSubSyncScopes().first()
        doReturnScopeFromBuilder(scope)
        val peopleToDownload = prepareResponseForSubScope(subScope, nPeopleToDownload)
        mockServer.enqueue(mockSuccessfulResponseForDownloadPatients(peopleToDownload))
        mockDbDependencies(localDbMock, DownSyncStatus(subScope,
            totalToDownload = nPeopleToDownload,
            lastPatientId = lastPatientId,
            lastPatientUpdatedAt = lastPatientUpdateAt))

        val argForInsertOrUpdateInLocalDb = argumentCaptor<List<Person>>()
        whenever(localDbMock.insertOrUpdatePeopleInLocal(argForInsertOrUpdateInLocalDb.capture())).doReturn(Completable.complete())

        val argForUpdateLastPatientIdInRoom = argumentCaptor<String>()
        doNothing().whenever(downSyncDao).updateLastPatientId(anyString(), argForUpdateLastPatientIdInRoom.capture())

        val sync = DownSyncTaskImpl(localDbMock, remotePeopleManagerSpy, TimeHelperImpl(), downSyncDao)
        sync.execute(subScope).test().awaitAndAssertSuccess()

        verify(localDbMock, times(batches)).insertOrUpdatePeopleInLocal(anyNotNull())
        verify(downSyncDao, times(batches)).updateLastPatientId(anyString(), anyString())
        verify(downSyncDao, times(batches + 1)).updateLastSyncTime(anyString(), anyLong())
        verifyLastPatientSaveIsTheRightOne(argForInsertOrUpdateInLocalDb.allValues.last(), peopleToDownload)
    }

    private fun verifyLastPatientSaveIsTheRightOne(saved: List<Person>, inResponse: List<fb_Person>) {
        Assert.assertEquals(saved.last().patientId, inResponse.last().patientId)
        Assert.assertEquals(saved.last().patientId, inResponse.last().patientId)
    }

    private fun doReturnScopeFromBuilder(scope: SyncScope) {
        doReturn(scope).`when`(syncScopeBuilderSpy).buildSyncScope()
    }

    private fun prepareResponseForSubScope(subSyncScope: SubSyncScope, nPeople: Int) =
        getRandomPeople(nPeople, subSyncScope, listOf(false)).map { it.toFirebasePerson() }.sortedBy { it.updatedAt }

    private fun setupApi() {
        PeopleRemoteInterface.baseUrl = this.mockServer.url("/").toString()
        apiClient = SimApiClient(PeopleRemoteInterface::class.java, PeopleRemoteInterface.baseUrl)
        remotePeopleApi = apiClient.api
    }

    private fun mockDbDependencies(
        localDbMock: LocalDbManager,
        downSyncStatus: DownSyncStatus? = null) {

        doNothingForInsertPeopleToLocalDb(localDbMock)
        doNothingForRealmMigration(localDbMock)
        doReturnDownSyncStatusFromRoom(downSyncStatus)
        doNothingForDownStatusInRoom()
    }

    private fun doReturnDownSyncStatusFromRoom(downSyncStatus: DownSyncStatus?) {
        downSyncStatus?.let {
            whenever(downSyncDao.getDownSyncStatusForId(anyString())).doReturn(downSyncStatus)
        }
    }

    private fun doNothingForDownStatusInRoom() {
        doNothing().whenever(downSyncDao).updateLastSyncTime(anyString(), anyLong())
        doNothing().whenever(downSyncDao).updatePeopleToDownSync(anyString(), anyInt())
        doNothing().whenever(downSyncDao).updateLastPatientId(anyString(), anyString())
    }


    private fun doNothingForRealmMigration(localDbMock: LocalDbManager) {
        whenever(localDbMock.getRlSyncInfo(anyNotNull())).thenReturn(Single.error(NoSuchRlSessionInfoException("no RlInfo")))
    }

    private fun doNothingForInsertPeopleToLocalDb(localDbMock: LocalDbManager) {
        whenever(localDbMock.insertOrUpdatePeopleInLocal(anyNotNull())).doReturn(Completable.complete())
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

    companion object {
        private const val PROJECT_ID = "projectIDTest"
        private const val MODULE_ID = "moduleId"
        private const val USER_ID = "userId"
    }
}
