package com.simprints.id.activities.dashboard

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.google.firebase.FirebaseApp
import com.nhaarman.mockito_kotlin.anyOrNull
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.simprints.id.activities.ShadowAndroidXMultiDex
import com.simprints.id.activities.dashboard.viewModels.DashboardCardType
import com.simprints.id.activities.dashboard.viewModels.syncCard.DashboardSyncCardViewModel
import com.simprints.id.activities.dashboard.viewModels.syncCard.SyncCardState
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.room.DownSyncStatus
import com.simprints.id.data.db.local.room.UpSyncStatus
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppModuleForTests
import com.simprints.id.di.DaggerForTests
import com.simprints.id.services.scheduledSync.peopleDownSync.SyncStatusDatabase
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.DownSyncManager
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilder
import com.simprints.id.services.scheduledSync.peopleDownSync.models.PeopleDownSyncTrigger
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncState
import com.simprints.id.shared.DependencyRule.MockRule
import com.simprints.id.shared.DependencyRule.SpyRule
import com.simprints.id.shared.PreferencesModuleForAnyTests
import com.simprints.id.shared.anyNotNull
import com.simprints.id.shared.liveData.testObserver
import com.simprints.id.shared.whenever
import com.simprints.id.testUtils.base.RxJavaTest
import com.simprints.id.testUtils.roboletric.*
import com.simprints.id.testUtils.workManager.initWorkManagerIfRequired
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.delegates.lazyVar
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
class DashboardSyncCardViewModelTest : RxJavaTest, DaggerForTests() {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @Inject lateinit var remoteDbManagerMock: RemoteDbManager
    @Inject lateinit var localDbManagerMock: LocalDbManager
    @Inject lateinit var preferencesManagerSpy: PreferencesManager
    @Inject lateinit var dbManagerMock: DbManager
    @Inject lateinit var syncStatusDatabase: SyncStatusDatabase
    @Inject lateinit var syncSCopeBuilder: SyncScopesBuilder
    @Inject lateinit var timeHelper: TimeHelper
    @Inject lateinit var downSyncManagerMock: DownSyncManager

    private val downSyncDao by lazy { syncStatusDatabase.downSyncDao.getDownSyncStatusLiveData() }
    private val upSyncDao by lazy { syncStatusDatabase.upSyncDao.getUpSyncStatus() }
    private val syncScope by lazy { syncSCopeBuilder.buildSyncScope()!! }
    private val subSyncScopes by lazy { syncScope.toSubSyncScopes() }
    private lateinit var dashboardCardViewModel: DashboardSyncCardViewModel
    private val fakeSyncStateLiveData = MutableLiveData<SyncState>()

    override var preferencesModule by lazyVar {
        PreferencesModuleForAnyTests(
            settingsPreferencesManagerRule = SpyRule
        )
    }

    override var module by lazyVar {
        AppModuleForTests(app,
            dbManagerRule = MockRule,
            remoteDbManagerRule = MockRule,
            localDbManagerRule = MockRule,
            downSyncManagerRule = MockRule)
    }


    @Before
    override fun setUp() {
        app = (ApplicationProvider.getApplicationContext() as TestApplication)
        FirebaseApp.initializeApp(app)
        super.setUp()
        testAppComponent.inject(this)
        initWorkManagerIfRequired(app)

        initLogInStateMock(getRoboSharedPreferences(), remoteDbManagerMock)
        setUserLogInState(true, getRoboSharedPreferences())

        mockLoadProject(localDbManagerMock, remoteDbManagerMock)

        whenever(preferencesManagerSpy.peopleDownSyncTriggers).thenReturn(mapOf(PeopleDownSyncTrigger.MANUAL to true))
        whenever(downSyncManagerMock.onSyncStateUpdated()).thenReturn(fakeSyncStateLiveData)
    }

    @Test
    fun downSyncIsNotRunning_shouldFetchDownSyncCounterFromRetrofit() {
        insertASyncWorkInfoEvent(SyncState.NOT_RUNNING)
        mockCounters(1, 2, 3)

        dashboardCardViewModel = createViewModelDashboardToTest()
        val vm = dashboardCardViewModel.stateLiveData.testObserver()
        val lastState = vm.observedValues.last()

        Truth.assert_().that(lastState?.peopleInDb).isEqualTo(1)
        Truth.assert_().that(lastState?.peopleToUpload).isEqualTo(2)
        Truth.assert_().that(lastState?.peopleToDownload).isEqualTo(3)
        verify(dbManagerMock, times(1)).calculateNPatientsToDownSync(anyNotNull(), anyNotNull(), anyNotNull())
    }

