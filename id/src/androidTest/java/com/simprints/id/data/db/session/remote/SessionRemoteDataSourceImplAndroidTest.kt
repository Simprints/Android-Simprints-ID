package com.simprints.id.data.db.session.remote

import android.net.NetworkInfo
import com.google.common.truth.Truth.assertThat
import com.simprints.core.network.SimApiClientFactory
import com.simprints.core.tools.EncodingUtils
import com.simprints.id.commontesttools.PeopleGeneratorUtils
import com.simprints.id.commontesttools.sessionEvents.createFakeClosedSession
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.person.domain.FingerIdentifier
import com.simprints.id.data.db.session.SessionRepositoryImpl
import com.simprints.id.data.db.session.domain.models.events.*
import com.simprints.id.data.db.session.domain.models.events.callback.*
import com.simprints.id.data.db.session.domain.models.events.callout.ConfirmationCalloutEvent
import com.simprints.id.data.db.session.domain.models.events.callout.EnrolmentCalloutEvent
import com.simprints.id.data.db.session.domain.models.events.callout.IdentificationCalloutEvent
import com.simprints.id.data.db.session.domain.models.events.callout.VerificationCalloutEvent
import com.simprints.id.data.db.session.domain.models.session.SessionEvents
import com.simprints.id.domain.moduleapi.app.responses.entities.Tier
import com.simprints.id.testtools.testingapi.TestProjectRule
import com.simprints.id.testtools.testingapi.models.TestProject
import com.simprints.id.testtools.testingapi.remote.RemoteTestingManager
import com.simprints.id.tools.TimeHelperImpl
import com.simprints.id.tools.utils.SimNetworkUtils
import com.simprints.testtools.android.waitOnSystem
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*

class SessionRemoteDataSourceImplAndroidTest {

    companion object {
        const val SIGNED_ID_USER = "some_signed_user"
        const val CLOUD_ASYNC_SESSION_CREATION_TIMEOUT = 5000L
        val RANDOM_GUID = UUID.randomUUID().toString()
    }

    private val remoteTestingManager: RemoteTestingManager = RemoteTestingManager.create()
    private val timeHelper = TimeHelperImpl()

    @get:Rule
    val testProjectRule = TestProjectRule()
    private lateinit var testProject: TestProject

    lateinit var sessionRemoteDataSource: SessionRemoteDataSource
    @MockK var remoteDbManager = mockk<RemoteDbManager>()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        testProject = testProjectRule.testProject

