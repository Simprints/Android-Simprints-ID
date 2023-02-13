package com.simprints.eventsystem.sampledata

import android.os.Build
import com.simprints.core.tools.utils.SimNetworkUtils
import com.simprints.core.tools.utils.SimNetworkUtils.Connection
import com.simprints.eventsystem.event.domain.models.*
import com.simprints.eventsystem.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.BLUETOOTH_NOT_ENABLED
import com.simprints.eventsystem.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload.Reason.NEW_SESSION
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result.AUTHENTICATED
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload.UserInfo
import com.simprints.eventsystem.event.domain.models.AuthorizationEvent.AuthorizationPayload
import com.simprints.eventsystem.event.domain.models.AuthorizationEvent.AuthorizationPayload.AuthorizationResult.AUTHORIZED
import com.simprints.eventsystem.event.domain.models.CandidateReadEvent.CandidateReadPayload.LocalResult.FOUND
import com.simprints.eventsystem.event.domain.models.CandidateReadEvent.CandidateReadPayload.RemoteResult.NOT_FOUND
import com.simprints.eventsystem.event.domain.models.ConsentEvent.ConsentPayload.Result.ACCEPTED
import com.simprints.eventsystem.event.domain.models.ConsentEvent.ConsentPayload.Type.INDIVIDUAL
import com.simprints.eventsystem.event.domain.models.IntentParsingEvent.IntentParsingPayload.IntegrationInfo.COMMCARE
import com.simprints.eventsystem.event.domain.models.Matcher.RANK_ONE
import com.simprints.eventsystem.event.domain.models.Matcher.SIM_AFIS
import com.simprints.eventsystem.event.domain.models.OneToManyMatchEvent.OneToManyMatchPayload.MatchPool
import com.simprints.eventsystem.event.domain.models.OneToManyMatchEvent.OneToManyMatchPayload.MatchPoolType.PROJECT
import com.simprints.eventsystem.event.domain.models.RefusalEvent.RefusalPayload.Answer.OTHER
import com.simprints.eventsystem.event.domain.models.ScannerConnectionEvent.ScannerConnectionPayload.ScannerGeneration.VERO_1
import com.simprints.eventsystem.event.domain.models.ScannerConnectionEvent.ScannerConnectionPayload.ScannerInfo
import com.simprints.eventsystem.event.domain.models.Vero2InfoSnapshotEvent.BatteryInfo
import com.simprints.eventsystem.event.domain.models.Vero2InfoSnapshotEvent.Vero2Version
import com.simprints.eventsystem.event.domain.models.callback.*
import com.simprints.eventsystem.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason.DIFFERENT_PROJECT_ID_SIGNED_IN
import com.simprints.eventsystem.event.domain.models.callout.*
import com.simprints.eventsystem.event.domain.models.face.*
import com.simprints.eventsystem.event.domain.models.face.FaceCaptureConfirmationEvent.FaceCaptureConfirmationPayload.Result.CONTINUE
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureBiometricsEvent
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureEvent
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintTemplateFormat
import com.simprints.eventsystem.event.domain.models.session.DatabaseInfo
import com.simprints.eventsystem.event.domain.models.session.Device
import com.simprints.eventsystem.event.domain.models.session.Location
import com.simprints.eventsystem.event.domain.models.session.SessionCaptureEvent
import com.simprints.eventsystem.sampledata.SampleDefaults.CREATED_AT
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_METADATA
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.ENDED_AT
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID1
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID2
import com.simprints.infra.config.domain.models.GeneralConfiguration.Modality
import com.simprints.moduleapi.app.responses.IAppResponseTier.TIER_1
import com.simprints.moduleapi.fingerprint.IFingerIdentifier.LEFT_THUMB

val eventLabels = EventLabels(sessionId = GUID1, deviceId = GUID1, projectId = DEFAULT_PROJECT_ID)

fun createConfirmationCallbackEvent() = ConfirmationCallbackEvent(CREATED_AT, true, eventLabels)

fun createEnrolmentCallbackEvent() = EnrolmentCallbackEvent(CREATED_AT, GUID1, eventLabels)

fun createErrorCallbackEvent() =
    ErrorCallbackEvent(CREATED_AT, DIFFERENT_PROJECT_ID_SIGNED_IN, eventLabels)

fun createIdentificationCallbackEvent(): IdentificationCallbackEvent {
    val comparisonScore = CallbackComparisonScore(GUID1, 1, TIER_1)
    return IdentificationCallbackEvent(CREATED_AT, GUID1, listOf(comparisonScore), eventLabels)
}

fun createRefusalCallbackEvent() =
    RefusalCallbackEvent(CREATED_AT, "some_reason", "extra", eventLabels)

fun createVerificationCallbackEvent(): VerificationCallbackEvent {
    val comparisonScore = CallbackComparisonScore(GUID1, 1, TIER_1)
    return VerificationCallbackEvent(CREATED_AT, comparisonScore, eventLabels)
}

