package com.simprints.id.integration.sync

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.work.WorkManager
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.dashboard.DashboardActivity
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_REALM_KEY
import com.simprints.id.commontesttools.PeopleGeneratorUtils
import com.simprints.id.commontesttools.di.DependencyRule
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.commontesttools.di.TestPreferencesModule
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.network.PeopleRemoteInterface
import com.simprints.id.data.db.remote.people.RemotePeopleManager
import com.simprints.id.data.prefs.PreferencesManagerImpl
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.domain.Constants
import com.simprints.id.domain.Person
import com.simprints.id.integration.testtools.TestProjectRule
import com.simprints.id.integration.testtools.models.TestProject
import com.simprints.id.integration.testtools.remote.RemoteTestingManager
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.DownSyncManager
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilder
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import com.simprints.id.testtools.AndroidTestConfig
import com.simprints.id.testtools.state.LoginStateMocker
import com.simprints.id.tools.RandomGenerator
import com.simprints.testframework.android.tryOnUiUntilTimeout
import com.simprints.testframework.android.waitOnSystem
import com.simprints.testframework.common.syntax.whenever
import io.reactivex.Completable
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
class DashboardActivityAndroidTest {

    companion object {
        private val modules = setOf("module1", "module2", "module3")
        private const val N_PEOPLE_ON_SERVER_PER_MODULE = 200 //200 * 3 (#modules) = 600
        private const val N_PEOPLE_ON_DB_PER_MODULE = 30
        private const val PEOPLE_UPLOAD_BATCH_SIZE = 20
        private const val SIGNED_ID_USER = "some_user"
    }

    private val app = ApplicationProvider.getApplicationContext<Application>()

    @get:Rule val testProjectRule = TestProjectRule()
    private lateinit var testProject: TestProject

    @get:Rule val launchActivityRule = ActivityTestRule(DashboardActivity::class.java, false, false)

    @Inject lateinit var secureDataManagerSpy: SecureDataManager
    @Inject lateinit var remoteDbManagerSpy: RemoteDbManager
    @Inject lateinit var remotePeopleManagerSpy: RemotePeopleManager
    @Inject lateinit var localDbManager: LocalDbManager
    @Inject lateinit var syncScopesBuilder: SyncScopesBuilder
    @Inject lateinit var settingsPreferencesManagerSpy: SettingsPreferencesManager
    @Inject lateinit var downSyncManager: DownSyncManager

    private val syncScope
        get() = syncScopesBuilder.buildSyncScope()!!

    private val preferencesModule by lazy {
        TestPreferencesModule(
            settingsPreferencesManagerRule = DependencyRule.SpyRule)
    }

    private val module by lazy {
        TestAppModule(app,
            remoteDbManagerRule = DependencyRule.SpyRule,
            randomGeneratorRule = DependencyRule.MockRule,
            secureDataManagerRule = DependencyRule.SpyRule)
    }

    @Inject lateinit var randomGeneratorMock: RandomGenerator

    private val remoteTestingManager: RemoteTestingManager = RemoteTestingManager.create()
    private var peopleInDb = mutableListOf<Person>()
    private var peopleOnServer = mutableListOf<Person>()

    @Before
    fun setUp() {
        AndroidTestConfig(this, module, preferencesModule).fullSetup()

        testProject = testProjectRule.testProject

        mockBeingSignedIn()
        signOut()

        mockGlobalScope()

        WorkManager.getInstance().cancelAllWork()
        WorkManager.getInstance().pruneWork()
    }

    @Test
    fun openDashboardWithGlobalSync_shouldShowTheRightCounters() {
        uploadFakePeopleAndPrepareLocalDb(mockGlobalScope())
        launchActivityRule.launchActivity(Intent())
        waitForDownSyncCountAndValidateUI()
    }

    @Test
    fun openDashboardWithUserSync_shouldShowTheRightCounters() {
        uploadFakePeopleAndPrepareLocalDb(mockUserScope())
        launchActivityRule.launchActivity(Intent())
        waitForDownSyncCountAndValidateUI()
    }

    @Test
    fun openDashboardWithModuleSync_shouldShowTheRightCounters() {
        uploadFakePeopleAndPrepareLocalDb(mockModuleScope())
        launchActivityRule.launchActivity(Intent())
        waitForDownSyncCountAndValidateUI()
    }

