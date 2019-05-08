package com.simprints.id.services.scheduledSync.sessionSync

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth
import com.simprints.id.Application
import com.simprints.id.FingerIdentifier
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import com.simprints.id.commontesttools.DefaultTestConstants
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.commontesttools.di.TestPreferencesModule
import com.simprints.id.commontesttools.sessionEvents.createFakeClosedSession
import com.simprints.id.commontesttools.state.LoginStateMocker
import com.simprints.id.commontesttools.state.setupRandomGeneratorToGenerateKey
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.controllers.local.SessionEventsLocalDbManager
import com.simprints.id.data.analytics.eventdata.models.domain.session.SessionEvents
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.sessions.RemoteSessionsManager
import com.simprints.id.data.prefs.PreferencesManagerImpl
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncMasterTask.Companion.BATCH_SIZE
import com.simprints.id.testtools.AndroidTestConfig
import com.simprints.id.testtools.testingapi.TestProjectRule
import com.simprints.id.testtools.testingapi.models.TestProject
import com.simprints.id.testtools.testingapi.remote.RemoteTestingManager
import com.simprints.id.tools.RandomGenerator
import com.simprints.id.tools.TimeHelper
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.common.syntax.awaitAndAssertSuccess
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.whenever
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@SmallTest
class SessionEventsUploaderTaskAndroidTest { // TODO : Tests are failing because creating a project remotely is throwing a 404

    companion object {
        const val SIGNED_ID_USER = "some_signed_user"
    }

    private val app = ApplicationProvider.getApplicationContext<Application>()

    @get:Rule val testProjectRule = TestProjectRule()
    private lateinit var testProject: TestProject

    @get:Rule val simprintsActionTestRule = ActivityTestRule(CheckLoginFromIntentActivity::class.java, false, false)

    @Inject lateinit var realmSessionEventsManager: SessionEventsLocalDbManager
    @Inject lateinit var sessionEventsManager: SessionEventsManager
    @Inject lateinit var settingsPreferencesManagerSpy: SettingsPreferencesManager
    @Inject lateinit var remoteSessionsManager: RemoteSessionsManager
    @Inject lateinit var timeHelper: TimeHelper
    @Inject lateinit var randomGeneratorMock: RandomGenerator
    @Inject lateinit var secureDataManagerSpy: SecureDataManager
    @Inject lateinit var remoteDbManagerSpy: RemoteDbManager

    private val remoteTestingManager: RemoteTestingManager = RemoteTestingManager.create()

    private val preferencesModule by lazy {
        TestPreferencesModule(settingsPreferencesManagerRule = DependencyRule.SpyRule)
    }

    private val module by lazy {
        TestAppModule(
            app,
            remoteDbManagerRule = DependencyRule.SpyRule,
            randomGeneratorRule = DependencyRule.ReplaceRule { mock<RandomGenerator>().apply { setupRandomGeneratorToGenerateKey(this) } },
            secureDataManagerRule = DependencyRule.SpyRule
        )
    }

    @Before
    fun setUp() {
        AndroidTestConfig(this, module, preferencesModule).fullSetup()

        whenever(settingsPreferencesManagerSpy.fingerStatus).thenReturn(hashMapOf(
            FingerIdentifier.LEFT_THUMB to true,
            FingerIdentifier.LEFT_INDEX_FINGER to true))

        testProject = testProjectRule.testProject
        signOut()
    }

    @Test
    fun closeSessions_shouldGetUploaded() {
        mockBeingSignedIn()

        val nSession = BATCH_SIZE + 1
        createClosedSessions(nSession).forEach {
            realmSessionEventsManager.insertOrUpdateSessionEvents(it).blockingAwait()
        }

        val testObserver = executeUpload()
        testObserver.awaitAndAssertSuccess()

        val response = RemoteTestingManager.create().getSessionCount(testProject.id)
        Truth.assertThat(response.count).isEqualTo(nSession)
    }

    private fun executeUpload(): TestObserver<Void> {
        val syncTask = SessionEventsUploaderTask(
            sessionEventsManager,
            timeHelper,
            remoteSessionsManager.getSessionsApiClient().blockingGet())

        return syncTask.execute(
            testProject.id,
            realmSessionEventsManager.loadSessions().blockingGet()).test()
    }

    private fun createClosedSessions(nSessions: Int) =
        mutableListOf<SessionEvents>().apply {
            repeat(nSessions) { this.add(createFakeClosedSession(timeHelper, testProject.id)) }
        }

    private fun signOut() {
        remoteDbManagerSpy.signOutOfRemoteDb()
    }

    private fun mockBeingSignedIn() {
        val token = remoteTestingManager.generateFirebaseToken(testProject.id, SIGNED_ID_USER)
        LoginStateMocker.setupLoginStateFullyToBeSignedIn(
            app.getSharedPreferences(PreferencesManagerImpl.PREF_FILE_NAME, PreferencesManagerImpl.PREF_MODE),
            secureDataManagerSpy,
            remoteDbManagerSpy,
            testProject.id,
            SIGNED_ID_USER,
            testProject.secret,
            LocalDbKey(
                testProject.id,
                DefaultTestConstants.DEFAULT_REALM_KEY),
            token.token)
    }
}
