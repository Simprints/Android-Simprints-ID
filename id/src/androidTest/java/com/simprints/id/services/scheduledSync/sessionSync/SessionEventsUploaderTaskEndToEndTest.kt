package com.simprints.id.services.scheduledSync.sessionSync

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth
import com.simprints.id.Application
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_REALM_KEY
import com.simprints.id.commontesttools.di.DependencyRule
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.commontesttools.di.TestPreferencesModule
import com.simprints.id.commontesttools.sessionEvents.createFakeClosedSession
import com.simprints.id.data.analytics.eventData.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventData.controllers.local.SessionEventsLocalDbManager
import com.simprints.id.data.analytics.eventData.models.domain.session.SessionEvents
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.sessions.RemoteSessionsManager
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncMasterTask.Companion.BATCH_SIZE
import com.simprints.id.testSnippets.*
import com.simprints.id.testtools.AndroidTestConfig
import com.simprints.id.testtools.TestProjectRule
import com.simprints.id.testtools.integration.adapters.toCalloutCredentials
import com.simprints.id.testtools.integration.models.TestProject
import com.simprints.id.testtools.integration.remote.RemoteTestingManager
import com.simprints.id.tools.RandomGenerator
import com.simprints.id.tools.TimeHelper
import com.simprints.libsimprints.FingerIdentifier
import com.simprints.mockscanner.MockBluetoothAdapter
import com.simprints.mockscanner.MockFinger
import com.simprints.mockscanner.MockScannerManager
import com.simprints.testframework.common.syntax.awaitAndAssertSuccess
import com.simprints.testframework.common.syntax.whenever
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@SmallTest
class SessionEventsUploaderTaskEndToEndTest {

    private val app = ApplicationProvider.getApplicationContext<Application>()

    @get:Rule val testProjectRule = TestProjectRule()
    private lateinit var testProject: TestProject

    @get:Rule val simprintsActionTestRule = ActivityTestRule(CheckLoginFromIntentActivity::class.java, false, false)

    @Inject lateinit var realmSessionEventsManager: SessionEventsLocalDbManager
    @Inject lateinit var sessionEventsManager: SessionEventsManager
    @Inject lateinit var settingsPreferencesManagerSpy: SettingsPreferencesManager
    @Inject lateinit var remoteDbManager: RemoteDbManager
    @Inject lateinit var remoteSessionsManager: RemoteSessionsManager
    @Inject lateinit var timeHelper: TimeHelper
    @Inject lateinit var randomGeneratorMock: RandomGenerator

    private val preferencesModule by lazy {
        TestPreferencesModule(settingsPreferencesManagerRule = DependencyRule.SpyRule)
    }

    private val module by lazy {
        TestAppModule(
            app,
            randomGeneratorRule = DependencyRule.MockRule,
            bluetoothComponentAdapterRule = DependencyRule.ReplaceRule { mockBluetoothAdapter }
        )
    }

    private lateinit var mockBluetoothAdapter: MockBluetoothAdapter

    @Before
    fun setUp() {
        AndroidTestConfig(this, module, preferencesModule).fullSetup()

        setupRandomGeneratorToGenerateKey(DEFAULT_REALM_KEY, randomGeneratorMock)

        whenever(settingsPreferencesManagerSpy.fingerStatus).thenReturn(hashMapOf(
            FingerIdentifier.LEFT_THUMB to true,
            FingerIdentifier.LEFT_INDEX_FINGER to true))

        testProject = testProjectRule.testProject
        signOut()
    }

    @Test
    fun closeSessions_shouldGetUploaded() {
        mockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager(mockFingers = arrayOf(*MockFinger.person1TwoFingersGoodScan)))

        launchActivityEnrol(testProject.toCalloutCredentials(), simprintsActionTestRule)
        enterCredentialsDirectly(testProject.toCalloutCredentials(), testProject.secret)
        pressSignIn()
        setupActivityAndContinue()

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
        remoteDbManager.signOutOfRemoteDb()
    }
}