        val firebaseTestToken = remoteTestingManager.generateFirebaseToken(testProject.id, SIGNED_ID_USER)
        coEvery { remoteDbManager.getCurrentToken() } returns firebaseTestToken.token
        sessionRemoteDataSource = SessionRemoteDataSourceImpl(remoteDbManager, SimApiClientFactory("some_device"))
    }

    @Test
    fun closeSessions_shouldGetUploaded() {
        runBlocking {
            val nSession = SessionRepositoryImpl.SESSION_BATCH_SIZE + 1
            val sessions = createClosedSessions(nSession)

            sessions.forEach {
                it.addAlertScreenEvents()
            }

            executeUpload(sessions)

            waitOnSystem(CLOUD_ASYNC_SESSION_CREATION_TIMEOUT)

            val response = RemoteTestingManager.create().getSessionCount(testProject.id)
            assertThat(response.count).isEqualTo(nSession)
        }
    }

    @Test
    fun closeSession_withAllEvents_shouldGetUploaded() {
        runBlocking {
            val session = createClosedSessions(1).first().apply {
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
            }

            executeUpload(mutableListOf(session))

            waitOnSystem(CLOUD_ASYNC_SESSION_CREATION_TIMEOUT)

            val response = RemoteTestingManager.create().getSessionCount(testProject.id)
            assertThat(response.count).isEqualTo(1)
        }
    }


    private suspend fun executeUpload(sessions: MutableList<SessionEvents>) {
        sessionRemoteDataSource.uploadSessions(testProject.id, sessions)
    }

    private fun createClosedSessions(nSessions: Int) =
        mutableListOf<SessionEvents>().apply {
            repeat(nSessions) { this.add(createFakeClosedSession(timeHelper, testProject.id)) }
        }

    private fun SessionEvents.addAlertScreenEvents() {
        AlertScreenEvent.AlertScreenEventType.values().forEach {
            events.add(AlertScreenEvent(0, it))
        }
    }

    private fun SessionEvents.addArtificialTerminationEvent() {
        ArtificialTerminationEvent.Reason.values().forEach {
            events.add(ArtificialTerminationEvent(0, it))
        }
    }

    private fun SessionEvents.addAuthenticationEvent() {
        AuthenticationEvent.Result.values().forEach {
            events.add(AuthenticationEvent(0, 0, AuthenticationEvent.UserInfo("some_project", "user_id"), it))
        }
    }

    private fun SessionEvents.addAuthorizationEvent() {
        AuthorizationEvent.Result.values().forEach {
            events.add(AuthorizationEvent(0, it, AuthorizationEvent.UserInfo("some_project", "user_id")))
        }
    }

    private fun SessionEvents.addCandidateReadEvent() {
        CandidateReadEvent.LocalResult.values().forEach { local ->
            CandidateReadEvent.RemoteResult.values().forEach { remote ->
                events.add(CandidateReadEvent(0, 0, RANDOM_GUID, local, remote))
            }
        }
    }

    private fun SessionEvents.addConnectivitySnapshotEvent() {
        events.add(ConnectivitySnapshotEvent(0, "Unknown", listOf(SimNetworkUtils.Connection("connection", NetworkInfo.DetailedState.CONNECTED))))
    }

    private fun SessionEvents.addConsentEvent() {
        ConsentEvent.Type.values().forEach { type ->
            ConsentEvent.Result.values().forEach { result ->
                events.add(ConsentEvent(0, 0, type, result))
            }
        }
    }

    private fun SessionEvents.addEnrolmentEvent() {
        events.add(EnrolmentEvent(0, RANDOM_GUID))
    }

    private fun SessionEvents.addFingerprintCaptureEvent() {
        FingerprintCaptureEvent.Result.values().forEach { result ->
            FingerIdentifier.values().forEach { fingerIdentifier ->
                val fakeTemplate = EncodingUtils.byteArrayToBase64(PeopleGeneratorUtils.getRandomFingerprintSample().template)
                events.add(FingerprintCaptureEvent(0, 0, fingerIdentifier, 0, result,
                    FingerprintCaptureEvent.Fingerprint(fingerIdentifier, 0, fakeTemplate)))
            }
        }
    }

    private fun SessionEvents.addGuidSelectionEvent() {
        events.add(GuidSelectionEvent(0, RANDOM_GUID))
    }

    private fun SessionEvents.addIntentParsingEvent() {
        IntentParsingEvent.IntegrationInfo.values().forEach {
            events.add(IntentParsingEvent(0, it))
        }
    }

    private fun SessionEvents.addInvalidIntentEvent() {
        events.add(InvalidIntentEvent(0, "some_action", emptyMap()))
    }

    private fun SessionEvents.addOneToManyMatchEvent() {
        OneToManyMatchEvent.MatchPoolType.values().forEach {
            events.add(OneToManyMatchEvent(0, 0, OneToManyMatchEvent.MatchPool(it, 0), emptyList()))
        }
    }

    private fun SessionEvents.addOneToOneMatchEvent() {
        events.add(OneToOneMatchEvent(0, 0, RANDOM_GUID, MatchEntry(RANDOM_GUID, 0F)))
    }

    private fun SessionEvents.addPersonCreationEvent() {
        events.add(PersonCreationEvent(0, listOf(RANDOM_GUID, RANDOM_GUID)))
    }

    private fun SessionEvents.addRefusalEvent() {
        RefusalEvent.Answer.values().forEach {
            events.add(RefusalEvent(0, 0, it, "other_text"))
        }
    }

    private fun SessionEvents.addScannerConnectionEvent() {
        events.add(ScannerConnectionEvent(0, ScannerConnectionEvent.ScannerInfo("scanner_id", "macAddress", "hardware")))
    }

    private fun SessionEvents.addSuspiciousIntentEvent() {
        events.add(SuspiciousIntentEvent(0, mapOf("some_extra_key" to "value")))
    }

    private fun SessionEvents.addCompletionCheckEvent() {
        events.add(CompletionCheckEvent(0, true))
    }

    private fun SessionEvents.addCallbackEvent() {
        with(events) {
            add(EnrolmentCallbackEvent(0, RANDOM_GUID))

            ErrorCallbackEvent.Reason.values().forEach {
                add(ErrorCallbackEvent(0, it))
            }

            Tier.values().forEach {
                add(IdentificationCallbackEvent(0, RANDOM_GUID, listOf(CallbackComparisonScore(RANDOM_GUID, 0, it))))
            }

            add(RefusalCallbackEvent(0, "reason", "other_text"))
            add(VerificationCallbackEvent(0, CallbackComparisonScore(RANDOM_GUID, 0, Tier.TIER_1)))
            add(ConfirmationCallbackEvent(0, true))
        }
    }

    private fun SessionEvents.addCalloutEvent() {
        with(events) {
            add(EnrolmentCalloutEvent(1, "project_id", "user_id", "module_id", "metadata"))
            add(ConfirmationCalloutEvent(10, "projectId", RANDOM_GUID, RANDOM_GUID))
            add(IdentificationCalloutEvent(0, "project_id", "user_id", "module_id", "metadata"))
            add(VerificationCalloutEvent(2, "project_id", "user_id", "module_id", RANDOM_GUID, "metadata"))
        }
    }


}
