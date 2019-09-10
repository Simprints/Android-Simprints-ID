package com.simprints.id.services.scheduledSync.sessionSync

import android.net.NetworkInfo
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth
import com.simprints.id.Application
import com.simprints.id.data.db.person.domain.FingerIdentifier
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import com.simprints.id.commontesttools.DefaultTestConstants
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.commontesttools.di.TestPreferencesModule
import com.simprints.id.commontesttools.sessionEvents.createFakeClosedSession
import com.simprints.id.commontesttools.state.LoginStateMocker
import com.simprints.id.commontesttools.state.setupRandomGeneratorToGenerateKey
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.controllers.local.SessionEventsLocalDbManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.*
import com.simprints.id.data.analytics.eventdata.models.domain.events.callback.*
import com.simprints.id.data.analytics.eventdata.models.domain.events.callout.ConfirmationCalloutEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.callout.EnrolmentCalloutEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.callout.IdentificationCalloutEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.callout.VerificationCalloutEvent
import com.simprints.id.data.analytics.eventdata.models.domain.session.SessionEvents
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.sessions.RemoteSessionsManager
import com.simprints.id.data.prefs.PreferencesManagerImpl
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.domain.moduleapi.app.responses.entities.Tier
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncMasterTask.Companion.BATCH_SIZE
import com.simprints.id.testtools.AndroidTestConfig
import com.simprints.id.testtools.testingapi.TestProjectRule
import com.simprints.id.testtools.testingapi.models.TestProject
import com.simprints.id.testtools.testingapi.remote.RemoteTestingManager
import com.simprints.id.tools.RandomGenerator
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.utils.SimNetworkUtils
import com.simprints.testtools.android.waitOnSystem
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.common.syntax.awaitAndAssertSuccess
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.whenever
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class SessionEventsUploaderTaskAndroidTest {

    companion object {
        const val SIGNED_ID_USER = "some_signed_user"
        val RANDOM_GUID = UUID.randomUUID().toString()
        const val CLOUD_ASYNC_SESSION_CREATION_TIMEOUT = 5000L
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
            it.addAlertScreenEvents()
            realmSessionEventsManager.insertOrUpdateSessionEvents(it).blockingAwait()
        }

        val testObserver = executeUpload()
        testObserver.awaitAndAssertSuccess()

        waitOnSystem(CLOUD_ASYNC_SESSION_CREATION_TIMEOUT)

        val response = RemoteTestingManager.create().getSessionCount(testProject.id)
        Truth.assertThat(response.count).isEqualTo(nSession)
    }

    @Test
    fun closeSession_withAllEvents_shouldGetUploaded() {
        mockBeingSignedIn()

        createClosedSessions(1).first().apply {
            addAlertScreenEvents()
            addArtificialTerminationEvent()
            addAuthenticationEvent()
            addAuthorizationEvent()
            addCandidateReadEvent()
            addConnectivitySnapshotEvent()
            addConsentEvent()
            addEnrolmentEvent()
            addFingerprintCaptureEvent()
            addGuidSelectionEvent()
            addIntentParsingEvent()
            addInvalidIntentEvent()
            addOneToOneMatchEvent()
            addOneToManyMatchEvent()
            addPersonCreationEvent()
            addRefusalEvent()
            addScannerConnectionEvent()
            addSuspiciousIntentEvent()
            addCallbackEvent()
            addCalloutEvent()
            addCompletionCheckEvent()
        }.also {
            realmSessionEventsManager.insertOrUpdateSessionEvents(it).blockingAwait()
        }

        val testObserver = executeUpload()
        testObserver.awaitAndAssertSuccess()

        waitOnSystem(CLOUD_ASYNC_SESSION_CREATION_TIMEOUT)

        val response = RemoteTestingManager.create().getSessionCount(testProject.id)
        Truth.assertThat(response.count).isEqualTo(1)
    }

    private fun SessionEvents.addAlertScreenEvents() {
        AlertScreenEvent.AlertScreenEventType.values().forEach {
            addEvent(AlertScreenEvent(0, it))
        }
    }

    private fun SessionEvents.addArtificialTerminationEvent() {
        ArtificialTerminationEvent.Reason.values().forEach {
            addEvent(ArtificialTerminationEvent(0, it))
        }
    }

    private fun SessionEvents.addAuthenticationEvent() {
        AuthenticationEvent.Result.values().forEach {
            addEvent(AuthenticationEvent(0, 0, AuthenticationEvent.UserInfo("some_project", "user_id"), it))
        }
    }

    private fun SessionEvents.addAuthorizationEvent() {
        AuthorizationEvent.Result.values().forEach {
            addEvent(AuthorizationEvent(0, it, AuthorizationEvent.UserInfo("some_project", "user_id")))
        }
    }

    private fun SessionEvents.addCandidateReadEvent() {
        CandidateReadEvent.LocalResult.values().forEach { local ->
            CandidateReadEvent.RemoteResult.values().forEach { remote ->
                addEvent(CandidateReadEvent(0, 0, RANDOM_GUID, local, remote))
            }
        }
    }

    private fun SessionEvents.addConnectivitySnapshotEvent() {
        addEvent(ConnectivitySnapshotEvent(0,  "Unknown", listOf(SimNetworkUtils.Connection("connection", NetworkInfo.DetailedState.CONNECTED))))
    }

    private fun SessionEvents.addConsentEvent() {
        ConsentEvent.Type.values().forEach { type ->
            ConsentEvent.Result.values().forEach { result ->
                addEvent(ConsentEvent(0, 0, type, result))
            }
        }
    }

    private fun SessionEvents.addEnrolmentEvent() {
        addEvent(EnrolmentEvent(0, RANDOM_GUID))
    }

    private fun SessionEvents.addFingerprintCaptureEvent() {
        FingerprintCaptureEvent.Result.values().forEach { result ->
            FingerIdentifier.values().forEach { fingerIdentifier ->
                addEvent(FingerprintCaptureEvent(0, 0, fingerIdentifier, 0, result,
                    FingerprintCaptureEvent.Fingerprint(fingerIdentifier, 0, "some_template")))
            }
        }
    }

    private fun SessionEvents.addGuidSelectionEvent() {
        addEvent(GuidSelectionEvent(0, "selected_id"))
    }

    private fun SessionEvents.addIntentParsingEvent() {
        IntentParsingEvent.IntegrationInfo.values().forEach {
            addEvent(IntentParsingEvent(0, it))
        }
    }

    private fun SessionEvents.addInvalidIntentEvent() {
        addEvent(InvalidIntentEvent(0, "some_action", emptyMap()))
    }

    private fun SessionEvents.addOneToManyMatchEvent() {
        OneToManyMatchEvent.MatchPoolType.values().forEach {
            addEvent(OneToManyMatchEvent(0, 0, OneToManyMatchEvent.MatchPool(it, 0), emptyList()))
        }
    }

    private fun SessionEvents.addOneToOneMatchEvent() {
        addEvent(OneToOneMatchEvent(0, 0, RANDOM_GUID, MatchEntry(RANDOM_GUID, 0F)))
    }

    private fun SessionEvents.addPersonCreationEvent() {
        addEvent(PersonCreationEvent(0, listOf(RANDOM_GUID, RANDOM_GUID)))
    }

    private fun SessionEvents.addRefusalEvent() {
        RefusalEvent.Answer.values().forEach {
            addEvent(RefusalEvent(0, 0, it, "other_text"))
        }
    }

    private fun SessionEvents.addScannerConnectionEvent() {
        addEvent(ScannerConnectionEvent(0, ScannerConnectionEvent.ScannerInfo("scanner_id", "macAddress", "hardware")))
    }

    private fun SessionEvents.addSuspiciousIntentEvent() {
        addEvent(SuspiciousIntentEvent(0, mapOf("some_extra_key" to "value")))
    }

    private fun SessionEvents.addCompletionCheckEvent() {
        addEvent(CompletionCheckEvent(0, true))
    }

    private fun SessionEvents.addCallbackEvent() {
        addEvent(EnrolmentCallbackEvent(0, "guid"))

        ErrorCallbackEvent.Reason.values().forEach {
            addEvent(ErrorCallbackEvent(0, it))
        }

        Tier.values().forEach {
            addEvent(IdentificationCallbackEvent(0, "session_id", listOf(CallbackComparisonScore(RANDOM_GUID, 0, it))))
        }

        addEvent(RefusalCallbackEvent(0, "reason", "other_text"))
        addEvent(VerificationCallbackEvent(0, CallbackComparisonScore(RANDOM_GUID, 0, Tier.TIER_1)))
        addEvent(ConfirmationCallbackEvent(0, true))
    }

    private fun SessionEvents.addCalloutEvent() {
        addEvent(ConfirmationCalloutEvent(0, "projectId", "selected_guid", "session_id"))
        addEvent(EnrolmentCalloutEvent(0, "project_id", "user_id", "module_id", "metadata"))
        addEvent(IdentificationCalloutEvent(0, "project_id", "user_id", "module_id", "metadata"))
        addEvent(VerificationCalloutEvent(0, "project_id", "user_id", "module_id", "verify_guid","metadata"))
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