    @Test
    fun downSyncIsNotRunning_shouldEnableTheSyncButton() {
        insertASyncWorkInfoEvent(SyncState.NOT_RUNNING)
        mockCounters(1, 2, 3)

        dashboardCardViewModel = createViewModelDashboardToTest()
        val vm = dashboardCardViewModel.stateLiveData.testObserver()
        val lastState = vm.observedValues.last()

        Truth.assert_().that(lastState?.syncCardState).isEqualTo(SyncCardState.SYNC_ENABLED)
    }

    @Test
    fun downSyncIsRunning_shouldFetchDownSyncCounterFromWorkers() {
        insertASyncWorkInfoEvent(SyncState.RUNNING)
        mockCounters(1, 2)
        insertADownSyncStatusInDb(DownSyncStatus(subSyncScopes.first(), totalToDownload = 100))

        dashboardCardViewModel = createViewModelDashboardToTest()
        val vm = dashboardCardViewModel.stateLiveData.testObserver()
        val lastState = vm.observedValues.last()

        Truth.assert_().that(lastState?.peopleToDownload).isEqualTo(100)
        verifyCalculateNPatientsToDownSyncWasCalled(0)
    }

    @Test
    fun downSyncIsRunning_shouldShowARunningStateForSyncButton() {
        mockCounters(1, 2)
        insertASyncWorkInfoEvent(SyncState.RUNNING)
        insertADownSyncStatusInDb(DownSyncStatus(subSyncScopes.first(), totalToDownload = 100))

        dashboardCardViewModel = createViewModelDashboardToTest()
        val vm = dashboardCardViewModel.stateLiveData.testObserver()
        val lastState = vm.observedValues.last()

        Truth.assert_().that(lastState?.syncCardState).isEqualTo(SyncCardState.SYNC_RUNNING)
    }


    @Test
    fun upSyncIsTheLatestSync_shouldShowUpSyncTimeAsLatestTimestamp() {
        mockCounters(1, 2)
        insertASyncWorkInfoEvent(SyncState.RUNNING)
        val latestSyncTime = timeHelper.now()
        insertADownSyncStatusInDb(DownSyncStatus(subSyncScopes.first(), lastSyncTime = timeHelper.nowMinus(2, TimeUnit.MINUTES)))
        insertAnUpSyncStatusInDb(UpSyncStatus(lastUpSyncTime = latestSyncTime))
        insertADownSyncStatusInDb(DownSyncStatus(subSyncScopes.first(), lastSyncTime = timeHelper.nowMinus(1, TimeUnit.MINUTES)))

        dashboardCardViewModel = createViewModelDashboardToTest()
        val vm = dashboardCardViewModel.stateLiveData.testObserver()
        val lastState = vm.observedValues.last()

        Truth.assert_().that(lastState?.lastSyncTime).isEqualTo(dashboardCardViewModel.helper?.dateFormat?.format(latestSyncTime))
    }

    @Test
    fun downSyncIsTheLatestSync_shouldShowUpSyncTimeAsLatestTimestamp() {
        mockCounters(1, 2)
        insertASyncWorkInfoEvent(SyncState.RUNNING)
        val latestSyncTime = timeHelper.now()
        insertADownSyncStatusInDb(DownSyncStatus(subSyncScopes.first(), lastSyncTime = timeHelper.nowMinus(2, TimeUnit.MINUTES)))
        insertADownSyncStatusInDb(DownSyncStatus(subSyncScopes.first(), lastSyncTime = latestSyncTime))
        insertAnUpSyncStatusInDb(UpSyncStatus(lastUpSyncTime = timeHelper.nowMinus(1, TimeUnit.MINUTES)))

        dashboardCardViewModel = createViewModelDashboardToTest()
        val vm = dashboardCardViewModel.stateLiveData.testObserver()
        val lastState = vm.observedValues.last()

        Truth.assert_().that(lastState?.lastSyncTime).isEqualTo(dashboardCardViewModel.helper?.dateFormat?.format(latestSyncTime))
    }

