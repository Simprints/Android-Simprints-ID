package com.simprints.id.data.db.session.remote

import android.net.NetworkInfo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.EncodingUtils
import com.simprints.core.tools.utils.randomUUID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.commontesttools.SubjectsGeneratorUtils
import com.simprints.id.commontesttools.sessionEvents.createFakeClosedSession
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.session.SessionRepositoryImpl
import com.simprints.id.data.db.session.domain.models.events.*
import com.simprints.id.data.db.session.domain.models.events.ScannerConnectionEvent.ScannerGeneration
import com.simprints.id.data.db.session.domain.models.events.callback.*
import com.simprints.id.data.db.session.domain.models.events.callout.*
import com.simprints.id.data.db.session.domain.models.session.SessionEvents
import com.simprints.id.data.db.subject.domain.FingerIdentifier
import com.simprints.id.domain.moduleapi.app.responses.entities.Tier
import com.simprints.id.network.BaseUrlProvider
import com.simprints.id.network.NetworkConstants.Companion.DEFAULT_BASE_URL
import com.simprints.id.network.SimApiClientFactoryImpl
import com.simprints.id.testtools.testingapi.TestProjectRule
import com.simprints.id.testtools.testingapi.models.TestProject
import com.simprints.id.testtools.testingapi.remote.RemoteTestingManager
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.utils.SimNetworkUtils
import com.simprints.testtools.android.waitOnSystem
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class SessionRemoteDataSourceImplAndroidTest {

    companion object {
        const val SIGNED_ID_USER = "some_signed_user"
        const val CLOUD_ASYNC_SESSION_CREATION_TIMEOUT = 5000L
        const val DEFAULT_TIME = 1000L
        val RANDOM_GUID = UUID.randomUUID().toString()
    }

    private val remoteTestingManager: RemoteTestingManager = RemoteTestingManager.create()
    @MockK lateinit var timeHelper: TimeHelper

    @get:Rule
    val testProjectRule = TestProjectRule()
    private lateinit var testProject: TestProject

    private lateinit var sessionRemoteDataSource: SessionRemoteDataSource

    @MockK
    var remoteDbManager = mockk<RemoteDbManager>()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        testProject = testProjectRule.testProject

        val firebaseTestToken = remoteTestingManager.generateFirebaseToken(testProject.id, SIGNED_ID_USER)
        coEvery { remoteDbManager.getCurrentToken() } returns firebaseTestToken.token
        val mockBaseUrlProvider = mockk<BaseUrlProvider>()
        every { mockBaseUrlProvider.getApiBaseUrl() } returns DEFAULT_BASE_URL
        sessionRemoteDataSource = SessionRemoteDataSourceImpl(
            SimApiClientFactoryImpl(mockBaseUrlProvider, "some_device", remoteDbManager)
        )
        every { timeHelper.nowMinus(any(), any()) } returns 100
        every { timeHelper.now() } returns 100
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
                addVero2InfoSnapshotEvents()
                addScannerFirmwareUpdateEvent()
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
        AlertScreenEvent.AlertScreenEventType.values()
            .forEach {
                addEvent(AlertScreenEvent(DEFAULT_TIME, it))
            }
    }

    private fun SessionEvents.addArtificialTerminationEvent() {
        ArtificialTerminationEvent.Reason.values().forEach {
            addEvent(ArtificialTerminationEvent(DEFAULT_TIME, it))
        }
    }

    private fun SessionEvents.addAuthenticationEvent() {
        AuthenticationEvent.Result.values().forEach {
            addEvent(AuthenticationEvent(DEFAULT_TIME, DEFAULT_TIME, AuthenticationEvent.UserInfo("some_project", "user_id"), it))
        }
    }

    private fun SessionEvents.addAuthorizationEvent() {
        AuthorizationEvent.Result.values().forEach {
            addEvent(AuthorizationEvent(DEFAULT_TIME, it, AuthorizationEvent.UserInfo("some_project", "user_id")))
        }
    }

    private fun SessionEvents.addCandidateReadEvent() {
        CandidateReadEvent.LocalResult.values().forEach { local ->
            CandidateReadEvent.RemoteResult.values().forEach { remote ->
                addEvent(CandidateReadEvent(DEFAULT_TIME, DEFAULT_TIME, RANDOM_GUID, local, remote))
            }
        }
    }

    private fun SessionEvents.addConnectivitySnapshotEvent() {
        addEvent(
            ConnectivitySnapshotEvent(
                DEFAULT_TIME,
                "Unknown",
                listOf(SimNetworkUtils.Connection("connection", NetworkInfo.DetailedState.CONNECTED))
            )
        )
    }

    private fun SessionEvents.addConsentEvent() {
        ConsentEvent.Type.values().forEach { type ->
            ConsentEvent.Result.values().forEach { result ->
                addEvent(ConsentEvent(DEFAULT_TIME, DEFAULT_TIME, type, result))
            }
        }
    }

    private fun SessionEvents.addEnrolmentEvent() {
        addEvent(EnrolmentEvent(DEFAULT_TIME, RANDOM_GUID))
    }

    private fun SessionEvents.addFingerprintCaptureEvent() {
        FingerprintCaptureEvent.Result.values().forEach { result ->
            FingerIdentifier.values().forEach { fingerIdentifier ->
                val fakeTemplate = EncodingUtils.byteArrayToBase64(
                    SubjectsGeneratorUtils.getRandomFingerprintSample().template
                )

                val fingerprint = FingerprintCaptureEvent.Fingerprint(
                    fingerIdentifier,
                    0,
                    fakeTemplate
                )

                val event = FingerprintCaptureEvent(
                    DEFAULT_TIME,
                    DEFAULT_TIME,
                    fingerIdentifier,
                    0,
                    result,
                    fingerprint,
                    randomUUID()
                )

                addEvent(event)
            }
        }
    }

    private fun SessionEvents.addGuidSelectionEvent() {
        addEvent(GuidSelectionEvent(DEFAULT_TIME, RANDOM_GUID))
    }

    private fun SessionEvents.addIntentParsingEvent() {
        IntentParsingEvent.IntegrationInfo.values().forEach {
            addEvent(IntentParsingEvent(DEFAULT_TIME, it))
        }
    }

    private fun SessionEvents.addInvalidIntentEvent() {
        addEvent(InvalidIntentEvent(DEFAULT_TIME, "some_action", mapOf("wrong_field" to "wrong_value")))
    }

    private fun SessionEvents.addOneToManyMatchEvent() {
        OneToManyMatchEvent.MatchPoolType.values().forEach {
            addEvent(
                OneToManyMatchEvent(
                    DEFAULT_TIME,
                    DEFAULT_TIME,
                    OneToManyMatchEvent.MatchPool(it, 0),
                    Matcher.SIM_AFIS,
                    emptyList()
                )
            )
        }
    }

    private fun SessionEvents.addOneToOneMatchEvent() {
        addEvent(
            OneToOneMatchEvent(
                DEFAULT_TIME,
                DEFAULT_TIME,
                RANDOM_GUID,
                Matcher.SIM_AFIS,
                MatchEntry(RANDOM_GUID, 0F)
            )
        )
    }

    private fun SessionEvents.addPersonCreationEvent() {
        addEvent(PersonCreationEvent(DEFAULT_TIME, listOf(RANDOM_GUID, RANDOM_GUID), null))
    }

    private fun SessionEvents.addRefusalEvent() {
        RefusalEvent.Answer.values().forEach {
            addEvent(RefusalEvent(DEFAULT_TIME, DEFAULT_TIME, it, "other_text"))
        }
    }

    private fun SessionEvents.addScannerConnectionEvent() {
        addEvent(
            ScannerConnectionEvent(
                DEFAULT_TIME,
                ScannerConnectionEvent.ScannerInfo(
                    "scanner_id", "macAddress",
                    ScannerGeneration.VERO_2, "hardware"
                )
            )
        )
    }

    private fun SessionEvents.addVero2InfoSnapshotEvents() {
        addEvent(
            Vero2InfoSnapshotEvent(
                DEFAULT_TIME,
                Vero2InfoSnapshotEvent.Vero2Version(
                    Int.MAX_VALUE.toLong() + 1, "1.23",
                    "api", "stmApp", "stmApi", "un20App", "un20Api"
                ),
                Vero2InfoSnapshotEvent.BatteryInfo(70, 15, 1, 37)
            )
        )
    }

    private fun SessionEvents.addScannerFirmwareUpdateEvent() {
        addEvent(
            ScannerFirmwareUpdateEvent(
                DEFAULT_TIME, DEFAULT_TIME, "stm",
                "targetApp", "failureReason"
            )
        )
    }

    private fun SessionEvents.addSuspiciousIntentEvent() {
        addEvent(SuspiciousIntentEvent(DEFAULT_TIME, mapOf("some_extra_key" to "value")))
    }

    private fun SessionEvents.addCompletionCheckEvent() {
        addEvent(CompletionCheckEvent(DEFAULT_TIME, true))
    }

    private fun SessionEvents.addCallbackEvent() {
        addEvent(EnrolmentCallbackEvent(DEFAULT_TIME, RANDOM_GUID))

        ErrorCallbackEvent.Reason.values().forEach {
            addEvent(ErrorCallbackEvent(DEFAULT_TIME, it))
        }

        Tier.values().forEach {
            addEvent(IdentificationCallbackEvent(DEFAULT_TIME, RANDOM_GUID, listOf(CallbackComparisonScore(RANDOM_GUID, 0, it))))
        }

        addEvent(RefusalCallbackEvent(DEFAULT_TIME, "reason", "other_text"))
        addEvent(VerificationCallbackEvent(DEFAULT_TIME, CallbackComparisonScore(RANDOM_GUID, 0, Tier.TIER_1)))
        addEvent(ConfirmationCallbackEvent(DEFAULT_TIME, true))
    }

    private fun SessionEvents.addCalloutEvent() {
        addEvent(EnrolmentCalloutEvent(DEFAULT_TIME, DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, "metadata"))
        addEvent(ConfirmationCalloutEvent(DEFAULT_TIME, DEFAULT_PROJECT_ID, RANDOM_GUID, RANDOM_GUID))
        addEvent(IdentificationCalloutEvent(DEFAULT_TIME, DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, "metadata"))
        addEvent(VerificationCalloutEvent(DEFAULT_TIME, DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, RANDOM_GUID, "metadata"))
        addEvent(EnrolmentLastBiometricsCalloutEvent(DEFAULT_TIME, DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, "metadata", RANDOM_GUID))
    }
}
