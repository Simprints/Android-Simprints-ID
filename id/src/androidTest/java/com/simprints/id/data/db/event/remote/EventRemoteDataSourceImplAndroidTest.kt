package com.simprints.eventsystem.event.remote

import android.net.NetworkInfo
import android.os.Build
import android.os.Build.VERSION
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.core.domain.modality.Modes.FACE
import com.simprints.core.domain.modality.Modes.FINGERPRINT
import com.simprints.core.network.NetworkConstants.Companion.DEFAULT_BASE_URL
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.core.tools.extentions.safeSealedWhens
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.utils.EncodingUtilsImpl
import com.simprints.core.tools.utils.SimNetworkUtils
import com.simprints.core.tools.utils.randomUUID
import com.simprints.eventsystem.event.domain.models.*
import com.simprints.eventsystem.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload.UserInfo
import com.simprints.eventsystem.event.domain.models.AuthorizationEvent.AuthorizationPayload
import com.simprints.eventsystem.event.domain.models.CandidateReadEvent.CandidateReadPayload
import com.simprints.eventsystem.event.domain.models.ConsentEvent.ConsentPayload
import com.simprints.eventsystem.event.domain.models.EventType.*
import com.simprints.eventsystem.event.domain.models.IntentParsingEvent.IntentParsingPayload
import com.simprints.eventsystem.event.domain.models.OneToManyMatchEvent.OneToManyMatchPayload
import com.simprints.eventsystem.event.domain.models.RefusalEvent.RefusalPayload
import com.simprints.eventsystem.event.domain.models.ScannerConnectionEvent.ScannerConnectionPayload
import com.simprints.eventsystem.event.domain.models.ScannerConnectionEvent.ScannerConnectionPayload.ScannerGeneration
import com.simprints.eventsystem.event.domain.models.Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload
import com.simprints.eventsystem.event.domain.models.callback.*
import com.simprints.eventsystem.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload
import com.simprints.eventsystem.event.domain.models.callout.*
import com.simprints.eventsystem.event.domain.models.face.*
import com.simprints.eventsystem.event.domain.models.face.FaceCaptureConfirmationEvent.FaceCaptureConfirmationPayload
import com.simprints.eventsystem.event.domain.models.face.FaceCaptureEvent.FaceCapturePayload
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureEvent
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureEvent.FingerprintCapturePayload
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintTemplateFormat
import com.simprints.eventsystem.event.domain.models.session.DatabaseInfo
import com.simprints.eventsystem.event.domain.models.session.Device
import com.simprints.eventsystem.event.domain.models.session.Location
import com.simprints.eventsystem.event.domain.models.session.SessionCaptureEvent
import com.simprints.eventsystem.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.eventsystem.sampledata.SampleDefaults.CREATED_AT
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID1
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID2
import com.simprints.eventsystem.sampledata.buildFakeBiometricReferences
import com.simprints.eventsystem.sampledata.createEnrolmentEventV1
import com.simprints.id.commontesttools.SubjectsGeneratorUtils
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.subject.domain.FingerIdentifier
import com.simprints.id.data.db.subject.domain.fromDomainToModuleApi
import com.simprints.id.network.BaseUrlProvider
import com.simprints.id.network.DefaultOkHttpClientBuilder
import com.simprints.id.network.SimApiClientFactoryImpl
import com.simprints.id.network.SimberLogger
import com.simprints.id.testtools.testingapi.TestProjectRule
import com.simprints.id.testtools.testingapi.models.TestProject
import com.simprints.id.testtools.testingapi.remote.RemoteTestingManager
import com.simprints.logging.Simber
import com.simprints.moduleapi.app.responses.IAppResponseTier
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.unit.EncodingUtilsImplForTests
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.internal.toImmutableList
import okhttp3.logging.HttpLoggingInterceptor
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

    private val remoteTestingManager: RemoteTestingManager by lazy {
        RemoteTestingManager.create(testDispatcherProvider)
    }

    @MockK lateinit var timeHelper: TimeHelper

    private lateinit var testProject: TestProject

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val testDispatcherProvider = object : DispatcherProvider {
        override fun main(): CoroutineDispatcher = testCoroutineRule.testCoroutineDispatcher

        override fun default(): CoroutineDispatcher = testCoroutineRule.testCoroutineDispatcher

        override fun io(): CoroutineDispatcher = testCoroutineRule.testCoroutineDispatcher

        override fun unconfined(): CoroutineDispatcher = testCoroutineRule.testCoroutineDispatcher
    }

    @get:Rule
    val testProjectRule = TestProjectRule(testDispatcherProvider)

    private lateinit var eventRemoteDataSource: EventRemoteDataSource
    private lateinit var eventLabels: EventLabels

    @MockK
    var remoteDbManager = mockk<RemoteDbManager>()

    private val okHttpClientBuilder = object : DefaultOkHttpClientBuilder() {
        override fun get(authToken: String?,
                         deviceId: String,
                         versionName: String,
                         interceptor: Interceptor): OkHttpClient.Builder =
            super.get(authToken, deviceId, versionName,interceptor).apply {
                addInterceptor(HttpLoggingInterceptor(SimberLogger).apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                addNetworkInterceptor {
                    var request = it.request()
                    val url: HttpUrl = request.url.newBuilder().addQueryParameter("acceptInvalidEvents", "false").build()
                    request = request.newBuilder().url(url).build()
                    return@addNetworkInterceptor it.proceed(request)
                }
            }
    }

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        testProject = testProjectRule.testProject
        eventLabels = EventLabels(sessionId = GUID1, deviceId = GUID1, projectId = testProject.id)

        val firebaseTestToken = remoteTestingManager.generateFirebaseToken(testProject.id, SIGNED_ID_USER)
        coEvery { remoteDbManager.getCurrentToken() } returns firebaseTestToken.token
        val mockBaseUrlProvider = mockk<BaseUrlProvider>()
        every { mockBaseUrlProvider.getApiBaseUrl() } returns DEFAULT_BASE_URL
        eventRemoteDataSource = EventRemoteDataSourceImpl(
            SimApiClientFactoryImpl(mockBaseUrlProvider, "some_device","some_version", remoteDbManager, mockk(relaxed = true), JsonHelper, testDispatcherProvider, HttpLoggingInterceptor(), okHttpClientBuilder),
            JsonHelper
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

            Simber.d("UPLOAD ALL EVENTS")
            executeUpload(events)
            Simber.d("UPLOAD ENROLMENT V1")
            executeUpload(listOf(createEnrolmentEventV1().apply { labels = eventLabels }))
        }
    }

    private suspend fun executeUpload(events: List<Event>) {
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
        add(EnrolmentEventV2(DEFAULT_TIME, randomUUID(), testProject.id, DEFAULT_MODULE_ID, DEFAULT_USER_ID, randomUUID(), eventLabels))
    }

    private fun MutableList<Event>.addFingerprintCaptureEvent() {
        FingerprintCapturePayload.Result.values().forEach { result ->
            FingerIdentifier.values().forEach { fingerIdentifier ->
                val fakeTemplate = EncodingUtilsImpl.byteArrayToBase64(
                    SubjectsGeneratorUtils.getRandomFingerprintSample().template
                )

                val fingerprint = FingerprintCapturePayload.Fingerprint(
                    fingerIdentifier.fromDomainToModuleApi(),
                    0,
                    fakeTemplate,
                    FingerprintTemplateFormat.ISO_19794_2
                )

                val event = FingerprintCaptureEvent(
                    DEFAULT_TIME,
                    DEFAULT_TIME,
                    fingerIdentifier.fromDomainToModuleApi(),
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
            val template = EncodingUtilsImpl.byteArrayToBase64(
                SubjectsGeneratorUtils.getRandomFaceSample().template
            )

            val face = FaceCapturePayload.Face(30f, 40f, 100f, template, FaceTemplateFormat.RANK_ONE_1_23)

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

    private fun MutableList<Event>.addPersonCreationEvent(fingerprintCaptureEvent: FingerprintCaptureEvent?, faceCaptureEvent: FaceCaptureEvent?) {
        add(PersonCreationEvent(DEFAULT_TIME, listOf(fingerprintCaptureEvent?.id
            ?: ""), randomUUID(), listOf(faceCaptureEvent?.id ?: ""), randomUUID(), eventLabels))
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
        event.payload.uploadedAt = 1
        event.payload.endedAt = 1

        add(event)
    }

    private fun MutableList<Event>.addEnrolmentRecordCreation() {
        add(EnrolmentRecordCreationEvent(
            CREATED_AT, GUID1, testProject.id, DEFAULT_MODULE_ID, DEFAULT_USER_ID, listOf(FINGERPRINT, FACE), buildFakeBiometricReferences(EncodingUtilsImplForTests),
            EventLabels(subjectId = GUID1, projectId = testProject.id, moduleIds = listOf(GUID2), attendantId = DEFAULT_USER_ID, mode = listOf(FINGERPRINT, FACE)))
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
        IAppResponseTier.values().forEach {
            add(VerificationCallbackEvent(DEFAULT_TIME, CallbackComparisonScore(randomUUID(), 0, it), eventLabels))
        }
    }

    private fun MutableList<Event>.addCallbackIdentificationEvent() {
        IAppResponseTier.values().forEach {
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
            ARTIFICIAL_TERMINATION -> addArtificialTerminationEvent()
            AUTHENTICATION -> addAuthenticationEvent()
            CONSENT -> addConsentEvent()
            ENROLMENT_V2 -> addEnrolmentEvent()
            AUTHORIZATION -> addAuthorizationEvent()
            FINGERPRINT_CAPTURE -> addFingerprintCaptureEvent()
            ONE_TO_ONE_MATCH -> addOneToOneMatchEvent()
            ONE_TO_MANY_MATCH -> addOneToManyMatchEvent()
            PERSON_CREATION -> addPersonCreationEvent(this.filterIsInstance<FingerprintCaptureEvent>().firstOrNull(), this.filterIsInstance<FaceCaptureEvent>().firstOrNull())
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
            ENROLMENT_RECORD_DELETION,
            ENROLMENT_RECORD_MOVE,
            ENROLMENT_V1 -> { }
        }.safeSealedWhens
    }

}

