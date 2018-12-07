package com.simprints.id.activities

import android.content.Intent
import androidx.test.InstrumentationRegistry
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.dashboard.DashboardActivity
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.prefs.PreferencesManagerImpl
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.di.AppModuleForAndroidTests
import com.simprints.id.di.DaggerForAndroidTests
import com.simprints.id.domain.Constants
import com.simprints.id.domain.Person
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilder
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import com.simprints.id.shared.DefaultTestConstants.DEFAULT_REALM_KEY
import com.simprints.id.shared.DependencyRule
import com.simprints.id.shared.PeopleGeneratorUtils
import com.simprints.id.shared.PreferencesModuleForAnyTests
import com.simprints.id.shared.whenever
import com.simprints.id.testTemplates.FirstUseLocalAndRemote
import com.simprints.id.testTools.LoginManagerTest
import com.simprints.id.testTools.models.TestProject
import com.simprints.id.testTools.remote.RemoteTestingManager
import com.simprints.id.testTools.tryOnUiUntilTimeout
import com.simprints.id.tools.RandomGenerator
import com.simprints.id.tools.delegates.lazyVar
import io.realm.Realm
import io.realm.RealmConfiguration
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
class DashboardActivityAndroidTest : DaggerForAndroidTests(), FirstUseLocalAndRemote {

    companion object {
        private const val N_PEOPLE_ON_SERVER_PER_MODULE = 300 //300 * 3 = 900
        private const val N_PEOPLE_ON_DB_PER_MODULE = 30
        private const val SIGNED_ID_USER = "some_user"
    }

    override lateinit var testProject: TestProject

    @Rule @JvmField val launchActivityRule = ActivityTestRule(DashboardActivity::class.java, false, false)
    override var peopleRealmConfiguration: RealmConfiguration? = null
    override var sessionsRealmConfiguration: RealmConfiguration? = null

    @Inject lateinit var secureDataManagerSpy: SecureDataManager
    @Inject lateinit var remoteDbManagerSpy: RemoteDbManager
    @Inject lateinit var localDbManager: LocalDbManager
    @Inject lateinit var syncScopesBuilder: SyncScopesBuilder
    @Inject lateinit var settingsPreferencesManagerSpy: SettingsPreferencesManager

    override var preferencesModule: PreferencesModuleForAnyTests by lazyVar {
        PreferencesModuleForAnyTests(
            settingsPreferencesManagerRule = DependencyRule.SpyRule)
    }

    override var module by lazyVar {
        AppModuleForAndroidTests(app,
            remoteDbManagerRule = DependencyRule.SpyRule,
            randomGeneratorRule = DependencyRule.MockRule,
            secureDataManagerRule = DependencyRule.SpyRule)
    }

    @Inject lateinit var randomGeneratorMock: RandomGenerator

    private val remoteTestingManager: RemoteTestingManager = RemoteTestingManager.create()
    private var peopleInDb = mutableListOf<Person>()
    private var peopleOnServer = mutableListOf<Person>()

    @Before
    override fun setUp() {
        app = InstrumentationRegistry.getTargetContext().applicationContext as Application
        super<DaggerForAndroidTests>.setUp()
        testAppComponent.inject(this)

        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
        super<FirstUseLocalAndRemote>.setUp()

        mockBeingSignedIn()
        app.initDependencies()

        mockGlobalScope()
    }

    @Test
    fun openDashboardWithGlobalSync_shouldShowDashboardSyncCard() {
        createPeopleAndPrepareRemoteAndLocalDbs(mockGlobalScope())
        launchActivityRule.launchActivity(Intent())
        waitForDownSyncCountAndValidateUI()
    }

    @Test
    fun openDashboardWithUserSync_shouldShowDashboardSyncCard() {
        createPeopleAndPrepareRemoteAndLocalDbs(mockUserScope())
        launchActivityRule.launchActivity(Intent())
        waitForDownSyncCountAndValidateUI()
    }

    @Test
    fun openDashboardWithModuleSync_shouldShowDashboardSyncCard() {
        createPeopleAndPrepareRemoteAndLocalDbs(mockModuleScope())
        launchActivityRule.launchActivity(Intent())
        waitForDownSyncCountAndValidateUI()
    }

    private fun waitForDownSyncCountAndValidateUI() {
        tryOnUiUntilTimeout(10000, 200) {
            onView(withId(R.id.dashboardCardSyncDownloadText))
                .check(matches(Matchers.not(withText(""))))
                .check(matches(Matchers.not(withText("0"))))
        }

        onView(withId(R.id.dashboardCardSyncDownloadText))
            .check(matches(withText("${peopleOnServer.size - peopleInDbForSyncScope(syncScopesBuilder.buildSyncScope(), false)}")))

        onView(withId(R.id.dashboardCardSyncUploadText))
            .check(matches(withText("${peopleInDbForSyncScope(syncScopesBuilder.buildSyncScope(), true)}")))

        onView(withId(R.id.dashboardCardSyncTotalLocalText))
            .check(matches(withText("${peopleInDb.size}")))
    }

    private fun peopleInDbForSyncScope(scope: SyncScope, toSync: Boolean): Int =
        peopleInDb.count {
            it.toSync == toSync &&
                it.projectId == scope.projectId &&
                if (scope.userId != null) {
                    it.userId == scope.userId
                } else {
                    true
                } &&
                if (!scope.moduleIds.isNullOrEmpty()) {
                    scope.moduleIds?.contains(it.moduleId) ?: false
                } else {
                    true
                }
        }

    private fun mockGlobalScope(): SyncScope {
        whenever(settingsPreferencesManagerSpy.syncGroup).thenReturn(Constants.GROUP.GLOBAL)
        return syncScopesBuilder.buildSyncScope()
    }

    private fun mockUserScope(): SyncScope {
        whenever(settingsPreferencesManagerSpy.syncGroup).thenReturn(Constants.GROUP.USER)
        return syncScopesBuilder.buildSyncScope()
    }

    private fun mockModuleScope(): SyncScope {
        whenever(settingsPreferencesManagerSpy.moduleIdOptions).thenReturn(setOf("module1", "module2", "module3"))
        whenever(settingsPreferencesManagerSpy.syncGroup).thenReturn(Constants.GROUP.MODULE)
        return syncScopesBuilder.buildSyncScope()
    }


    private fun createPeopleAndPrepareRemoteAndLocalDbs(syncScope: SyncScope) {
        peopleOnServer = PeopleGeneratorUtils.getRandomPeople(N_PEOPLE_ON_SERVER_PER_MODULE, syncScope, listOf(false))
        remoteDbManagerSpy.uploadPeople(testProject.id, peopleOnServer).blockingAwait()
        peopleInDb.addAll(PeopleGeneratorUtils.getRandomPeople(N_PEOPLE_ON_DB_PER_MODULE, syncScope, listOf(true, false)))
        localDbManager.insertOrUpdatePeopleInLocal(peopleInDb).blockingAwait()
    }

    private fun mockBeingSignedIn() {
        val token = remoteTestingManager.generateFirebaseToken(testProject.id, SIGNED_ID_USER)
        LoginManagerTest().setUpSignedInState(
            app.getSharedPreferences(PreferencesManagerImpl.PREF_FILE_NAME, PreferencesManagerImpl.PREF_MODE),
            secureDataManagerSpy,
            remoteDbManagerSpy,
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

    @After
    override fun tearDown() {
        super.tearDown()
    }

    private fun signOut() {
        remoteDbManagerSpy.signOutOfRemoteDb()
    }
}
