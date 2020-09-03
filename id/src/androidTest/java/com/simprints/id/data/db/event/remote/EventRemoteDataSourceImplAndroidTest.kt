package com.simprints.id.data.db.event.remote

import android.net.NetworkInfo
import android.os.Build
import android.os.Build.VERSION
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.core.tools.EncodingUtils
import com.simprints.core.tools.extentions.safeSealedWhens
import com.simprints.core.tools.utils.randomUUID
import com.simprints.id.commontesttools.DefaultTestConstants
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.commontesttools.SubjectsGeneratorUtils
import com.simprints.id.commontesttools.events.CREATED_AT
import com.simprints.id.commontesttools.events.buildFakeBiometricReferences
import com.simprints.id.commontesttools.events.createBiometricReferences
import com.simprints.id.commontesttools.events.eventLabels
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.event.domain.models.*
import com.simprints.id.data.db.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload
import com.simprints.id.data.db.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result
import com.simprints.id.data.db.event.domain.models.AuthenticationEvent.AuthenticationPayload.UserInfo
import com.simprints.id.data.db.event.domain.models.AuthorizationEvent.AuthorizationPayload
import com.simprints.id.data.db.event.domain.models.CandidateReadEvent.CandidateReadPayload
import com.simprints.id.data.db.event.domain.models.ConsentEvent.ConsentPayload
import com.simprints.id.data.db.event.domain.models.FingerprintCaptureEvent.FingerprintCapturePayload
import com.simprints.id.data.db.event.domain.models.IntentParsingEvent.IntentParsingPayload
import com.simprints.id.data.db.event.domain.models.OneToManyMatchEvent.OneToManyMatchPayload
import com.simprints.id.data.db.event.domain.models.RefusalEvent.RefusalPayload
import com.simprints.id.data.db.event.domain.models.ScannerConnectionEvent.ScannerConnectionPayload
import com.simprints.id.data.db.event.domain.models.ScannerConnectionEvent.ScannerConnectionPayload.ScannerGeneration
import com.simprints.id.data.db.event.domain.models.Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload
import com.simprints.id.data.db.event.domain.models.callback.*
import com.simprints.id.data.db.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload
import com.simprints.id.data.db.event.domain.models.callout.*
import com.simprints.id.data.db.event.domain.models.face.*
import com.simprints.id.data.db.event.domain.models.face.FaceCaptureConfirmationEvent.FaceCaptureConfirmationPayload
import com.simprints.id.data.db.event.domain.models.face.FaceCaptureEvent.FaceCapturePayload
import com.simprints.id.data.db.event.domain.models.session.DatabaseInfo
import com.simprints.id.data.db.event.domain.models.session.Device
import com.simprints.id.data.db.event.domain.models.session.Location
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordDeletionEvent
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordMoveEvent
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordMoveEvent.EnrolmentRecordCreationInMove
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordMoveEvent.EnrolmentRecordDeletionInMove
import com.simprints.id.data.db.event.remote.models.ApiEventPayloadType
import com.simprints.id.data.db.event.remote.models.ApiEventPayloadType.*
import com.simprints.id.data.db.subject.domain.FingerIdentifier
import com.simprints.id.domain.modality.Modes.FACE
import com.simprints.id.domain.modality.Modes.FINGERPRINT
import com.simprints.id.domain.moduleapi.app.responses.entities.Tier
import com.simprints.id.domain.moduleapi.app.responses.entities.Tier.TIER_1
import com.simprints.id.network.BaseUrlProvider
import com.simprints.id.network.NetworkConstants.Companion.DEFAULT_BASE_URL
import com.simprints.id.network.SimApiClientFactoryImpl
import com.simprints.id.testtools.testingapi.TestProjectRule
import com.simprints.id.testtools.testingapi.models.TestProject
import com.simprints.id.testtools.testingapi.remote.RemoteTestingManager
import com.simprints.id.tools.time.TimeHelper
import com.simprints.id.tools.utils.SimNetworkUtils
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import okhttp3.internal.toImmutableList
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventRemoteDataSourceImplAndroidTest {

    companion object {
        const val SIGNED_ID_USER = "some_signed_user"
        const val CLOUD_ASYNC_SESSION_CREATION_TIMEOUT = 5000L
        const val DEFAULT_TIME = 1000L
    }

    private val remoteTestingManager: RemoteTestingManager = RemoteTestingManager.create()
    @MockK lateinit var timeHelper: TimeHelper

    @get:Rule
    val testProjectRule = TestProjectRule()
    private lateinit var testProject: TestProject

    private lateinit var eventRemoteDataSource: EventRemoteDataSource

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
        eventRemoteDataSource = EventRemoteDataSourceImpl(
            SimApiClientFactoryImpl(mockBaseUrlProvider, "some_device", remoteDbManager)
        )
        every { timeHelper.nowMinus(any(), any()) } returns 100
        every { timeHelper.now() } returns 100
    }

    @Test
    fun aSessionWithAllEvents_shouldGetUploaded() {
        runBlocking {
            val events = mutableListOf<Event>().apply {
                addAlertScreenEvents()
                addArtificialTerminationEvent()
                addAuthenticationEvent()
                addAuthorizationEvent()
                addCandidateReadEvent()
                addConnectivitySnapshotEvent()
                addConsentEvent()
                addEnrolmentEvent()
                addFingerprintCaptureEvent()
                addFaceCaptureEvent()
                addFaceCaptureConfirmationEvent()
                addFaceCaptureRetryEvent()
                addFaceFallbackCaptureEvent()
                addFaceOnboardingCompleteEvent()
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
                addSessionCaptureEvent()
                addSuspiciousIntentEvent()
                addCallbackEvent()
                addCalloutEvent()
                addCompletionCheckEvent()
            }

            executeUpload(events)
        }
    }

    private suspend fun executeUpload(events: MutableList<Event>) {
        eventRemoteDataSource.post(testProject.id, events.toImmutableList())
    }

    private fun MutableList<Event>.addAlertScreenEvents() {
        AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.values()
            .forEach {
                add(AlertScreenEvent(DEFAULT_TIME, it, eventLabels))
            }
    }

    private fun MutableList<Event>.addArtificialTerminationEvent() {
        ArtificialTerminationPayload.Reason.values().forEach {
            add(ArtificialTerminationEvent(DEFAULT_TIME, it, eventLabels))
        }
    }

    private fun MutableList<Event>.addAuthenticationEvent() {
        Result.values().forEach {
            add(AuthenticationEvent(DEFAULT_TIME, DEFAULT_TIME, UserInfo("some_project", DEFAULT_USER_ID), it, eventLabels))
        }
    }

    private fun MutableList<Event>.addAuthorizationEvent() {
        AuthorizationPayload.AuthorizationResult.values().forEach {
            add(AuthorizationEvent(DEFAULT_TIME, it, AuthorizationPayload.UserInfo("some_project", DEFAULT_USER_ID), eventLabels))
        }
    }

    private fun MutableList<Event>.addCandidateReadEvent() {
        CandidateReadPayload.LocalResult.values().forEach { local ->
            CandidateReadPayload.RemoteResult.values().forEach { remote ->
                add(CandidateReadEvent(DEFAULT_TIME, DEFAULT_TIME, randomUUID(), local, remote, eventLabels))
            }
        }
    }

    private fun MutableList<Event>.addConnectivitySnapshotEvent() {
        add(
            ConnectivitySnapshotEvent(
                DEFAULT_TIME,
                "Unknown",
                listOf(SimNetworkUtils.Connection("connection", NetworkInfo.DetailedState.CONNECTED)), eventLabels
            )
        )
    }

    private fun MutableList<Event>.addConsentEvent() {
        ConsentPayload.Type.values().forEach { type ->
            ConsentPayload.Result.values().forEach { result ->
                add(ConsentEvent(DEFAULT_TIME, DEFAULT_TIME, type, result, eventLabels))
            }
        }
    }

    private fun MutableList<Event>.addEnrolmentEvent() {
        add(EnrolmentEvent(DEFAULT_TIME, randomUUID(), eventLabels))
    }

    private fun MutableList<Event>.addFingerprintCaptureEvent() {
        FingerprintCapturePayload.Result.values().forEach { result ->
            FingerIdentifier.values().forEach { fingerIdentifier ->
                val fakeTemplate = EncodingUtils.byteArrayToBase64(
                    SubjectsGeneratorUtils.getRandomFingerprintSample().template
                )

                val fingerprint = FingerprintCapturePayload.Fingerprint(
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
                    randomUUID(),
                    eventLabels
                )

                add(event)
            }
        }
    }

    private fun MutableList<Event>.addFaceCaptureEvent() {
        FaceCapturePayload.Result.values().forEachIndexed { index, result ->
            val template = EncodingUtils.byteArrayToBase64(
                SubjectsGeneratorUtils.getRandomFaceSample().template
            )

            val face = FaceCapturePayload.Face(30f, 40f, 100f, template)

            val event = FaceCaptureEvent(
                DEFAULT_TIME,
                DEFAULT_TIME + 100,
                index + 1,
                0f,
                result,
                false,
                face,
                eventLabels
            )

            add(event)
        }
    }

    private fun MutableList<Event>.addFaceCaptureConfirmationEvent() {
        FaceCaptureConfirmationPayload.Result.values().forEach { result ->
            val event = FaceCaptureConfirmationEvent(
                DEFAULT_TIME,
                DEFAULT_TIME + 100,
                result,
                eventLabels
            )

            add(event)
        }
    }

    private fun MutableList<Event>.addFaceCaptureRetryEvent() {
        val event = FaceCaptureRetryEvent(DEFAULT_TIME, DEFAULT_TIME + 100, eventLabels)
        add(event)
    }

    private fun MutableList<Event>.addFaceFallbackCaptureEvent() {
        val event = FaceFallbackCaptureEvent(DEFAULT_TIME, DEFAULT_TIME + 100, eventLabels)
        add(event)
    }

    private fun MutableList<Event>.addFaceOnboardingCompleteEvent() {
        val event = FaceOnboardingCompleteEvent(DEFAULT_TIME, DEFAULT_TIME + 100, eventLabels)
        add(event)
    }

    private fun MutableList<Event>.addGuidSelectionEvent() {
        add(GuidSelectionEvent(DEFAULT_TIME, randomUUID(), eventLabels))
    }

    private fun MutableList<Event>.addIntentParsingEvent() {
        IntentParsingPayload.IntegrationInfo.values().forEach {
            add(IntentParsingEvent(DEFAULT_TIME, it, eventLabels))
        }
    }

    private fun MutableList<Event>.addInvalidIntentEvent() {
        add(InvalidIntentEvent(DEFAULT_TIME, "some_action", mapOf("wrong_field" to "wrong_value"), eventLabels))
    }

    private fun MutableList<Event>.addOneToManyMatchEvent() {
        OneToManyMatchPayload.MatchPoolType.values().forEach {
            add(
                OneToManyMatchEvent(
                    DEFAULT_TIME,
                    DEFAULT_TIME,
                    OneToManyMatchPayload.MatchPool(it, 0),
                    Matcher.SIM_AFIS,
                    emptyList(),
                    eventLabels
                )
            )
        }
    }

    private fun MutableList<Event>.addOneToOneMatchEvent() {
        add(
            OneToOneMatchEvent(
                DEFAULT_TIME,
                DEFAULT_TIME,
                randomUUID(),
                Matcher.SIM_AFIS,
                MatchEntry(randomUUID(), 0F),
                eventLabels
            )
        )
    }

    private fun MutableList<Event>.addPersonCreationEvent() {
        add(PersonCreationEvent(DEFAULT_TIME, listOf(randomUUID(), randomUUID()), listOf(randomUUID()), eventLabels))
    }

    private fun MutableList<Event>.addRefusalEvent() {
        RefusalPayload.Answer.values().forEach {
            add(RefusalEvent(DEFAULT_TIME, DEFAULT_TIME, it, "other_text", eventLabels))
        }
    }

    private fun MutableList<Event>.addScannerConnectionEvent() {
        add(
            ScannerConnectionEvent(
                DEFAULT_TIME,
                ScannerConnectionPayload.ScannerInfo(
                    "scanner_id", "macAddress",
                    ScannerGeneration.VERO_2, "hardware"
                ),
                eventLabels
            )
        )
    }

    private fun MutableList<Event>.addVero2InfoSnapshotEvents() {
        add(
            Vero2InfoSnapshotEvent(
                DEFAULT_TIME,
                Vero2InfoSnapshotPayload.Vero2Version(
                    Int.MAX_VALUE.toLong() + 1, "1.23",
                    "api", "stmApp", "stmApi", "un20App", "un20Api"
                ),
                Vero2InfoSnapshotPayload.BatteryInfo(70, 15, 1, 37),
                eventLabels
            )
        )
    }

    private fun MutableList<Event>.addScannerFirmwareUpdateEvent() {
        add(
            ScannerFirmwareUpdateEvent(
                DEFAULT_TIME, DEFAULT_TIME, "stm",
                "targetApp", "failureReason",
                eventLabels
            )
        )
    }

    private fun MutableList<Event>.addSuspiciousIntentEvent() {
        add(SuspiciousIntentEvent(DEFAULT_TIME, mapOf("some_extra_key" to "value"), eventLabels))
    }

    private fun MutableList<Event>.addCompletionCheckEvent() {
        add(CompletionCheckEvent(DEFAULT_TIME, true, eventLabels))
    }

    private fun MutableList<Event>.addSessionCaptureEvent() {
        val deviceArg = Device(
            VERSION.SDK_INT.toString(),
            Build.MANUFACTURER + "_" + Build.MODEL,
            DefaultTestConstants.GUID1)

        add(SessionCaptureEvent(
            DEFAULT_PROJECT_ID,
            CREATED_AT,
            listOf(FINGERPRINT, FACE),
            "appVersionName",
            "libSimprintsVersionName",
            "EN",
            deviceArg,
            DatabaseInfo(0, 2),
            Location(0.0, 0.0),
            "analyticsId",
            labels = EventLabels(deviceId = DefaultTestConstants.GUID1, projectId = DefaultTestConstants.GUID1)
        ))
    }

    private fun MutableList<Event>.addEnrolmentRecordCreation() {
        add(EnrolmentRecordCreationEvent(
            CREATED_AT, DefaultTestConstants.GUID1, DEFAULT_PROJECT_ID, DEFAULT_MODULE_ID, DEFAULT_USER_ID, listOf(FINGERPRINT, FACE), buildFakeBiometricReferences(),
            eventLabels.copy(subjectId = DefaultTestConstants.GUID1)))
    }

    private fun MutableList<Event>.addEnrolmentRecordMoveEvent() {
        add(EnrolmentRecordMoveEvent(
            CREATED_AT,
            EnrolmentRecordCreationInMove(DefaultTestConstants.GUID1, DEFAULT_PROJECT_ID, DEFAULT_MODULE_ID, DEFAULT_USER_ID, createBiometricReferences()),
            EnrolmentRecordDeletionInMove(DefaultTestConstants.GUID1, DEFAULT_PROJECT_ID, DEFAULT_MODULE_ID, DEFAULT_USER_ID),
            eventLabels.copy(subjectId = DefaultTestConstants.GUID1)
        ))
    }

    private fun MutableList<Event>.addEnrolmentRecordDeletionEvent() {
        add(EnrolmentRecordDeletionEvent(
            CREATED_AT, DefaultTestConstants.GUID1, DEFAULT_PROJECT_ID, DEFAULT_MODULE_ID, DEFAULT_USER_ID,
            eventLabels.copy(subjectId = DefaultTestConstants.GUID1)))
    }

    private fun MutableList<Event>.addCallbackEvent() {
        add(EnrolmentCallbackEvent(DEFAULT_TIME, randomUUID(), eventLabels))

        ErrorCallbackPayload.Reason.values().forEach {
            add(ErrorCallbackEvent(DEFAULT_TIME, it, eventLabels))
        }

        Tier.values().forEach {
            add(IdentificationCallbackEvent(DEFAULT_TIME, randomUUID(), listOf(CallbackComparisonScore(randomUUID(), 0, it)), eventLabels))
        }

        add(RefusalCallbackEvent(DEFAULT_TIME, "reason", "other_text", eventLabels))
        add(VerificationCallbackEvent(DEFAULT_TIME, CallbackComparisonScore(randomUUID(), 0, TIER_1), eventLabels))
        add(ConfirmationCallbackEvent(DEFAULT_TIME, true, eventLabels))
    }

    private fun MutableList<Event>.addCalloutEvent() {
        add(EnrolmentCalloutEvent(DEFAULT_TIME, DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, "metadata", eventLabels))
        add(ConfirmationCalloutEvent(DEFAULT_TIME, DEFAULT_PROJECT_ID, randomUUID(), randomUUID(), eventLabels))
        add(IdentificationCalloutEvent(DEFAULT_TIME, DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, "metadata", eventLabels))
        add(VerificationCalloutEvent(DEFAULT_TIME, DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, randomUUID(), "metadata", eventLabels))
        add(EnrolmentLastBiometricsCalloutEvent(DEFAULT_TIME, DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, "metadata", randomUUID(), eventLabels))
    }

    // Never invoked, but used to enforce that the implementation of a test for every event class
    fun enforceThatAnyTestHasATest() {
        val events: MutableList<Event> = mutableListOf<Event>()
        val type: ApiEventPayloadType? = null

        when (type) {
            EnrolmentRecordCreation -> events.addEnrolmentRecordCreation()
            EnrolmentRecordDeletion -> events.addEnrolmentRecordDeletionEvent()
            EnrolmentRecordMove -> events.addEnrolmentRecordMoveEvent()
            Callout -> events.addCalloutEvent()
            Callback -> events.addCallbackEvent()
            ArtificialTermination -> events.addArtificialTerminationEvent()
            Authentication -> events.addAuthenticationEvent()
            Consent -> events.addConsentEvent()
            Enrolment -> events.addEnrolmentEvent()
            Authorization -> events.addAuthorizationEvent()
            FingerprintCapture -> events.addFingerprintCaptureEvent()
            OneToOneMatch -> events.addOneToOneMatchEvent()
            OneToManyMatch -> events.addOneToManyMatchEvent()
            PersonCreation -> events.addPersonCreationEvent()
            AlertScreen -> events.addAlertScreenEvents()
            GuidSelection -> events.addGuidSelectionEvent()
            ConnectivitySnapshot -> events.addConnectivitySnapshotEvent()
            Refusal -> events.addRefusalEvent()
            CandidateRead -> events.addCandidateReadEvent()
            ScannerConnection -> events.addScannerConnectionEvent()
            Vero2InfoSnapshot -> events.addVero2InfoSnapshotEvents()
            ScannerFirmwareUpdate -> events.addScannerFirmwareUpdateEvent()
            InvalidIntent -> events.addInvalidIntentEvent()
            SuspiciousIntent -> events.addSuspiciousIntentEvent()
            IntentParsing -> events.addInvalidIntentEvent()
            CompletionCheck -> events.addCompletionCheckEvent()
            SessionCapture -> events.addSessionCaptureEvent()
            FaceOnboardingComplete -> events.addFaceOnboardingCompleteEvent()
            FaceFallbackCapture -> events.addFaceFallbackCaptureEvent()
            FaceCapture -> events.addFaceCaptureEvent()
            FaceCaptureConfirmation -> events.addFaceCaptureConfirmationEvent()
            FaceCaptureRetry -> events.addFaceCaptureRetryEvent()
            null -> {
            }
        }.safeSealedWhens
    }

}
