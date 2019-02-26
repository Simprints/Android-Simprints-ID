package com.simprints.id.data.analytics.eventdata

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth
import com.simprints.id.Application
import com.simprints.id.FingerIdentifier
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_REALM_KEY
import com.simprints.id.commontesttools.di.DependencyRule
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.commontesttools.di.TestPreferencesModule
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.controllers.local.RealmSessionEventsDbManagerImpl
import com.simprints.id.data.analytics.eventdata.controllers.local.SessionEventsLocalDbManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.RefusalEvent
import com.simprints.id.data.analytics.eventdata.models.domain.session.DatabaseInfo
import com.simprints.id.data.analytics.eventdata.models.domain.session.Location
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.testtools.state.setupRandomGeneratorToGenerateKey
import com.simprints.id.testtools.AndroidTestConfig
import com.simprints.id.tools.RandomGenerator
import com.simprints.id.tools.TimeHelper
import com.simprints.mockscanner.MockBluetoothAdapter
import com.simprints.testtools.common.syntax.whenever
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@SmallTest
class RealmSessionEventsDbManagerImplTest {

    private val app = ApplicationProvider.getApplicationContext<Application>()

    private val testProjectId1 = "test_project1"
    private val testProjectId2 = "test_project2"
    private val testProjectId3 = "test_project3"

    @get:Rule val simprintsActionTestRule = ActivityTestRule(CheckLoginFromIntentActivity::class.java, false, false)

    @Inject lateinit var realmSessionEventsManager: SessionEventsLocalDbManager
    @Inject lateinit var sessionEventsManagerSpy: SessionEventsManager
    @Inject lateinit var settingsPreferencesManagerSpy: SettingsPreferencesManager
    @Inject lateinit var remoteDbManager: RemoteDbManager
    @Inject lateinit var timeHelper: TimeHelper
    @Inject lateinit var randomGeneratorMock: RandomGenerator

    private val preferencesModule by lazy {
        TestPreferencesModule(settingsPreferencesManagerRule = DependencyRule.SpyRule)
    }

    private val module by lazy {
        TestAppModule(
            app,
            randomGeneratorRule = DependencyRule.MockRule,
            localDbManagerRule = DependencyRule.SpyRule,
            remoteDbManagerRule = DependencyRule.SpyRule,
            sessionEventsManagerRule = DependencyRule.SpyRule,
            bluetoothComponentAdapterRule = DependencyRule.ReplaceRule { mockBluetoothAdapter }
        )
    }

    private lateinit var mockBluetoothAdapter: MockBluetoothAdapter
    private val realmForDataEvent
        get() = (realmSessionEventsManager as RealmSessionEventsDbManagerImpl).getRealmInstance().blockingGet()

    @Before
    fun setUp() {
        AndroidTestConfig(this, module, preferencesModule).fullSetup()

        setupRandomGeneratorToGenerateKey(DEFAULT_REALM_KEY, randomGeneratorMock)

        signOut()

        whenever(settingsPreferencesManagerSpy.fingerStatus).thenReturn(hashMapOf(
            FingerIdentifier.LEFT_THUMB to true,
            FingerIdentifier.LEFT_INDEX_FINGER to true))
    }

    @Test
    fun deleteSessions_shouldCleanDb() {
        val sessionOpenProject1Id = createAndSaveOpenSession()
        val sessionCloseProject1Id = createAndSaveCloseSession()
        val sessionCloseProject2Id = createAndSaveCloseSession(testProjectId2)
        createAndSaveCloseSession(testProjectId3)
        createAndSaveCloseSession(testProjectId3)

        sessionEventsManagerSpy.updateSession {
            it.databaseInfo = DatabaseInfo(0)
            it.location = Location(0.0, 0.0)
            it.events.add(RefusalEvent(200, 200, RefusalEvent.Answer.OTHER, "fake_event"))
        }.blockingGet()

        verifyNumberOfSessionsInDb(5, realmForDataEvent)

        realmSessionEventsManager.deleteSessions(projectId = testProjectId3).blockingAwait()
        verifySessionsStoredInDb(sessionOpenProject1Id, sessionCloseProject1Id, sessionCloseProject2Id)

        realmSessionEventsManager.deleteSessions(openSession = true).blockingAwait()
        verifySessionsStoredInDb(sessionCloseProject1Id, sessionCloseProject2Id)

        realmSessionEventsManager.deleteSessions(openSession = false).blockingAwait()

        verifyNumberOfSessionsInDb(0, realmForDataEvent)
        verifyNumberOfEventsInDb(0, realmForDataEvent)
        verifyNumberOfDatabaseInfosInDb(0, realmForDataEvent)
        verifyNumberOfLocationsInDb(0, realmForDataEvent)
        verifyNumberOfDeviceInfosInDb(0, realmForDataEvent)
    }

    private fun verifySessionsStoredInDb(vararg sessionsIds: String) {
        with(realmSessionEventsManager.loadSessions().blockingGet()) {
            val ids = this.map { it.id }
            Truth.assertThat(ids).containsExactlyElementsIn(sessionsIds)
        }
    }

    private fun createAndSaveCloseSession(projectId: String = testProjectId1): String =
        createAndSaveCloseFakeSession(timeHelper, realmSessionEventsManager, projectId)

    private fun createAndSaveOpenSession(projectId: String = testProjectId1): String =
        createAndSaveOpenFakeSession(timeHelper, realmSessionEventsManager, projectId)

    private fun signOut() {
        remoteDbManager.signOutOfRemoteDb()
    }
}
