package com.simprints.id.data.db.event.remote

import android.os.Build
import android.os.Build.VERSION
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.core.domain.modality.Modes.FACE
import com.simprints.core.domain.modality.Modes.FINGERPRINT
import com.simprints.core.network.NetworkConstants.Companion.DEFAULT_BASE_URL
import com.simprints.core.tools.extentions.safeSealedWhens
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.utils.EncodingUtilsImpl
import com.simprints.core.tools.utils.SimNetworkUtils
import com.simprints.core.tools.utils.randomUUID
import com.simprints.eventsystem.event.domain.models.AlertScreenEvent
import com.simprints.eventsystem.event.domain.models.ArtificialTerminationEvent
import com.simprints.eventsystem.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload.UserInfo
import com.simprints.eventsystem.event.domain.models.AuthorizationEvent
import com.simprints.eventsystem.event.domain.models.AuthorizationEvent.AuthorizationPayload
import com.simprints.eventsystem.event.domain.models.CandidateReadEvent
import com.simprints.eventsystem.event.domain.models.CandidateReadEvent.CandidateReadPayload
import com.simprints.eventsystem.event.domain.models.CompletionCheckEvent
import com.simprints.eventsystem.event.domain.models.ConnectivitySnapshotEvent
import com.simprints.eventsystem.event.domain.models.ConsentEvent
import com.simprints.eventsystem.event.domain.models.ConsentEvent.ConsentPayload
import com.simprints.eventsystem.event.domain.models.EnrolmentEventV2
import com.simprints.eventsystem.event.domain.models.Event
import com.simprints.eventsystem.event.domain.models.EventLabels
import com.simprints.eventsystem.event.domain.models.EventType
import com.simprints.eventsystem.event.domain.models.EventType.ALERT_SCREEN
import com.simprints.eventsystem.event.domain.models.EventType.ARTIFICIAL_TERMINATION
import com.simprints.eventsystem.event.domain.models.EventType.AUTHENTICATION
import com.simprints.eventsystem.event.domain.models.EventType.AUTHORIZATION
import com.simprints.eventsystem.event.domain.models.EventType.CALLBACK_CONFIRMATION
import com.simprints.eventsystem.event.domain.models.EventType.CALLBACK_ENROLMENT
import com.simprints.eventsystem.event.domain.models.EventType.CALLBACK_ERROR
import com.simprints.eventsystem.event.domain.models.EventType.CALLBACK_IDENTIFICATION
import com.simprints.eventsystem.event.domain.models.EventType.CALLBACK_REFUSAL
import com.simprints.eventsystem.event.domain.models.EventType.CALLBACK_VERIFICATION
import com.simprints.eventsystem.event.domain.models.EventType.CALLOUT_CONFIRMATION
import com.simprints.eventsystem.event.domain.models.EventType.CALLOUT_ENROLMENT
import com.simprints.eventsystem.event.domain.models.EventType.CALLOUT_IDENTIFICATION
import com.simprints.eventsystem.event.domain.models.EventType.CALLOUT_LAST_BIOMETRICS
import com.simprints.eventsystem.event.domain.models.EventType.CALLOUT_VERIFICATION
import com.simprints.eventsystem.event.domain.models.EventType.CANDIDATE_READ
import com.simprints.eventsystem.event.domain.models.EventType.COMPLETION_CHECK
import com.simprints.eventsystem.event.domain.models.EventType.CONNECTIVITY_SNAPSHOT
import com.simprints.eventsystem.event.domain.models.EventType.CONSENT
import com.simprints.eventsystem.event.domain.models.EventType.ENROLMENT_RECORD_CREATION
import com.simprints.eventsystem.event.domain.models.EventType.ENROLMENT_RECORD_DELETION
import com.simprints.eventsystem.event.domain.models.EventType.ENROLMENT_RECORD_MOVE
import com.simprints.eventsystem.event.domain.models.EventType.ENROLMENT_V1
import com.simprints.eventsystem.event.domain.models.EventType.ENROLMENT_V2
import com.simprints.eventsystem.event.domain.models.EventType.FACE_CAPTURE
import com.simprints.eventsystem.event.domain.models.EventType.FACE_CAPTURE_BIOMETRICS
import com.simprints.eventsystem.event.domain.models.EventType.FACE_CAPTURE_CONFIRMATION
import com.simprints.eventsystem.event.domain.models.EventType.FACE_CAPTURE_V3
import com.simprints.eventsystem.event.domain.models.EventType.FACE_FALLBACK_CAPTURE
import com.simprints.eventsystem.event.domain.models.EventType.FACE_ONBOARDING_COMPLETE
import com.simprints.eventsystem.event.domain.models.EventType.FINGERPRINT_CAPTURE
import com.simprints.eventsystem.event.domain.models.EventType.FINGERPRINT_CAPTURE_BIOMETRICS
import com.simprints.eventsystem.event.domain.models.EventType.FINGERPRINT_CAPTURE_V3
import com.simprints.eventsystem.event.domain.models.EventType.GUID_SELECTION
import com.simprints.eventsystem.event.domain.models.EventType.INTENT_PARSING
import com.simprints.eventsystem.event.domain.models.EventType.INVALID_INTENT
import com.simprints.eventsystem.event.domain.models.EventType.ONE_TO_MANY_MATCH
import com.simprints.eventsystem.event.domain.models.EventType.ONE_TO_ONE_MATCH
import com.simprints.eventsystem.event.domain.models.EventType.PERSON_CREATION
import com.simprints.eventsystem.event.domain.models.EventType.REFUSAL
import com.simprints.eventsystem.event.domain.models.EventType.SCANNER_CONNECTION
import com.simprints.eventsystem.event.domain.models.EventType.SCANNER_FIRMWARE_UPDATE
import com.simprints.eventsystem.event.domain.models.EventType.SESSION_CAPTURE
import com.simprints.eventsystem.event.domain.models.EventType.SUSPICIOUS_INTENT
import com.simprints.eventsystem.event.domain.models.EventType.VERO_2_INFO_SNAPSHOT
import com.simprints.eventsystem.event.domain.models.GuidSelectionEvent
import com.simprints.eventsystem.event.domain.models.IntentParsingEvent
import com.simprints.eventsystem.event.domain.models.IntentParsingEvent.IntentParsingPayload
import com.simprints.eventsystem.event.domain.models.InvalidIntentEvent
import com.simprints.eventsystem.event.domain.models.MatchEntry
import com.simprints.eventsystem.event.domain.models.Matcher
import com.simprints.eventsystem.event.domain.models.OneToManyMatchEvent
import com.simprints.eventsystem.event.domain.models.OneToManyMatchEvent.OneToManyMatchPayload
import com.simprints.eventsystem.event.domain.models.OneToOneMatchEvent
import com.simprints.eventsystem.event.domain.models.PersonCreationEvent
import com.simprints.eventsystem.event.domain.models.RefusalEvent
import com.simprints.eventsystem.event.domain.models.RefusalEvent.RefusalPayload
import com.simprints.eventsystem.event.domain.models.ScannerConnectionEvent
import com.simprints.eventsystem.event.domain.models.ScannerConnectionEvent.ScannerConnectionPayload
import com.simprints.eventsystem.event.domain.models.ScannerConnectionEvent.ScannerConnectionPayload.ScannerGeneration
import com.simprints.eventsystem.event.domain.models.ScannerFirmwareUpdateEvent
import com.simprints.eventsystem.event.domain.models.SuspiciousIntentEvent
import com.simprints.eventsystem.event.domain.models.Vero2InfoSnapshotEvent
import com.simprints.eventsystem.event.domain.models.Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload
import com.simprints.eventsystem.event.domain.models.callback.CallbackComparisonScore
import com.simprints.eventsystem.event.domain.models.callback.ConfirmationCallbackEvent
import com.simprints.eventsystem.event.domain.models.callback.EnrolmentCallbackEvent
import com.simprints.eventsystem.event.domain.models.callback.ErrorCallbackEvent
import com.simprints.eventsystem.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload
import com.simprints.eventsystem.event.domain.models.callback.IdentificationCallbackEvent
import com.simprints.eventsystem.event.domain.models.callback.RefusalCallbackEvent
import com.simprints.eventsystem.event.domain.models.callback.VerificationCallbackEvent
import com.simprints.eventsystem.event.domain.models.callout.ConfirmationCalloutEvent
import com.simprints.eventsystem.event.domain.models.callout.EnrolmentCalloutEvent
import com.simprints.eventsystem.event.domain.models.callout.EnrolmentLastBiometricsCalloutEvent
import com.simprints.eventsystem.event.domain.models.callout.IdentificationCalloutEvent
import com.simprints.eventsystem.event.domain.models.callout.VerificationCalloutEvent
import com.simprints.eventsystem.event.domain.models.face.FaceCaptureBiometricsEvent
import com.simprints.eventsystem.event.domain.models.face.FaceCaptureConfirmationEvent
import com.simprints.eventsystem.event.domain.models.face.FaceCaptureConfirmationEvent.FaceCaptureConfirmationPayload
import com.simprints.eventsystem.event.domain.models.face.FaceCaptureEventV3
import com.simprints.eventsystem.event.domain.models.face.FaceFallbackCaptureEvent
import com.simprints.eventsystem.event.domain.models.face.FaceOnboardingCompleteEvent
import com.simprints.eventsystem.event.domain.models.face.FaceTemplateFormat
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureBiometricsEvent
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureEventV3
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintTemplateFormat
import com.simprints.eventsystem.event.domain.models.session.DatabaseInfo
import com.simprints.eventsystem.event.domain.models.session.Device
import com.simprints.eventsystem.event.domain.models.session.Location
import com.simprints.eventsystem.event.domain.models.session.SessionCaptureEvent
import com.simprints.eventsystem.event.remote.EventRemoteDataSource
import com.simprints.eventsystem.event.remote.EventRemoteDataSourceImpl
import com.simprints.eventsystem.sampledata.SampleDefaults.CREATED_AT
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID1
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
import com.simprints.testtools.common.coroutines.TestDispatcherProvider
import com.simprints.testtools.unit.EncodingUtilsImplForTests
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
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

    @MockK
    lateinit var timeHelper: TimeHelper

    private lateinit var testProject: TestProject

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()
    private val testDispatcherProvider = TestDispatcherProvider(testCoroutineRule)

    @get:Rule
    val testProjectRule = TestProjectRule(testDispatcherProvider)

    private lateinit var eventRemoteDataSource: EventRemoteDataSource
    private lateinit var eventLabels: EventLabels

    @MockK
    var remoteDbManager = mockk<RemoteDbManager>()

    private val okHttpClientBuilder = object : DefaultOkHttpClientBuilder() {
        override fun get(
            authToken: String?,
            deviceId: String,
            versionName: String,
            interceptor: Interceptor
        ): OkHttpClient.Builder =
            super.get(authToken, deviceId, versionName, interceptor).apply {
                addInterceptor(HttpLoggingInterceptor(SimberLogger).apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                addNetworkInterceptor {
                    var request = it.request()
                    val url: HttpUrl =
                        request.url.newBuilder().addQueryParameter("acceptInvalidEvents", "false")
                            .build()
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

        val firebaseTestToken =
            remoteTestingManager.generateFirebaseToken(
                projectId = testProject.id,
                userId = SIGNED_ID_USER
            )
        coEvery { remoteDbManager.getCurrentToken() } returns firebaseTestToken.token
        val mockBaseUrlProvider = mockk<BaseUrlProvider>()
        every { mockBaseUrlProvider.getApiBaseUrl() } returns DEFAULT_BASE_URL
        eventRemoteDataSource = EventRemoteDataSourceImpl(
            SimApiClientFactoryImpl(
                baseUrlProvider = mockBaseUrlProvider,
                deviceId = "some_device",
                versionName = "some_version",
                remoteDbManager = remoteDbManager,
                jsonHelper = JsonHelper,
                dispatcher = testDispatcherProvider,
                interceptor = HttpLoggingInterceptor(),
                okHttpClientBuilder = okHttpClientBuilder
            ),
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
        eventRemoteDataSource.post(projectId = testProject.id, events = events.toImmutableList())
    }

    private fun MutableList<Event>.addAlertScreenEvents() {
        AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.values()
            .forEach {
                add(
                    AlertScreenEvent(
                        createdAt = DEFAULT_TIME,
                        alertType = it,
                        labels = eventLabels
                    )
                )
            }
    }

    private fun MutableList<Event>.addArtificialTerminationEvent() {
        ArtificialTerminationPayload.Reason.values().forEach {
            add(
                ArtificialTerminationEvent(
                    createdAt = DEFAULT_TIME,
                    reason = it,
                    labels = eventLabels
                )
            )
        }
    }

    private fun MutableList<Event>.addAuthenticationEvent() {
        listOf(
            Result.OFFLINE,
            Result.OFFLINE,
            Result.OFFLINE,
            Result.OFFLINE,
            Result.TECHNICAL_FAILURE,
            Result.BACKEND_MAINTENANCE_ERROR,
            Result.SAFETYNET_UNAVAILABLE,
            Result.SAFETYNET_INVALID_CLAIM,
            Result.UNKNOWN
        ).forEach {
            add(
                AuthenticationEvent(
                    createdAt = DEFAULT_TIME,
                    endTime = DEFAULT_TIME,
                    userInfo = UserInfo("some_project", DEFAULT_USER_ID),
                    result = it,
                    labels = eventLabels
                )
            )
        }
    }

    private fun MutableList<Event>.addAuthorizationEvent() {
        AuthorizationPayload.AuthorizationResult.values().forEach {
            add(
                AuthorizationEvent(
                    createdAt = DEFAULT_TIME,
                    result = it,
                    userInfo = AuthorizationPayload.UserInfo("some_project", DEFAULT_USER_ID),
                    labels = eventLabels
                )
            )
        }
    }

    private fun MutableList<Event>.addCandidateReadEvent() {
        CandidateReadPayload.LocalResult.values().forEach { local ->
            CandidateReadPayload.RemoteResult.values().forEach { remote ->
                add(
                    CandidateReadEvent(
                        createdAt = DEFAULT_TIME,
                        endTime = DEFAULT_TIME,
                        candidateId = randomUUID(),
                        localResult = local,
                        remoteResult = remote,
                        labels = eventLabels
                    )
                )
            }
        }
    }

    private fun MutableList<Event>.addConnectivitySnapshotEvent() {
        add(
            ConnectivitySnapshotEvent(
                createdAt = DEFAULT_TIME,
                connections = listOf(
                    SimNetworkUtils.Connection(
                        SimNetworkUtils.ConnectionType.MOBILE,
                        SimNetworkUtils.ConnectionState.CONNECTED
                    )
                ), labels = eventLabels
            )
        )
    }

    private fun MutableList<Event>.addConsentEvent() {
        ConsentPayload.Type.values().forEach { type ->
            ConsentPayload.Result.values().forEach { result ->
                add(
                    ConsentEvent(
                        createdAt = DEFAULT_TIME,
                        endTime = DEFAULT_TIME,
                        consentType = type,
                        result = result,
                        labels = eventLabels
                    )
                )
            }
        }
    }

    private fun MutableList<Event>.addEnrolmentEvent() {
        add(
            EnrolmentEventV2(
                createdAt = DEFAULT_TIME,
                subjectId = randomUUID(),
                projectId = testProject.id,
                moduleId = DEFAULT_MODULE_ID,
                attendantId = DEFAULT_USER_ID,
                personCreationEventId = randomUUID(),
                labels = eventLabels
            )
        )
    }

    private fun MutableList<Event>.addFingerprintCaptureEventV3() {
        FingerprintCaptureEventV3.FingerprintCapturePayloadV3.Result.values().forEach { result ->
            FingerIdentifier.values().forEach { fingerIdentifier ->

                val fingerprint = FingerprintCaptureEventV3.FingerprintCapturePayloadV3.Fingerprint(
                    finger = fingerIdentifier.fromDomainToModuleApi(),
                    quality = 0,
                    format = FingerprintTemplateFormat.ISO_19794_2
                )

                val event = FingerprintCaptureEventV3(
                    createdAt = DEFAULT_TIME,
                    endTime = DEFAULT_TIME,
                    finger = fingerIdentifier.fromDomainToModuleApi(),
                    qualityThreshold = 0,
                    result = result,
                    fingerprint = fingerprint,
                    labels = eventLabels
                )

                add(event)
            }
        }
    }

    private fun MutableList<Event>.addFingerprintBiometricCaptureEvent() {
        FingerprintCaptureBiometricsEvent.FingerprintCaptureBiometricsPayload.Result.values()
            .forEach { result ->
                FingerIdentifier.values().forEach { fingerIdentifier ->
                    val fakeTemplate = EncodingUtilsImpl.byteArrayToBase64(
                        SubjectsGeneratorUtils.getRandomFingerprintSample().template
                    )

                    val fingerprint =
                        FingerprintCaptureBiometricsEvent.FingerprintCaptureBiometricsPayload.Fingerprint(
                            finger = fingerIdentifier.fromDomainToModuleApi(),
                            template = fakeTemplate,
                            format = FingerprintTemplateFormat.ISO_19794_2
                        )

                    val event = FingerprintCaptureBiometricsEvent(
                        createdAt = DEFAULT_TIME,
                        result = result,
                        fingerprint = fingerprint,
                        labels = eventLabels
                    )

                    add(event)
                }
            }
    }


    private fun MutableList<Event>.addFaceCaptureEventV3() {
        FaceCaptureEventV3.FaceCapturePayloadV3.Result.values().forEachIndexed { index, result ->

            val face =
                FaceCaptureEventV3.FaceCapturePayloadV3.Face(
                    yaw = 30f,
                    roll = 40f,
                    quality = 100f,
                    format = FaceTemplateFormat.RANK_ONE_1_23
                )

            val event = FaceCaptureEventV3(
                startTime = DEFAULT_TIME,
                endTime = DEFAULT_TIME + 100,
                attemptNb = index + 1,
                qualityThreshold = 0f,
                result = result,
                isFallback = false,
                face = face,
                labels = eventLabels
            )

            add(event)
        }
    }

    private fun MutableList<Event>.addFaceCaptureBiometricCaptureEvent() {
        FaceCaptureBiometricsEvent.FaceCaptureBiometricsPayload.Result.values()
            .forEachIndexed { index, result ->
                val template = EncodingUtilsImpl.byteArrayToBase64(
                    SubjectsGeneratorUtils.getRandomFaceSample().template
                )

                val face =
                    FaceCaptureBiometricsEvent.FaceCaptureBiometricsPayload.Face(
                        template = template,
                        format = FaceTemplateFormat.RANK_ONE_1_23
                    )

                val event = FaceCaptureBiometricsEvent(
                    startTime = DEFAULT_TIME,
                    qualityThreshold = 0f,
                    result = result,
                    face = face,
                    labels = eventLabels
                )

                add(event)
            }
    }

    private fun MutableList<Event>.addFaceCaptureConfirmationEvent() {
        FaceCaptureConfirmationPayload.Result.values().forEach { result ->
            val event = FaceCaptureConfirmationEvent(
                startTime = DEFAULT_TIME,
                endTime = DEFAULT_TIME + 100,
                result = result,
                labels = eventLabels
            )

            add(event)
        }
    }

    private fun MutableList<Event>.addFaceFallbackCaptureEvent() {
        val event = FaceFallbackCaptureEvent(
            startTime = DEFAULT_TIME,
            endTime = DEFAULT_TIME + 100,
            labels = eventLabels
        )
        add(event)
    }

    private fun MutableList<Event>.addFaceOnboardingCompleteEvent() {
        val event = FaceOnboardingCompleteEvent(
            startTime = DEFAULT_TIME,
            endTime = DEFAULT_TIME + 100,
            labels = eventLabels
        )
        add(event)
    }

    private fun MutableList<Event>.addGuidSelectionEvent() {
        add(
            GuidSelectionEvent(
                createdAt = DEFAULT_TIME,
                selectedId = randomUUID(),
                labels = eventLabels
            )
        )
    }

    private fun MutableList<Event>.addIntentParsingEvent() {
        IntentParsingPayload.IntegrationInfo.values().forEach {
            add(
                IntentParsingEvent(
                    createdAt = DEFAULT_TIME,
                    integration = it,
                    labels = eventLabels
                )
            )
        }
    }

    private fun MutableList<Event>.addInvalidIntentEvent() {
        add(
            InvalidIntentEvent(
                creationTime = DEFAULT_TIME,
                action = "some_action",
                extras = mapOf("wrong_field" to "wrong_value"),
                labels = eventLabels
            )
        )
    }

    private fun MutableList<Event>.addOneToManyMatchEvent() {
        OneToManyMatchPayload.MatchPoolType.values().forEach {
            add(
                OneToManyMatchEvent(
                    createdAt = DEFAULT_TIME,
                    endTime = DEFAULT_TIME,
                    pool = OneToManyMatchPayload.MatchPool(it, 0),
                    matcher = Matcher.SIM_AFIS,
                    result = emptyList(),
                    labels = eventLabels
                )
            )
        }
    }

    private fun MutableList<Event>.addOneToOneMatchEvent() {
        add(
            OneToOneMatchEvent(
                createdAt = DEFAULT_TIME,
                endTime = DEFAULT_TIME,
                candidateId = randomUUID(),
                matcher = Matcher.SIM_AFIS,
                result = MatchEntry(randomUUID(), 0F),
                labels = eventLabels
            )
        )
    }

    private fun MutableList<Event>.addPersonCreationEvent(
        fingerprintCaptureEvent: FingerprintCaptureEventV3?,
        faceCaptureEvent: FaceCaptureEventV3?
    ) {
        add(
            PersonCreationEvent(
                startTime = DEFAULT_TIME,
                fingerprintCaptureIds = listOf(
                    fingerprintCaptureEvent?.id
                        ?: ""
                ),
                fingerprintReferenceId = randomUUID(),
                faceCaptureIds = listOf(faceCaptureEvent?.id ?: ""),
                faceReferenceId = randomUUID(),
                labels = eventLabels
            )
        )
    }

    private fun MutableList<Event>.addRefusalEvent() {
        RefusalPayload.Answer.values().forEach {
            add(
                RefusalEvent(
                    createdAt = DEFAULT_TIME,
                    endTime = DEFAULT_TIME,
                    reason = it,
                    otherText = "other_text",
                    labels = eventLabels
                )
            )
        }
    }

    private fun MutableList<Event>.addScannerConnectionEvent() {
        add(
            ScannerConnectionEvent(
                createdAt = DEFAULT_TIME,
                scannerInfo = ScannerConnectionPayload.ScannerInfo(
                    "scanner_id", "macAddress",
                    ScannerGeneration.VERO_2, "hardware"
                ),
                labels = eventLabels
            )
        )
    }

    private fun MutableList<Event>.addVero2InfoSnapshotEvents() {
        add(
            Vero2InfoSnapshotEvent(
                createdAt = DEFAULT_TIME,
                version = Vero2InfoSnapshotPayload.Vero2Version(
                    Int.MAX_VALUE.toLong() + 1, "1.23",
                    "api", "stmApp", "stmApi", "un20App", "un20Api"
                ),
                battery = Vero2InfoSnapshotPayload.BatteryInfo(70, 15, 1, 37),
                labels = eventLabels
            )
        )
    }

    private fun MutableList<Event>.addScannerFirmwareUpdateEvent() {
        add(
            ScannerFirmwareUpdateEvent(
                createdAt = DEFAULT_TIME, endTime = DEFAULT_TIME, chip = "stm",
                targetAppVersion = "targetApp", failureReason = "failureReason",
                labels = eventLabels
            )
        )
    }

    private fun MutableList<Event>.addSuspiciousIntentEvent() {
        add(
            SuspiciousIntentEvent(
                createdAt = DEFAULT_TIME,
                unexpectedExtras = mapOf("some_extra_key" to "value"),
                labels = eventLabels
            )
        )
    }

    private fun MutableList<Event>.addCompletionCheckEvent() {
        add(CompletionCheckEvent(createdAt = DEFAULT_TIME, completed = true, labels = eventLabels))
    }

    private fun MutableList<Event>.addSessionCaptureEvent() {
        val deviceArg = Device(
            androidSdkVersion = VERSION.SDK_INT.toString(),
            deviceModel = Build.MANUFACTURER + "_" + Build.MODEL,
            deviceId = GUID1
        )

        val event = SessionCaptureEvent(
            id = randomUUID(),
            projectId = testProject.id,
            createdAt = CREATED_AT,
            modalities = listOf(FINGERPRINT, FACE),
            appVersionName = "appVersionName",
            libVersionName = "libSimprintsVersionName",
            language = "EN",
            device = deviceArg,
            databaseInfo = DatabaseInfo(sessionCount = 0, recordCount = 2)
        )

        event.payload.location = Location(latitude = 0.0, longitude = 0.0)
        event.payload.uploadedAt = 1
        event.payload.endedAt = 1

        add(event)
    }

    private fun MutableList<Event>.addCallbackErrorEvent() {
        ErrorCallbackPayload.Reason.values().forEach {
            add(ErrorCallbackEvent(createdAt = DEFAULT_TIME, reason = it, labels = eventLabels))
        }
    }

    private fun MutableList<Event>.addCallbackEnrolmentEvent() {
        add(
            EnrolmentCallbackEvent(
                createdAt = DEFAULT_TIME,
                guid = randomUUID(),
                eventLabels = eventLabels
            )
        )
    }

    private fun MutableList<Event>.addCallbackRefusalEvent() {
        add(
            RefusalCallbackEvent(
                createdAt = DEFAULT_TIME,
                reason = "reason",
                extra = "other_text",
                labels = eventLabels
            )
        )
    }

    private fun MutableList<Event>.addCallbackVerificationEvent() {
        IAppResponseTier.values().forEach {
            add(
                VerificationCallbackEvent(
                    createdAt = DEFAULT_TIME,
                    score = CallbackComparisonScore(randomUUID(), 0, it),
                    labels = eventLabels
                )
            )
        }
    }

    private fun MutableList<Event>.addCallbackIdentificationEvent() {
        IAppResponseTier.values().forEach {
            add(
                IdentificationCallbackEvent(
                    createdAt = DEFAULT_TIME,
                    sessionId = randomUUID(),
                    scores = listOf(CallbackComparisonScore(randomUUID(), 0, it)),
                    labels = eventLabels
                )
            )
        }
    }

    private fun MutableList<Event>.addCallbackConfirmationEvent() {
        add(
            ConfirmationCallbackEvent(
                createdAt = DEFAULT_TIME,
                identificationOutcome = true,
                labels = eventLabels
            )
        )
    }

    private fun MutableList<Event>.addCalloutEnrolmentEvent() {
        add(
            EnrolmentCalloutEvent(
                createdAt = DEFAULT_TIME,
                projectId = testProject.id,
                userId = DEFAULT_USER_ID,
                moduleId = DEFAULT_MODULE_ID,
                metadata = "metadata",
                labels = eventLabels
            )
        )
    }

    private fun MutableList<Event>.addCalloutIdentificationEvent() {
        add(
            IdentificationCalloutEvent(
                createdAt = DEFAULT_TIME,
                projectId = testProject.id,
                userId = DEFAULT_USER_ID,
                moduleId = DEFAULT_MODULE_ID,
                metadata = "metadata",
                labels = eventLabels
            )
        )
    }

    private fun MutableList<Event>.addCalloutVerificationEvent() {
        add(
            VerificationCalloutEvent(
                createdAt = DEFAULT_TIME,
                projectId = testProject.id,
                userId = DEFAULT_USER_ID,
                moduleId = DEFAULT_MODULE_ID,
                verifyGuid = randomUUID(),
                metadata = "metadata",
                labels = eventLabels
            )
        )
    }

    private fun MutableList<Event>.addCalloutLastBiomentricsEvent() {
        add(
            EnrolmentLastBiometricsCalloutEvent(
                createdAt = DEFAULT_TIME,
                projectId = testProject.id,
                userId = DEFAULT_USER_ID,
                moduleId = DEFAULT_MODULE_ID,
                metadata = "metadata",
                sessionId = randomUUID(),
                labels = eventLabels
            )
        )
    }

    private fun MutableList<Event>.addCalloutConfirmationCallbackEvent() {
        add(
            ConfirmationCalloutEvent(
                createdAt = DEFAULT_TIME,
                projectId = testProject.id,
                selectedGuid = randomUUID(),
                sessionId = randomUUID(),
                labels = eventLabels
            )
        )
    }

    // Never invoked, but used to enforce that the implementation of a test for every event class
    fun MutableList<Event>.addEventFor(type: EventType) {

        when (type) {
            SESSION_CAPTURE -> addSessionCaptureEvent()
            ARTIFICIAL_TERMINATION -> addArtificialTerminationEvent()
            AUTHENTICATION -> addAuthenticationEvent()
            CONSENT -> addConsentEvent()
            ENROLMENT_V2 -> addEnrolmentEvent()
            AUTHORIZATION -> addAuthorizationEvent()
            FINGERPRINT_CAPTURE_V3 -> addFingerprintCaptureEventV3()
            FINGERPRINT_CAPTURE_BIOMETRICS -> addFingerprintBiometricCaptureEvent()
            ONE_TO_ONE_MATCH -> addOneToOneMatchEvent()
            ONE_TO_MANY_MATCH -> addOneToManyMatchEvent()
            PERSON_CREATION -> addPersonCreationEvent(
                this.filterIsInstance<FingerprintCaptureEventV3>().firstOrNull(),
                this.filterIsInstance<FaceCaptureEventV3>().firstOrNull()
            )
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
            FACE_CAPTURE_V3 -> addFaceCaptureEventV3()
            FACE_CAPTURE_BIOMETRICS -> addFaceCaptureBiometricCaptureEvent()
            FACE_CAPTURE_CONFIRMATION -> addFaceCaptureConfirmationEvent()
            ENROLMENT_RECORD_DELETION,
            ENROLMENT_RECORD_CREATION,
            ENROLMENT_RECORD_MOVE,
            ENROLMENT_V1,
            FINGERPRINT_CAPTURE,
            FACE_CAPTURE -> {
            }
        }.safeSealedWhens
    }
}

