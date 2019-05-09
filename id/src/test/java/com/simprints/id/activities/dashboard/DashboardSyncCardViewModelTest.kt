package com.simprints.id.activities.dashboard

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.simprints.id.activities.dashboard.viewModels.DashboardCardType
import com.simprints.id.activities.dashboard.viewModels.syncCard.DashboardSyncCardViewModel
import com.simprints.id.activities.dashboard.viewModels.syncCard.SyncCardState
import com.simprints.testtools.common.di.DependencyRule.MockRule
import com.simprints.testtools.common.di.DependencyRule.SpyRule
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.commontesttools.di.TestPreferencesModule
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.room.DownSyncStatus
import com.simprints.id.data.db.local.room.SyncStatusDatabase
import com.simprints.id.data.db.local.room.UpSyncStatus
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.project.RemoteProjectManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.PreferencesManagerImpl
import com.simprints.id.domain.PeopleCount
import com.simprints.id.domain.modality.Modes
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilder
import com.simprints.id.services.scheduledSync.peopleDownSync.models.PeopleDownSyncTrigger
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncState
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.id.testtools.state.RobolectricTestMocker
import com.simprints.id.tools.TimeHelper
import com.simprints.testtools.common.livedata.testObserver
import com.simprints.testtools.common.syntax.*
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import com.simprints.testtools.unit.robolectric.getSharedPreferences
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class DashboardSyncCardViewModelTest {

    private val app = ApplicationProvider.getApplicationContext() as TestApplication

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @Inject lateinit var remoteDbManagerMock: RemoteDbManager
    @Inject lateinit var remoteProjectManagerMock: RemoteProjectManager
    @Inject lateinit var localDbManagerMock: LocalDbManager
    @Inject lateinit var preferencesManagerSpy: PreferencesManager
    @Inject lateinit var dbManagerMock: DbManager
    @Inject lateinit var syncStatusDatabase: SyncStatusDatabase
    @Inject lateinit var syncScopeBuilder: SyncScopesBuilder
    @Inject lateinit var timeHelper: TimeHelper

    private val downSyncDao by lazy { syncStatusDatabase.downSyncDao.getDownSyncStatusLiveData() }
    private val upSyncDao by lazy { syncStatusDatabase.upSyncDao.getUpSyncStatus() }
    private val syncScope by lazy { syncScopeBuilder.buildSyncScope()!! }
    private val subSyncScopes by lazy { syncScope.toSubSyncScopes() }
    private lateinit var dashboardCardViewModel: DashboardSyncCardViewModel
    private val fakeSyncStateLiveData = MutableLiveData<SyncState>()

    private val preferencesModule by lazy {
        TestPreferencesModule(
            settingsPreferencesManagerRule = SpyRule
        )
    }

    private val module by lazy {
        TestAppModule(app,
            dbManagerRule = MockRule,
            remoteDbManagerRule = MockRule,
            remoteProjectManagerRule = MockRule,
            localDbManagerRule = MockRule,
            downSyncManagerRule = MockRule)
    }

    @Before
    fun setUp() {
        UnitTestConfig(this, module, preferencesModule).fullSetup()

        val sharedPref = getSharedPreferences(PreferencesManagerImpl.PREF_FILE_NAME)

        RobolectricTestMocker
            .initLogInStateMock(sharedPref, remoteDbManagerMock)
            .setUserLogInState(true, sharedPref)
            .mockLoadProject(localDbManagerMock, remoteProjectManagerMock)

        whenever(preferencesManagerSpy.peopleDownSyncTriggers).thenReturn(mapOf(PeopleDownSyncTrigger.MANUAL to true))
    }

    @Test
    fun downSyncIsNotRunning_shouldFetchDownSyncCounterFromRetrofit() {
        insertASyncWorkInfoEvent(SyncState.NOT_RUNNING)
        mockCounters(getMockListOfPeopleCountWithCounter(1), 2,
            getMockListOfPeopleCountWithCounter(3))

        dashboardCardViewModel = createViewModelDashboardToTest()
        val vm = dashboardCardViewModel.viewModelStateLiveData.testObserver()
        val lastState = vm.observedValues.last()

        Truth.assert_().that(lastState?.peopleInDb).isEqualTo(1)
        Truth.assert_().that(lastState?.peopleToUpload).isEqualTo(2)
        Truth.assert_().that(lastState?.peopleToDownload).isEqualTo(3)
        verifyOnce(dbManagerMock) { getPeopleCountToDownSync(anyNotNull()) }
    }

    @Test
    fun downSyncIsNotRunning_shouldEnableTheSyncButton() {
        insertASyncWorkInfoEvent(SyncState.NOT_RUNNING)
        mockCounters(getMockListOfPeopleCountWithCounter(1), 2,
            getMockListOfPeopleCountWithCounter(3))

        dashboardCardViewModel = createViewModelDashboardToTest()
        val vm = dashboardCardViewModel.viewModelStateLiveData.testObserver()
        val lastState = vm.observedValues.last()

        Truth.assert_().that(lastState?.syncCardState).isEqualTo(SyncCardState.SYNC_ENABLED)
    }

    @Test
    fun downSyncIsRunning_shouldFetchDownSyncCounterFromWorkers() {
        insertASyncWorkInfoEvent(SyncState.RUNNING)
        mockCounters(getMockListOfPeopleCountWithCounter(1), 2)
        insertADownSyncStatusInDb(DownSyncStatus(subSyncScopes.first(), totalToDownload = 100))

        dashboardCardViewModel = createViewModelDashboardToTest()
        val vm = dashboardCardViewModel.viewModelStateLiveData.testObserver()
        val lastState = vm.observedValues.last()

        Truth.assert_().that(lastState?.peopleToDownload).isEqualTo(100)
        verifyCalculateNPatientsToDownSyncWasCalled(0)
    }

    @Test
    fun downSyncIsRunning_shouldShowARunningStateForSyncButton() {
        mockCounters(getMockListOfPeopleCountWithCounter(1), 2)
        insertASyncWorkInfoEvent(SyncState.RUNNING)
        insertADownSyncStatusInDb(DownSyncStatus(subSyncScopes.first(), totalToDownload = 100))

        dashboardCardViewModel = createViewModelDashboardToTest()
        val vm = dashboardCardViewModel.viewModelStateLiveData.testObserver()
        val lastState = vm.observedValues.last()

        Truth.assert_().that(lastState?.syncCardState).isEqualTo(SyncCardState.SYNC_RUNNING)
    }


    @Test
    fun upSyncIsTheLatestSync_shouldShowUpSyncTimeAsLatestTimestamp() {
        mockCounters(getMockListOfPeopleCountWithCounter(1), 2)
        insertASyncWorkInfoEvent(SyncState.RUNNING)
        val latestSyncTime = timeHelper.now()
        insertADownSyncStatusInDb(DownSyncStatus(subSyncScopes.first(), lastSyncTime = timeHelper.nowMinus(2, TimeUnit.MINUTES)))
        insertAnUpSyncStatusInDb(UpSyncStatus(lastUpSyncTime = latestSyncTime))
        insertADownSyncStatusInDb(DownSyncStatus(subSyncScopes.first(), lastSyncTime = timeHelper.nowMinus(1, TimeUnit.MINUTES)))

        dashboardCardViewModel = createViewModelDashboardToTest()
        val vm = dashboardCardViewModel.viewModelStateLiveData.testObserver()
        val lastState = vm.observedValues.last()

        Truth.assert_().that(lastState?.lastSyncTime).isEqualTo(dashboardCardViewModel.helper?.dateFormat?.format(latestSyncTime))
    }

    @Test
    fun downSyncIsTheLatestSync_shouldShowUpSyncTimeAsLatestTimestamp() {
        mockCounters(getMockListOfPeopleCountWithCounter(1), 2)
        insertASyncWorkInfoEvent(SyncState.RUNNING)
        val latestSyncTime = timeHelper.now()
        insertADownSyncStatusInDb(DownSyncStatus(subSyncScopes.first(), lastSyncTime = timeHelper.nowMinus(2, TimeUnit.MINUTES)))
        insertADownSyncStatusInDb(DownSyncStatus(subSyncScopes.first(), lastSyncTime = latestSyncTime))
        insertAnUpSyncStatusInDb(UpSyncStatus(lastUpSyncTime = timeHelper.nowMinus(1, TimeUnit.MINUTES)))

        dashboardCardViewModel = createViewModelDashboardToTest()
        val vm = dashboardCardViewModel.viewModelStateLiveData.testObserver()
        val lastState = vm.observedValues.last()

        Truth.assert_().that(lastState?.lastSyncTime).isEqualTo(dashboardCardViewModel.helper?.dateFormat?.format(latestSyncTime))
    }

    @Test
    fun downSyncFinishes_shouldUpdateTheTotalCounter() {
        val requiredCallsToInitTotalCounter = 1
        val requiredCallToInitAndUpdateUpSyncCounter = 2
        mockCounters(getMockListOfPeopleCountWithCounter(1), 2)
        insertASyncWorkInfoEvent(SyncState.RUNNING)

        dashboardCardViewModel = createViewModelDashboardToTest()
        val vm = dashboardCardViewModel.viewModelStateLiveData.testObserver()
        insertASyncWorkInfoEvent(SyncState.NOT_RUNNING)

        vm.observedValues.last()

        verifyGetPeopleCountFromLocalWasCalled(requiredCallsToInitTotalCounter)
        verifyGetPeopleCountFromLocalForSyncScopeWasCalled(requiredCallToInitAndUpdateUpSyncCounter)
        verifyCalculateNPatientsToDownSyncWasCalled(0)
    }

    @Test
    fun whileDownSyncProgresses_shouldNotUpdateTotalAndLocalCounters() {
        val requiredCallsToInitTotalCounter = 1
        val requiredCallToInitUpSyncCounter = 1
        mockCounters(getMockListOfPeopleCountWithCounter(1), 2)
        insertASyncWorkInfoEvent(SyncState.RUNNING)

        dashboardCardViewModel = createViewModelDashboardToTest()
        val vm = dashboardCardViewModel.viewModelStateLiveData.testObserver()

        insertADownSyncStatusInDb(DownSyncStatus(subSyncScopes.first(), totalToDownload = 100))
        insertADownSyncStatusInDb(DownSyncStatus(subSyncScopes.first(), totalToDownload = 50))

        val lastState = vm.observedValues.last()

        verifyGetPeopleCountFromLocalWasCalled(requiredCallsToInitTotalCounter)
        verifyGetPeopleCountFromLocalForSyncScopeWasCalled(requiredCallToInitUpSyncCounter)
        Truth.assert_().that(lastState?.peopleToDownload).isEqualTo(50)
    }

    @Test
    fun manualTriggerIsOff_shouldNotShowTheSyncButton() {
        mockCounters(getMockListOfPeopleCountWithCounter(1), 2,
            getMockListOfPeopleCountWithCounter(3))
        whenever(preferencesManagerSpy.peopleDownSyncTriggers).thenReturn(mapOf(PeopleDownSyncTrigger.MANUAL to false))
        insertASyncWorkInfoEvent(SyncState.NOT_RUNNING)
        dashboardCardViewModel = createViewModelDashboardToTest()
        val vm = dashboardCardViewModel.viewModelStateLiveData.testObserver()

        val lastState = vm.observedValues.last()

        Truth.assert_().that(lastState?.syncCardState).isEqualTo(SyncCardState.SYNC_DISABLED)
    }

    @Test
    fun manualTriggerIsOn_shouldShowTheSyncButton() {
        mockCounters(getMockListOfPeopleCountWithCounter(1), 2,
            getMockListOfPeopleCountWithCounter(3))
        whenever(preferencesManagerSpy.peopleDownSyncTriggers).thenReturn(mapOf(PeopleDownSyncTrigger.MANUAL to true))
        insertASyncWorkInfoEvent(SyncState.RUNNING)
        dashboardCardViewModel = createViewModelDashboardToTest()
        val vm = dashboardCardViewModel.viewModelStateLiveData.testObserver()

        val lastState = vm.observedValues.last()

        Truth.assert_().that(lastState?.syncCardState).isEqualTo(SyncCardState.SYNC_RUNNING)
    }

    private fun mockCounters(peopleInDb: List<PeopleCount>? = null, peopleToUpload: Int? = null, peopleToDownload: List<PeopleCount>? = null) {
        peopleToUpload?.let {
            whenever(localDbManagerMock.getPeopleCountFromLocal(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())).thenReturn(Single.just(peopleToUpload))
        }

        peopleInDb?.let {
            whenever(dbManagerMock.getPeopleCountFromLocalForSyncScope(anyNotNull())).thenReturn(Single.just(peopleInDb))
        }

        peopleToDownload?.let {
            whenever(dbManagerMock.getPeopleCountToDownSync(anyNotNull())).thenReturn(Single.just(peopleToDownload))
        }
    }

    private fun getMockListOfPeopleCountWithCounter(counter: Int) =
        listOf(PeopleCount("projectId", "userId", "0", listOf(Modes.FACE, Modes.FINGERPRINT), counter))

    private fun createViewModelDashboardToTest() =
        DashboardSyncCardViewModel(
            DashboardCardType.SYNC_DB,
            1,
            app.component,
            downSyncDao,
            upSyncDao,
            fakeSyncStateLiveData)

    private fun insertASyncWorkInfoEvent(vararg states: SyncState) {
        states.forEach {
            fakeSyncStateLiveData.postValue(it)
        }
    }

    private fun insertADownSyncStatusInDb(downSyncStatus: DownSyncStatus) {
        syncStatusDatabase.downSyncDao.insertOrReplaceDownSyncStatus(downSyncStatus)
    }

    private fun insertAnUpSyncStatusInDb(upSyncStatus: UpSyncStatus) {
        syncStatusDatabase.upSyncDao.insertLastUpSyncTime(upSyncStatus)
    }

    private fun verifyGetPeopleCountFromLocalWasCalled(requiredCallsToInitTotalCounter: Int) {
        verifyExactly(requiredCallsToInitTotalCounter, localDbManagerMock) { getPeopleCountFromLocal(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull()) }
    }

    private fun verifyCalculateNPatientsToDownSyncWasCalled(times: Int) {
        verifyExactly(times, dbManagerMock) { getPeopleCountToDownSync(anyNotNull()) }

    }

    private fun verifyGetPeopleCountFromLocalForSyncScopeWasCalled(requiredCallToInitAndUpdateUpSyncCounter: Int) {
        verifyExactly(requiredCallToInitAndUpdateUpSyncCounter, dbManagerMock) { getPeopleCountFromLocalForSyncScope(anyNotNull()) }
    }
}