    @Test
    fun downSyncFinishes_shouldUpdateTheTotalCounter() {
        val requiredCallsToInitTotalCounter = 1
        val requiredCallToInitAndUpdateUpSyncCounter = 2
        mockCounters(1, 2)
        insertASyncWorkInfoEvent(SyncState.RUNNING)

        dashboardCardViewModel = createViewModelDashboardToTest()
        val vm = dashboardCardViewModel.stateLiveData.testObserver()
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
        mockCounters(1, 2)
        insertASyncWorkInfoEvent(SyncState.RUNNING)

        dashboardCardViewModel = createViewModelDashboardToTest()
        val vm = dashboardCardViewModel.stateLiveData.testObserver()

        insertADownSyncStatusInDb(DownSyncStatus(subSyncScopes.first(), totalToDownload = 100))
        insertADownSyncStatusInDb(DownSyncStatus(subSyncScopes.first(), totalToDownload = 50))

        val lastState = vm.observedValues.last()

        verifyGetPeopleCountFromLocalWasCalled(requiredCallsToInitTotalCounter)
        verifyGetPeopleCountFromLocalForSyncScopeWasCalled(requiredCallToInitUpSyncCounter)
        Truth.assert_().that(lastState?.peopleToDownload).isEqualTo(50)
    }

    @Test
    fun manualTriggerIsOff_shouldNotShowTheSyncButton() {
        mockCounters(1, 2, 3)
        whenever(preferencesManagerSpy.peopleDownSyncTriggers).thenReturn(mapOf(PeopleDownSyncTrigger.MANUAL to false))
        insertASyncWorkInfoEvent(SyncState.NOT_RUNNING)
        dashboardCardViewModel = createViewModelDashboardToTest()
        val vm = dashboardCardViewModel.stateLiveData.testObserver()

        val lastState = vm.observedValues.last()

        Truth.assert_().that(lastState?.syncCardState).isEqualTo(SyncCardState.SYNC_DISABLED)
    }

    @Test
    fun manualTriggerIsOn_shouldShowTheSyncButton() {
        mockCounters(1, 2, 3)
        whenever(preferencesManagerSpy.peopleDownSyncTriggers).thenReturn(mapOf(PeopleDownSyncTrigger.MANUAL to true))
        insertASyncWorkInfoEvent(SyncState.RUNNING)
        dashboardCardViewModel = createViewModelDashboardToTest()
        val vm = dashboardCardViewModel.stateLiveData.testObserver()

        val lastState = vm.observedValues.last()

        Truth.assert_().that(lastState?.syncCardState).isEqualTo(SyncCardState.SYNC_RUNNING)
    }

    private fun mockCounters(peopleInDb: Int? = null, peopleToUpload: Int? = null, peopleToDownload: Int? = null) {
        peopleInDb?.let {
            whenever(localDbManagerMock.getPeopleCountFromLocal(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())).thenReturn(Single.just(peopleToUpload))
        }

        peopleInDb?.let {
            whenever(dbManagerMock.getPeopleCountFromLocalForSyncScope(anyNotNull())).thenReturn(Single.just(peopleInDb))
        }

        peopleToDownload?.let {
            whenever(dbManagerMock.calculateNPatientsToDownSync(anyNotNull(), anyNotNull(), anyNotNull())).thenReturn(Single.just(peopleToDownload))
        }
    }

    private fun createViewModelDashboardToTest() =
        DashboardSyncCardViewModel(
            DashboardCardType.SYNC_DB,
            1,
            testAppComponent,
            downSyncDao,
            upSyncDao)

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
        verify(localDbManagerMock, times(requiredCallsToInitTotalCounter)).getPeopleCountFromLocal(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
    }

    private fun verifyCalculateNPatientsToDownSyncWasCalled(times: Int) {
        verify(dbManagerMock, times(times)).calculateNPatientsToDownSync(anyNotNull(), anyNotNull(), anyNotNull())

    }

    private fun verifyGetPeopleCountFromLocalForSyncScopeWasCalled(requiredCallToInitAndUpdateUpSyncCounter: Int) {
        verify(dbManagerMock, times(requiredCallToInitAndUpdateUpSyncCounter)).getPeopleCountFromLocalForSyncScope(anyNotNull())
    }
}
