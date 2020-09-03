//package com.simprints.id.data.db.event.remote
//
//import android.net.NetworkInfo
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import com.google.common.truth.Truth.assertThat
//import com.simprints.core.tools.EncodingUtils
//import com.simprints.core.tools.utils.randomUUID
//import com.simprints.id.commontesttools.SubjectsGeneratorUtils
//import com.simprints.id.commontesttools.sessionEvents.createFakeClosedSession
//import com.simprints.id.data.db.common.RemoteDbManager
//import com.simprints.id.data.db.event.EventRepositoryImpl
//import com.simprints.id.data.db.event.domain.events.*
//import com.simprints.id.data.db.event.domain.events.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType
//import com.simprints.id.data.db.event.domain.events.ArtificialTerminationEvent.ArtificialTerminationPayload
//import com.simprints.id.data.db.event.domain.events.AuthenticationEvent.AuthenticationPayload
//import com.simprints.id.data.db.event.domain.events.AuthorizationEvent.AuthorizationPayload
//import com.simprints.id.data.db.event.domain.events.CandidateReadEvent.CandidateReadPayload
//import com.simprints.id.data.db.event.domain.events.ConsentEvent.ConsentPayload
//import com.simprints.id.data.db.event.domain.events.FingerprintCaptureEvent.FingerprintCapturePayload
//import com.simprints.id.data.db.event.domain.events.IntentParsingEvent.IntentParsingPayload
//import com.simprints.id.data.db.event.domain.events.OneToManyMatchEvent.OneToManyMatchPayload
//import com.simprints.id.data.db.event.domain.events.RefusalEvent.RefusalPayload
//import com.simprints.id.data.db.event.domain.events.ScannerConnectionEvent.ScannerConnectionPayload
//import com.simprints.id.data.db.event.domain.events.ScannerConnectionEvent.ScannerConnectionPayload.ScannerGeneration
//import com.simprints.id.data.db.event.domain.events.Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload
//import com.simprints.id.data.db.event.domain.events.callback.*
//import com.simprints.id.data.db.event.domain.events.callback.ErrorCallbackEvent.ErrorCallbackPayload
//import com.simprints.id.data.db.event.domain.events.callout.*
//import com.simprints.id.data.db.event.domain.events.session.SessionCaptureEvent
//import com.simprints.id.data.db.subject.domain.FingerIdentifier
//import com.simprints.id.domain.moduleapi.app.responses.entities.Tier
//import com.simprints.id.network.BaseUrlProvider
//import com.simprints.id.network.NetworkConstants.Companion.DEFAULT_BASE_URL
//import com.simprints.id.network.SimApiClientFactoryImpl
//import com.simprints.id.testtools.testingapi.TestProjectRule
//import com.simprints.id.testtools.testingapi.models.TestProject
//import com.simprints.id.testtools.testingapi.remote.RemoteTestingManager
//import com.simprints.id.tools.time.TimeHelperImpl
//import com.simprints.id.tools.utils.SimNetworkUtils
//import com.simprints.testtools.android.waitOnSystem
//import io.mockk.MockKAnnotations
//import io.mockk.coEvery
//import io.mockk.every
//import io.mockk.impl.annotations.MockK
//import io.mockk.mockk
//import kotlinx.coroutines.runBlocking
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import org.junit.runner.RunWith
//import java.util.*
//
// StopShip: it will get fixed when the event remote data source is implemented.
//@RunWith(AndroidJUnit4::class)
//class SessionRemoteDataSourceImplAndroidTest {
//
//    companion object {
//        const val SIGNED_ID_USER = "some_signed_user"
//        const val CLOUD_ASYNC_SESSION_CREATION_TIMEOUT = 5000L
//        val RANDOM_GUID = UUID.randomUUID().toString()
//    }
//
//    private val remoteTestingManager: RemoteTestingManager = RemoteTestingManager.create()
//    private val timeHelper = TimeHelperImpl()
//
//    @get:Rule
//    val testProjectRule = TestProjectRule()
//    private lateinit var testProject: TestProject
//
//    private lateinit var sessionRemoteDataSource: SessionRemoteDataSource
//
//    @MockK
//    var remoteDbManager = mockk<RemoteDbManager>()
//
//    @Before
//    fun setUp() {
//        MockKAnnotations.init(this)
//        testProject = testProjectRule.testProject
//
//        val firebaseTestToken = remoteTestingManager.generateFirebaseToken(testProject.id, SIGNED_ID_USER)
//        coEvery { remoteDbManager.getCurrentToken() } returns firebaseTestToken.token
//        val mockBaseUrlProvider = mockk<BaseUrlProvider>()
//        every { mockBaseUrlProvider.getApiBaseUrl() } returns DEFAULT_BASE_URL
//        sessionRemoteDataSource = SessionRemoteDataSourceImpl(
//            SimApiClientFactoryImpl(mockBaseUrlProvider, "some_device", remoteDbManager)
//        )
//    }
//
//    @Test
//    fun closeSessions_shouldGetUploaded() {
//        runBlocking {
//            val nSession = EventRepositoryImpl.SESSION_BATCH_SIZE + 1
//            val sessions = createClosedSessions(nSession)
//
//            val events = mutableListOf<Event>().apply {
//                addAlertScreenEvents()
//            }
//
//            executeUpload(events)
//
//            waitOnSystem(CLOUD_ASYNC_SESSION_CREATION_TIMEOUT)
//
//            val response = RemoteTestingManager.create().getSessionCount(testProject.id)
//            assertThat(response.count).isEqualTo(nSession)
//        }
//    }
//
//    @Test
//    fun closeSession_withAllEvents_shouldGetUploaded() {
//        runBlocking {
//            val events = mutableListOf<Event>().apply {
//                addAlertScreenEvents()
//                addArtificialTerminationEvent()
//                addAuthenticationEvent()
//                addAuthorizationEvent()
//                addCandidateReadEvent()
//                addConnectivitySnapshotEvent()
//                addConsentEvent()
//                addEnrolmentEvent()
//                addFingerprintCaptureEvent()
//                addGuidSelectionEvent()
//                addIntentParsingEvent()
//                addInvalidIntentEvent()
//                addOneToOneMatchEvent()
//                addOneToManyMatchEvent()
//                addPersonCreationEvent()
//                addRefusalEvent()
//                addScannerConnectionEvent()
//                addVero2InfoSnapshotEvents()
//                addScannerFirmwareUpdateEvent()
//                addSuspiciousIntentEvent()
//                addCallbackEvent()
//                addCalloutEvent()
//                addCompletionCheckEvent()
//            }
//
//            executeUpload(events)
//
//            waitOnSystem(CLOUD_ASYNC_SESSION_CREATION_TIMEOUT)
//
//            val response = RemoteTestingManager.create().getSessionCount(testProject.id)
//            assertThat(response.count).isEqualTo(1)
//        }
//    }
//
//
//    private suspend fun executeUpload(events: List<Event>) {
//        sessionRemoteDataSource.uploadSessions(testProject.id, events)
//    }
//
//    private fun createClosedSessions(nSessions: Int) =
//        mutableListOf<SessionCaptureEvent>().apply {
//            repeat(nSessions) { this.add(createFakeClosedSession(timeHelper, testProject.id)) }
//        }
//
//    private fun MutableList<Event>.addAlertScreenEvents() {
//        AlertScreenEventType.values().forEach {
//            add(AlertScreenEvent(0, it))
//        }
//    }
//
//    private fun MutableList<Event>.addArtificialTerminationEvent() {
//        ArtificialTerminationPayload.Reason.values().forEach {
//            add(ArtificialTerminationEvent(0, it))
//        }
//    }
//
//    private fun MutableList<Event>.addAuthenticationEvent() {
//        AuthenticationPayload.Result.values().forEach {
//            add(AuthenticationEvent(0, 0, AuthenticationPayload.UserInfo("some_project", "user_id"), it))
//        }
//    }
//
//    private fun MutableList<Event>.addAuthorizationEvent() {
//        AuthorizationPayload.Result.values().forEach {
//            add(AuthorizationEvent(0, it, AuthorizationPayload.UserInfo("some_project", "user_id")))
//        }
//    }
//
//    private fun MutableList<Event>.addCandidateReadEvent() {
//        CandidateReadPayload.LocalResult.values().forEach { local ->
//            CandidateReadPayload.RemoteResult.values().forEach { remote ->
//                add(CandidateReadEvent(0, 0, RANDOM_GUID, local, remote))
//            }
//        }
//    }
//
//    private fun MutableList<Event>.addConnectivitySnapshotEvent() {
//        add(ConnectivitySnapshotEvent(0, "Unknown", listOf(SimNetworkUtils.Connection("connection", NetworkInfo.DetailedState.CONNECTED))))
//    }
//
//    private fun MutableList<Event>.addConsentEvent() {
//        ConsentPayload.Type.values().forEach { type ->
//            ConsentPayload.Result.values().forEach { result ->
//                add(ConsentEvent(0, 0, type, result))
//            }
//        }
//    }
//
//    private fun MutableList<Event>.addEnrolmentEvent() {
//        add(EnrolmentEvent(0, RANDOM_GUID))
//    }
//
//    private fun MutableList<Event>.addFingerprintCaptureEvent() {
//        FingerprintCapturePayload.Result.values().forEach { result ->
//            FingerIdentifier.values().forEach { fingerIdentifier ->
//                val fakeTemplate = EncodingUtils.byteArrayToBase64(
//                    SubjectsGeneratorUtils.getRandomFingerprintSample().template
//                )
//
//                val fingerprint = FingerprintCapturePayload.Fingerprint(
//                    fingerIdentifier,
//                    0,
//                    fakeTemplate
//                )
//
//                val event = FingerprintCaptureEvent(
//                    0,
//                    0,
//                    fingerIdentifier,
//                    0,
//                    result,
//                    fingerprint,
//                    randomUUID()
//                )
//
//                add(event)
//            }
//        }
//    }
//
//    private fun MutableList<Event>.addGuidSelectionEvent() {
//        add(GuidSelectionEvent(0, RANDOM_GUID))
//    }
//
//    private fun MutableList<Event>.addIntentParsingEvent() {
//        IntentParsingPayload.IntegrationInfo.values().forEach {
//            add(IntentParsingEvent(0, it))
//        }
//    }
//
//    private fun MutableList<Event>.addInvalidIntentEvent() {
//        add(InvalidIntentEvent(0, "some_action", emptyMap()))
//    }
//
//    private fun MutableList<Event>.addOneToManyMatchEvent() {
//        OneToManyMatchPayload.MatchPoolType.values().forEach {
//            add(OneToManyMatchEvent(0, 0, OneToManyMatchPayload.MatchPool(it, 0), emptyList()))
//        }
//    }
//
//    private fun MutableList<Event>.addOneToOneMatchEvent() {
//        add(OneToOneMatchEvent(0, 0, RANDOM_GUID, MatchEntry(RANDOM_GUID, 0F)))
//    }
//
//    private fun MutableList<Event>.addPersonCreationEvent() {
//        add(PersonCreationEvent(0, listOf(RANDOM_GUID, RANDOM_GUID)))
//    }
//
//    private fun MutableList<Event>.addRefusalEvent() {
//        RefusalPayload.Answer.values().forEach {
//            add(RefusalEvent(0, 0, it, "other_text"))
//        }
//    }
//
//    private fun MutableList<Event>.addScannerConnectionEvent() {
//        add(ScannerConnectionEvent(0,
//            ScannerConnectionPayload.ScannerInfo("scanner_id", "macAddress",
//                ScannerGeneration.VERO_2, "hardware")))
//    }
//
//    private fun MutableList<Event>.addVero2InfoSnapshotEvents() {
//        add(Vero2InfoSnapshotEvent(0,
//            Vero2InfoSnapshotPayload.Vero2Version(Int.MAX_VALUE.toLong() + 1, "1.23",
//                "api", "stmApp", "stmApi", "un20App", "un20Api"),
//            Vero2InfoSnapshotPayload.BatteryInfo(70, 15, 1, 37)))
//    }
//
//    private fun MutableList<Event>.addScannerFirmwareUpdateEvent() {
//        add(ScannerFirmwareUpdateEvent(0, 0, "stm",
//            "targetApp", "failureReason"))
//    }
//
//    private fun MutableList<Event>.addSuspiciousIntentEvent() {
//        add(SuspiciousIntentEvent(0, mapOf("some_extra_key" to "value")))
//    }
//
//    private fun MutableList<Event>.addCompletionCheckEvent() {
//        add(CompletionCheckEvent(0, true))
//    }
//
//    private fun MutableList<Event>.addCallbackEvent() {
//        add(EnrolmentCallbackEvent(0, RANDOM_GUID))
//
//        ErrorCallbackPayload.Reason.values().forEach {
//            add(ErrorCallbackEvent(0, it))
//        }
//
//        Tier.values().forEach {
//            add(IdentificationCallbackEvent(0, RANDOM_GUID, listOf(CallbackComparisonScore(RANDOM_GUID, 0, it))))
//        }
//
//        add(RefusalCallbackEvent(0, "reason", "other_text"))
//        add(VerificationCallbackEvent(0, CallbackComparisonScore(RANDOM_GUID, 0, Tier.TIER_1)))
//        add(ConfirmationCallbackEvent(0, true))
//    }
//
//    private fun MutableList<Event>.addCalloutEvent() {
//        add(EnrolmentCalloutEvent(1, "project_id", "user_id", "module_id", "metadata"))
//        add(ConfirmationCalloutEvent(10, "projectId", RANDOM_GUID, RANDOM_GUID))
//        add(IdentificationCalloutEvent(0, "project_id", "user_id", "module_id", "metadata"))
//        add(VerificationCalloutEvent(2, "project_id", "user_id", "module_id", RANDOM_GUID, "metadata"))
//        add(EnrolmentLastBiometricsCalloutEvent(2, "project_id", "user_id", "module_id", "metadata", RANDOM_GUID))
//    }
//}