fun createConfirmationCalloutEvent() =
    ConfirmationCalloutEvent(CREATED_AT, DEFAULT_PROJECT_ID, GUID1, GUID2, eventLabels)

fun createEnrolmentCalloutEvent(projectId: String = DEFAULT_PROJECT_ID) = EnrolmentCalloutEvent(
    CREATED_AT,
    projectId,
    DEFAULT_USER_ID,
    DEFAULT_MODULE_ID,
    DEFAULT_METADATA,
    eventLabels,
    projectId
)

fun createIdentificationCalloutEvent() = IdentificationCalloutEvent(
    CREATED_AT,
    DEFAULT_PROJECT_ID,
    DEFAULT_USER_ID,
    DEFAULT_MODULE_ID,
    DEFAULT_METADATA,
    eventLabels
)

fun createLastBiometricsEnrolmentCalloutEvent() = EnrolmentLastBiometricsCalloutEvent(
    CREATED_AT,
    DEFAULT_PROJECT_ID,
    DEFAULT_USER_ID,
    DEFAULT_MODULE_ID,
    DEFAULT_METADATA,
    GUID2,
    eventLabels
)

fun createVerificationCalloutEvent() = VerificationCalloutEvent(
    CREATED_AT,
    DEFAULT_PROJECT_ID,
    DEFAULT_USER_ID,
    DEFAULT_MODULE_ID,
    DEFAULT_METADATA,
    GUID2,
    eventLabels
)

fun createFaceCaptureBiometricsEvent(): FaceCaptureBiometricsEvent {
    val faceArg = FaceCaptureBiometricsEvent.FaceCaptureBiometricsPayload.Face(
        yaw = 1.0f,
        roll = 0.0f,
        template = "template",
        quality = 1.0f,
        format = FaceTemplateFormat.RANK_ONE_1_23
    )
    return FaceCaptureBiometricsEvent(
        startTime = CREATED_AT,
        face = faceArg,
        labels = eventLabels
    )
}

fun createFaceCaptureConfirmationEvent() =
    FaceCaptureConfirmationEvent(CREATED_AT, ENDED_AT, CONTINUE, eventLabels)

fun createFaceCaptureEvent(): FaceCaptureEvent {
    val faceArg =
        FaceCaptureEvent.FaceCapturePayload.Face(0F, 1F, 2F, FaceTemplateFormat.RANK_ONE_1_23)
    return FaceCaptureEvent(
        startTime = CREATED_AT,
        endTime = ENDED_AT,
        attemptNb = 0,
        qualityThreshold = 1F,
        result = FaceCaptureEvent.FaceCapturePayload.Result.VALID,
        isFallback = true,
        face = faceArg,
        labels = eventLabels
    )
}

fun createFaceFallbackCaptureEvent() = FaceFallbackCaptureEvent(CREATED_AT, ENDED_AT, eventLabels)

fun createFaceOnboardingCompleteEvent() =
    FaceOnboardingCompleteEvent(CREATED_AT, ENDED_AT, eventLabels)

fun createSessionCaptureEvent(
    id: String = GUID1,
    createdAt: Long = CREATED_AT,
    projectId: String = DEFAULT_PROJECT_ID,
    isClosed: Boolean = false
): SessionCaptureEvent {

    val appVersionNameArg = "appVersionName"
    val libSimprintsVersionNameArg = "libSimprintsVersionName"
    val languageArg = "language"
    val deviceArg = Device(
        Build.VERSION.SDK_INT.toString(),
        Build.MANUFACTURER + "_" + Build.MODEL,
        GUID1
    )

    val databaseInfoArg = DatabaseInfo(2, 2)
    val locationArg = Location(0.0, 0.0)

    return SessionCaptureEvent(
        id,
        projectId,
        createdAt,
        listOf(Modality.FINGERPRINT, Modality.FACE),
        appVersionNameArg,
        libSimprintsVersionNameArg,
        languageArg,
        deviceArg,
        databaseInfoArg
    ).apply {
        payload.location = locationArg
        payload.endedAt = ENDED_AT
        payload.sessionIsClosed = isClosed
    }
}

fun createAlertScreenEvent() = AlertScreenEvent(CREATED_AT, BLUETOOTH_NOT_ENABLED, eventLabels)

fun createArtificialTerminationEvent() =
    ArtificialTerminationEvent(CREATED_AT, NEW_SESSION, eventLabels)

fun createAuthenticationEvent() =
    AuthenticationEvent(
        CREATED_AT,
        ENDED_AT,
        UserInfo(DEFAULT_PROJECT_ID, DEFAULT_USER_ID),
        AUTHENTICATED,
        eventLabels
    )

fun createAuthorizationEvent() = AuthorizationEvent(
    CREATED_AT,
    AUTHORIZED,
    AuthorizationPayload.UserInfo(DEFAULT_PROJECT_ID, DEFAULT_USER_ID),
    eventLabels
)