    @Test
    fun downSyncRunning_shouldShowTheRightStateAndUpdateCountersAtTheEnd() {
        uploadFakePeopleAndPrepareLocalDb(mockGlobalScope())

        launchActivityRule.launchActivity(Intent())

        waitOnSystem(2000)

        downSyncManager.enqueueOneTimeDownSyncMasterWorker()

        tryOnUiUntilTimeout(10000, 20) {
            onView(withId(R.id.dashboardSyncCardSyncButton)).check(matches(withText(R.string.dashboard_card_calculating)))
        }

        tryOnUiUntilTimeout(10000, 20) {
            onView(withId(R.id.dashboardCardSyncDescription))
                .check(matches(withText(not(String.format(app.getString(R.string.dashboard_card_syncing), "")))))
        }

        onView(withId(R.id.dashboardCardSyncUploadText))
            .check(matches(withText("${peopleInDbForSyncScope(syncScope, true)}")))

        onView(withId(R.id.dashboardCardSyncTotalLocalText))
            .check(matches(withText("${peopleInDb.size}")))

        tryOnUiUntilTimeout(10000, 200) {
            onView(withId(R.id.dashboardSyncCardSyncButton)).check(matches(withText(R.string.dashboard_card_sync_now)))
        }

        onView(withId(R.id.dashboardCardSyncTotalLocalText))
            .check(matches(withText("${localDbManager.getPeopleCountFromLocal().blockingGet()}")))

        onView(withId(R.id.dashboardCardSyncDownloadText))
            .check(matches(withText("0")))
    }

    @Test
    fun openDashboardInOffline_shouldNotShowDownloadCounter() {
        PeopleRemoteInterface.baseUrl = "http://wrong_url_simprints_com.com"
        downSyncManager.enqueueOneTimeDownSyncMasterWorker()

        launchActivityRule.launchActivity(Intent())
        waitOnSystem(3000)

        onView(withId(R.id.dashboardSyncCardSyncButton)).check(matches(withText(R.string.dashboard_card_sync_now)))
        onView(withId(R.id.dashboardCardSyncDownloadText))
            .check(matches(withText("")))
    }

    private fun waitForDownSyncCountAndValidateUI() {
        tryOnUiUntilTimeout(10000, 200) {
            onView(withId(R.id.dashboardCardSyncDownloadText))
                .check(matches(Matchers.not(withText(""))))
        }

        onView(withId(R.id.dashboardCardSyncDownloadText))
            .check(matches(withText("${peopleOnServer.size - peopleInDbForSyncScope(syncScope, false)}")))

        onView(withId(R.id.dashboardCardSyncUploadText))
            .check(matches(withText("${peopleInDbForSyncScope(syncScope, true)}")))

        onView(withId(R.id.dashboardCardSyncTotalLocalText))
            .check(matches(withText("${peopleInDb.size}")))
    }

    private fun peopleInDbForSyncScope(scope: SyncScope, toSync: Boolean): Int =
        peopleInDb.count {
            it.toSync == toSync &&
            it.projectId == scope.projectId &&
            if (scope.userId != null) {  it.userId == scope.userId } else { true } &&
            if (!scope.moduleIds.isNullOrEmpty()) { scope.moduleIds?.contains(it.moduleId) ?: false } else { true }
        }

    private fun mockGlobalScope(): SyncScope {
        whenever(settingsPreferencesManagerSpy.syncGroup).thenReturn(Constants.GROUP.GLOBAL)
        return syncScope
    }

    private fun mockUserScope(): SyncScope {
        whenever(settingsPreferencesManagerSpy.syncGroup).thenReturn(Constants.GROUP.USER)
        return syncScope
    }

    private fun mockModuleScope(): SyncScope {
        whenever(settingsPreferencesManagerSpy.selectedModules).thenReturn(modules)
        whenever(settingsPreferencesManagerSpy.syncGroup).thenReturn(Constants.GROUP.MODULE)
        return syncScope
    }


    private fun uploadFakePeopleAndPrepareLocalDb(syncScope: SyncScope) {
        peopleOnServer = PeopleGeneratorUtils.getRandomPeople(N_PEOPLE_ON_SERVER_PER_MODULE, syncScope, listOf(false))
        val requests = peopleOnServer.chunked(PEOPLE_UPLOAD_BATCH_SIZE).map {
            remotePeopleManagerSpy.uploadPeople(testProject.id, it)
        }
        Completable.merge(requests).blockingAwait()

        peopleInDb.addAll(PeopleGeneratorUtils.getRandomPeople(N_PEOPLE_ON_DB_PER_MODULE, syncScope, listOf(true, false)))
        localDbManager.insertOrUpdatePeopleInLocal(peopleInDb).blockingAwait()
    }

    private fun mockBeingSignedIn() {
        val token = remoteTestingManager.generateFirebaseToken(testProject.id, SIGNED_ID_USER)
        LoginStateMocker.setupLoginStateFullyToBeSignedIn(
            app.getSharedPreferences(PreferencesManagerImpl.PREF_FILE_NAME, PreferencesManagerImpl.PREF_MODE),
            secureDataManagerSpy,
            remoteDbManagerSpy,
            localDbManager,
            testProject.id,
            testProject.legacyId,
            SIGNED_ID_USER,
            testProject.secret,
            LocalDbKey(
                testProject.id,
                DEFAULT_REALM_KEY,
                testProject.legacyId),
            token.token)
    }

    private fun signOut() {
        remoteDbManagerSpy.signOutOfRemoteDb()
    }
}
