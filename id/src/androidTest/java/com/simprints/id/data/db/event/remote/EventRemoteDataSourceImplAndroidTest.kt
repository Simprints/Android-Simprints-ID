package com.simprints.id.data.db.event.remote

import android.net.NetworkInfo
import android.os.Build
import android.os.Build.VERSION
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.core.tools.EncodingUtils
import com.simprints.core.tools.extentions.safeSealedWhens
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.utils.randomUUID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.commontesttools.DefaultTestConstants.GUID1
import com.simprints.id.commontesttools.DefaultTestConstants.GUID2
import com.simprints.id.commontesttools.SubjectsGeneratorUtils
import com.simprints.id.commontesttools.events.CREATED_AT
import com.simprints.id.commontesttools.events.buildFakeBiometricReferences
import com.simprints.id.commontesttools.events.eventLabels
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.event.domain.models.*
import com.simprints.id.data.db.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload
import com.simprints.id.data.db.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result
import com.simprints.id.data.db.event.domain.models.AuthenticationEvent.AuthenticationPayload.UserInfo
import com.simprints.id.data.db.event.domain.models.AuthorizationEvent.AuthorizationPayload
import com.simprints.id.data.db.event.domain.models.CandidateReadEvent.CandidateReadPayload
import com.simprints.id.data.db.event.domain.models.ConsentEvent.ConsentPayload
import com.simprints.id.data.db.event.domain.models.EventType.*
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
import com.simprints.id.data.db.subject.domain.FingerIdentifier
import com.simprints.id.domain.modality.Modes.FACE
import com.simprints.id.domain.modality.Modes.FINGERPRINT
import com.simprints.id.domain.moduleapi.app.responses.entities.Tier
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
            SimApiClientFactoryImpl(mockBaseUrlProvider, "some_device", remoteDbManager, mockk(relaxed = true), JsonHelper()),
            JsonHelper()
        )
        every { timeHelper.nowMinus(any(), any()) } returns 100
        every { timeHelper.now() } returns 100
    }

    @Test
    fun aSessionWithAllEvents_shouldGetUploaded() {
        runBlocking {
            val events = mutableListOf<Event>()
            EventType.values().forEach {
                events.addEventFor(it)
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
        add(PersonCreationEvent(DEFAULT_TIME, listOf(randomUUID(), randomUUID()), randomUUID(), listOf(randomUUID()), randomUUID(), eventLabels))
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
            GUID1)

        val event = SessionCaptureEvent(
            randomUUID(),
            testProject.id,
            CREATED_AT,
            listOf(FINGERPRINT, FACE),
            "appVersionName",
            "libSimprintsVersionName",
            "EN",
            deviceArg,
            DatabaseInfo(0, 2)
        )

        event.payload.location = Location(0.0, 0.0)
        event.payload.analyticsId = "analyticsId"
        add(event)
    }

    private fun MutableList<Event>.addEnrolmentRecordCreation() {
        add(EnrolmentRecordCreationEvent(
            CREATED_AT, GUID1, testProject.id, DEFAULT_MODULE_ID, DEFAULT_USER_ID, listOf(FINGERPRINT, FACE), buildFakeBiometricReferences(),
            EventLabels(subjectId = GUID1, projectId = DEFAULT_PROJECT_ID, moduleIds = listOf(GUID2), attendantId = DEFAULT_USER_ID, mode = listOf(FINGERPRINT, FACE)))
        )
    }

    private fun MutableList<Event>.addEnrolmentRecordMoveEvent() {
        add(EnrolmentRecordMoveEvent(
            CREATED_AT,
            EnrolmentRecordCreationInMove(GUID1, testProject.id, DEFAULT_MODULE_ID, DEFAULT_USER_ID, buildFakeBiometricReferences()),
            EnrolmentRecordDeletionInMove(GUID1, testProject.id, DEFAULT_MODULE_ID, DEFAULT_USER_ID),
            EventLabels(subjectId = GUID1, projectId = DEFAULT_PROJECT_ID, moduleIds = listOf(GUID2), attendantId = DEFAULT_USER_ID, mode = listOf(FINGERPRINT, FACE)))
        )
    }

    private fun MutableList<Event>.addEnrolmentRecordDeletionEvent() {
        add(EnrolmentRecordDeletionEvent(
            CREATED_AT, GUID1, testProject.id, DEFAULT_MODULE_ID, DEFAULT_USER_ID,
            EventLabels(subjectId = GUID1, projectId = DEFAULT_PROJECT_ID, moduleIds = listOf(GUID2), attendantId = DEFAULT_USER_ID, mode = listOf(FINGERPRINT, FACE)))
        )
    }

    private fun MutableList<Event>.addCallbackErrorEvent() {
        ErrorCallbackPayload.Reason.values().forEach {
            add(ErrorCallbackEvent(DEFAULT_TIME, it, eventLabels))
        }
    }

    private fun MutableList<Event>.addCallbackEnrolmentEvent() {
        add(EnrolmentCallbackEvent(DEFAULT_TIME, randomUUID(), eventLabels))
    }

    private fun MutableList<Event>.addCallbackRefusalEvent() {
        add(RefusalCallbackEvent(DEFAULT_TIME, "reason", "other_text", eventLabels))
    }

    private fun MutableList<Event>.addCallbackVerificationEvent() {
        Tier.values().forEach {
            add(VerificationCallbackEvent(DEFAULT_TIME, CallbackComparisonScore(randomUUID(), 0, it), eventLabels))
        }
    }

    private fun MutableList<Event>.addCallbackIdentificationEvent() {
        Tier.values().forEach {
            add(IdentificationCallbackEvent(DEFAULT_TIME, randomUUID(), listOf(CallbackComparisonScore(randomUUID(), 0, it)), eventLabels))
        }
    }

    private fun MutableList<Event>.addCallbackConfirmationEvent() {
        add(ConfirmationCallbackEvent(DEFAULT_TIME, true, eventLabels))
    }

    private fun MutableList<Event>.addCalloutEnrolmentEvent() {
        add(EnrolmentCalloutEvent(DEFAULT_TIME, testProject.id, DEFAULT_USER_ID, DEFAULT_MODULE_ID, "metadata", eventLabels))
    }

    private fun MutableList<Event>.addCalloutIdentificationEvent() {
        add(IdentificationCalloutEvent(DEFAULT_TIME, testProject.id, DEFAULT_USER_ID, DEFAULT_MODULE_ID, "metadata", eventLabels))
    }

    private fun MutableList<Event>.addCalloutVerificationEvent() {
        add(VerificationCalloutEvent(DEFAULT_TIME, testProject.id, DEFAULT_USER_ID, DEFAULT_MODULE_ID, randomUUID(), "metadata", eventLabels))
    }

    private fun MutableList<Event>.addCalloutLastBiomentricsEvent() {
        add(EnrolmentLastBiometricsCalloutEvent(DEFAULT_TIME, testProject.id, DEFAULT_USER_ID, DEFAULT_MODULE_ID, "metadata", randomUUID(), eventLabels))
    }

    private fun MutableList<Event>.addCalloutConfirmationCallbackEvent() {
        add(ConfirmationCalloutEvent(DEFAULT_TIME, testProject.id, randomUUID(), randomUUID(), eventLabels))
    }

    // Never invoked, but used to enforce that the implementation of a test for every event class
    fun MutableList<Event>.addEventFor(type: EventType) {

        when (type) {
            SESSION_CAPTURE -> addSessionCaptureEvent()
            ENROLMENT_RECORD_CREATION -> addEnrolmentRecordCreation()
            ENROLMENT_RECORD_DELETION -> addEnrolmentRecordDeletionEvent()
            ENROLMENT_RECORD_MOVE -> addEnrolmentRecordMoveEvent()
            ARTIFICIAL_TERMINATION -> addArtificialTerminationEvent()
            AUTHENTICATION -> addAuthenticationEvent()
            CONSENT -> addConsentEvent()
            ENROLMENT -> addEnrolmentEvent()
            AUTHORIZATION -> addAuthorizationEvent()
            FINGERPRINT_CAPTURE -> addFingerprintCaptureEvent()
            ONE_TO_ONE_MATCH -> addOneToOneMatchEvent()
            ONE_TO_MANY_MATCH -> addOneToManyMatchEvent()
            PERSON_CREATION -> addPersonCreationEvent()
            ALERT_SCREEN -> addAlertScreenEvents()
            GUID_SELECTION -> addGuidSelectionEvent()
            CONNECTIVITY_SNAPSHOT -> addConnectivitySnapshotEvent()
            REFUSAL -> addRefusalEvent()
            CANDIDATE_READ -> addCandidateReadEvent()
            SCANNER_CONNECTION -> addScannerConnectionEvent()
            VERO_2_INFO_SNAPSHOT -> addVero2InfoSnapshotEvents()
            SCANNER_FIRMWARE_UPDATE -> addScannerFirmwareUpdateEvent()
            INVALID_INTENT -> addInvalidIntentEvent()
            CALLOUT_CONFIRMATION -> addCalloutConfirmationCallbackEvent()
            CALLOUT_IDENTIFICATION -> addCalloutIdentificationEvent()
            CALLOUT_ENROLMENT -> addCalloutEnrolmentEvent()
            CALLOUT_VERIFICATION -> addCalloutVerificationEvent()
            CALLOUT_LAST_BIOMETRICS -> addCalloutLastBiomentricsEvent()
            CALLBACK_IDENTIFICATION -> addCallbackIdentificationEvent()
            CALLBACK_ENROLMENT -> addCallbackEnrolmentEvent()
            CALLBACK_REFUSAL -> addCallbackRefusalEvent()
            CALLBACK_VERIFICATION -> addCallbackVerificationEvent()
            CALLBACK_ERROR -> addCallbackErrorEvent()
            CALLBACK_CONFIRMATION -> addCallbackConfirmationEvent()
            SUSPICIOUS_INTENT -> addSuspiciousIntentEvent()
            INTENT_PARSING -> addIntentParsingEvent()
            COMPLETION_CHECK -> addCompletionCheckEvent()
            FACE_ONBOARDING_COMPLETE -> addFaceOnboardingCompleteEvent()
            FACE_FALLBACK_CAPTURE -> addFaceFallbackCaptureEvent()
            FACE_CAPTURE -> addFaceCaptureEvent()
            FACE_CAPTURE_CONFIRMATION -> addFaceCaptureConfirmationEvent()
            FACE_CAPTURE_RETRY -> addFaceCaptureRetryEvent()
        }.safeSealedWhens
    }

}