fun createCandidateReadEvent() =
    CandidateReadEvent(CREATED_AT, ENDED_AT, GUID1, FOUND, NOT_FOUND, eventLabels)

fun createCompletionCheckEvent() = CompletionCheckEvent(CREATED_AT, true, eventLabels)

fun createConnectivitySnapshotEvent() =
    ConnectivitySnapshotEvent(
        CREATED_AT,
        listOf(
            Connection(
                SimNetworkUtils.ConnectionType.MOBILE,
                SimNetworkUtils.ConnectionState.CONNECTED
            )
        ),
        eventLabels
    )

fun createConsentEvent() = ConsentEvent(CREATED_AT, ENDED_AT, INDIVIDUAL, ACCEPTED, eventLabels)

fun createEnrolmentEventV2() =
    EnrolmentEventV2(
        CREATED_AT,
        GUID1,
        DEFAULT_PROJECT_ID,
        DEFAULT_MODULE_ID,
        DEFAULT_USER_ID,
        GUID2,
        eventLabels
    )

fun createEnrolmentEventV1() = EnrolmentEventV1(CREATED_AT, GUID1, eventLabels)

fun createFingerprintCaptureEvent(): FingerprintCaptureEvent {
    val fingerprint = FingerprintCaptureEvent.FingerprintCapturePayload.Fingerprint(
        LEFT_THUMB,
        8,
        FingerprintTemplateFormat.ISO_19794_2
    )

    return FingerprintCaptureEvent(
        createdAt = CREATED_AT,
        endTime = ENDED_AT,
        finger = LEFT_THUMB,
        qualityThreshold = 10,
        result = FingerprintCaptureEvent.FingerprintCapturePayload.Result.BAD_QUALITY,
        fingerprint = fingerprint,
        labels = eventLabels,
        payloadId = "payloadId"
    )
}

fun createFingerprintCaptureBiometricsEvent(): FingerprintCaptureBiometricsEvent {
    val fingerprint =
        FingerprintCaptureBiometricsEvent.FingerprintCaptureBiometricsPayload.Fingerprint(
            LEFT_THUMB,
            "sometemplate",
            10,
            FingerprintTemplateFormat.ISO_19794_2
        )

    return FingerprintCaptureBiometricsEvent(
        createdAt = CREATED_AT,

        fingerprint = fingerprint,
        labels = eventLabels,
        payloadId = "payloadId"
    )
}

fun createGuidSelectionEvent() = GuidSelectionEvent(CREATED_AT, GUID1, eventLabels)

fun createIntentParsingEvent() = IntentParsingEvent(CREATED_AT, COMMCARE, eventLabels)

fun createInvalidIntentEvent() =
    InvalidIntentEvent(CREATED_AT, "action", mapOf("extra_key" to "extra_value"), eventLabels)

fun createOneToManyMatchEvent(): OneToManyMatchEvent {
    val poolArg = MatchPool(PROJECT, 100)
    val resultArg = listOf(MatchEntry(GUID1, 0F))
    return OneToManyMatchEvent(CREATED_AT, ENDED_AT, poolArg, RANK_ONE, resultArg, eventLabels)
}

fun createOneToOneMatchEvent(): OneToOneMatchEvent {
    val matchEntry = MatchEntry(GUID1, 10F)
    return OneToOneMatchEvent(
        CREATED_AT,
        ENDED_AT,
        GUID1,
        SIM_AFIS,
        matchEntry,
        FingerComparisonStrategy.CROSS_FINGER_USING_MEAN_OF_MAX,
        eventLabels
    )
}

fun createPersonCreationEvent() =
    PersonCreationEvent(
        CREATED_AT,
        listOf(GUID1, GUID2),
        GUID1,
        listOf(GUID1, GUID2),
        GUID2,
        eventLabels
    )

fun createRefusalEvent() = RefusalEvent(CREATED_AT, ENDED_AT, OTHER, "other_text", eventLabels)

fun createScannerConnectionEvent() =
    ScannerConnectionEvent(
        CREATED_AT,
        ScannerInfo("scanner_id", "macaddress", VERO_1, "version"),
        eventLabels
    )

fun createScannerFirmwareUpdateEvent() =
    ScannerFirmwareUpdateEvent(
        CREATED_AT,
        ENDED_AT,
        "chip",
        "targetAppVersion",
        "error",
        eventLabels
    )

fun createSuspiciousIntentEvent() =
    SuspiciousIntentEvent(CREATED_AT, mapOf("extra_key" to "extra_value"), eventLabels)

fun createVero2InfoSnapshotEvent(): Vero2InfoSnapshotEvent {
    val vero2Version = Vero2Version.Vero2NewApiVersion("E-1", "cypressApp", "stmApp", "un20App")
    val batteryInfo = BatteryInfo(0, 1, 2, 3)
    return Vero2InfoSnapshotEvent(CREATED_AT, vero2Version, batteryInfo, eventLabels)
}
