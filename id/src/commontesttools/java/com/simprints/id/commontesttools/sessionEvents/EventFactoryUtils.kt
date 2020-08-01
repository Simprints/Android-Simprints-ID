package com.simprints.id.commontesttools.sessionEvents

import android.net.NetworkInfo.DetailedState.CONNECTED
import android.os.Build
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_METADATA
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.data.db.event.domain.models.*
import com.simprints.id.data.db.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.BLUETOOTH_NOT_ENABLED
import com.simprints.id.data.db.event.domain.models.ArtificialTerminationEvent.ArtificialTerminationPayload.Reason.NEW_SESSION
import com.simprints.id.data.db.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result.AUTHENTICATED
import com.simprints.id.data.db.event.domain.models.AuthenticationEvent.AuthenticationPayload.UserInfo
import com.simprints.id.data.db.event.domain.models.AuthorizationEvent.AuthorizationPayload
import com.simprints.id.data.db.event.domain.models.AuthorizationEvent.AuthorizationPayload.AuthorizationResult.AUTHORIZED
import com.simprints.id.data.db.event.domain.models.CandidateReadEvent.CandidateReadPayload.LocalResult.FOUND
import com.simprints.id.data.db.event.domain.models.CandidateReadEvent.CandidateReadPayload.RemoteResult.NOT_FOUND
import com.simprints.id.data.db.event.domain.models.ConsentEvent.ConsentPayload.Result.ACCEPTED
import com.simprints.id.data.db.event.domain.models.ConsentEvent.ConsentPayload.Type.INDIVIDUAL
import com.simprints.id.data.db.event.domain.models.FingerprintCaptureEvent.FingerprintCapturePayload.Fingerprint
import com.simprints.id.data.db.event.domain.models.FingerprintCaptureEvent.FingerprintCapturePayload.Result.BAD_QUALITY
import com.simprints.id.data.db.event.domain.models.IntentParsingEvent.IntentParsingPayload.IntegrationInfo.COMMCARE
import com.simprints.id.data.db.event.domain.models.Matcher.RANK_ONE
import com.simprints.id.data.db.event.domain.models.OneToManyMatchEvent.OneToManyMatchPayload.MatchPool
import com.simprints.id.data.db.event.domain.models.OneToManyMatchEvent.OneToManyMatchPayload.MatchPoolType.PROJECT
import com.simprints.id.data.db.event.domain.models.RefusalEvent.RefusalPayload.Answer.OTHER
import com.simprints.id.data.db.event.domain.models.ScannerConnectionEvent.ScannerConnectionPayload.ScannerGeneration.VERO_1
import com.simprints.id.data.db.event.domain.models.ScannerConnectionEvent.ScannerConnectionPayload.ScannerInfo
import com.simprints.id.data.db.event.domain.models.Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload.BatteryInfo
import com.simprints.id.data.db.event.domain.models.Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload.Vero2Version
import com.simprints.id.data.db.event.domain.models.callback.*
import com.simprints.id.data.db.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason.DIFFERENT_PROJECT_ID_SIGNED_IN
import com.simprints.id.data.db.event.domain.models.callout.*
import com.simprints.id.data.db.event.domain.models.face.*
import com.simprints.id.data.db.event.domain.models.face.FaceCaptureConfirmationEvent.FaceCaptureConfirmationPayload.Result.CONTINUE
import com.simprints.id.data.db.event.domain.models.face.FaceCaptureEvent.FaceCapturePayload.Face
import com.simprints.id.data.db.event.domain.models.face.FaceCaptureEvent.FaceCapturePayload.Result.VALID
import com.simprints.id.data.db.event.domain.models.session.DatabaseInfo
import com.simprints.id.data.db.event.domain.models.session.Device
import com.simprints.id.data.db.event.domain.models.session.Location
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent
import com.simprints.id.data.db.event.domain.models.subject.*
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordMoveEvent.EnrolmentRecordCreationInMove
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordMoveEvent.EnrolmentRecordDeletionInMove
import com.simprints.id.data.db.event.domain.models.subject.FingerIdentifier.LEFT_3RD_FINGER
import com.simprints.id.data.db.subject.domain.FingerIdentifier.LEFT_THUMB
import com.simprints.id.domain.modality.Modes.FACE
import com.simprints.id.domain.modality.Modes.FINGERPRINT
import com.simprints.id.domain.moduleapi.app.responses.entities.Tier.TIER_1
import com.simprints.id.orchestrator.SOME_GUID1
import com.simprints.id.orchestrator.SOME_GUID2
import com.simprints.id.tools.utils.SimNetworkUtils.Connection

val eventLabels = EventLabels(sessionId = SOME_GUID1, deviceId = SOME_GUID1, projectId = SOME_GUID1, subjectId = SOME_GUID1)

fun createConfirmationCallbackEvent() = ConfirmationCallbackEvent(CREATED_AT, true, eventLabels)

fun createEnrolmentCallbackEvent() = EnrolmentCallbackEvent(CREATED_AT, SOME_GUID1, eventLabels)

fun createErrorCallbackEvent() = ErrorCallbackEvent(CREATED_AT, DIFFERENT_PROJECT_ID_SIGNED_IN, eventLabels)

fun createIdentificationCallbackEvent(): IdentificationCallbackEvent {
    val comparisonScore = CallbackComparisonScore(SOME_GUID1, 1, TIER_1)
    return IdentificationCallbackEvent(CREATED_AT, SOME_GUID1, listOf(comparisonScore), eventLabels)
}

fun createRefusalCallbackEvent() = RefusalCallbackEvent(CREATED_AT, "some_reason", "extra", eventLabels)

fun createVerificationCallbackEvent(): VerificationCallbackEvent {
    val comparisonScore = CallbackComparisonScore(SOME_GUID1, 1, TIER_1)
    return VerificationCallbackEvent(CREATED_AT, comparisonScore, eventLabels)
}

fun createConfirmationCalloutEvent() = ConfirmationCalloutEvent(CREATED_AT, DEFAULT_PROJECT_ID, SOME_GUID1, SOME_GUID2, eventLabels)

fun createEnrolmentCalloutEvent() = EnrolmentCalloutEvent(CREATED_AT, DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, DEFAULT_METADATA, eventLabels)

fun createIdentificationCalloutEvent() = IdentificationCalloutEvent(CREATED_AT, DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, DEFAULT_METADATA, eventLabels)

fun createLastBiometricsEnrolmentCalloutEvent() = EnrolmentLastBiometricsCalloutEvent(CREATED_AT, DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, DEFAULT_METADATA, SOME_GUID2, eventLabels)

fun createVerificationCalloutEvent() = VerificationCalloutEvent(CREATED_AT, DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, DEFAULT_METADATA, SOME_GUID2, eventLabels)

fun createFaceCaptureConfirmationEvent() = FaceCaptureConfirmationEvent(CREATED_AT, ENDED_AT, CONTINUE, eventLabels)

fun createFaceCaptureEvent(): FaceCaptureEvent {
    val faceArg = Face(0F, 1F, 2F, "")
    return FaceCaptureEvent(CREATED_AT, ENDED_AT, 0, 1F, VALID, true, faceArg, eventLabels)
}

fun createFaceFallbackCaptureEvent() = FaceFallbackCaptureEvent(CREATED_AT, ENDED_AT, eventLabels)

fun createFaceCaptureRetryEvent() = FaceCaptureRetryEvent(CREATED_AT, ENDED_AT, eventLabels)

fun createFaceOnboardingCompleteEvent() = FaceOnboardingCompleteEvent(CREATED_AT, ENDED_AT, eventLabels)

fun createSessionCaptureEvent(): SessionCaptureEvent {
    val deviceArg = Device(
        Build.VERSION.SDK_INT.toString(),
        Build.MANUFACTURER + "_" + Build.MODEL,
        SOME_GUID1)

    return SessionCaptureEvent(
        CREATED_AT,
        DEFAULT_PROJECT_ID,
        "appVersionName",
        "libSimprintsVersionName",
        "EN",
        deviceArg,
        DatabaseInfo(0, 2),
        ENDED_AT,
        ENDED_AT,
        Location(0.0, 0.0),
        labels = EventLabels(deviceId = SOME_GUID1, projectId = SOME_GUID1)
    )
}


private fun buildFakeBiometricReferences(): List<BiometricReference> {
    val fingerprintReference = FingerprintReference(listOf(FingerprintTemplate(0, "some_template", LEFT_3RD_FINGER)), hashMapOf("some_key" to "some_value"))
    val faceReference = FaceReference(listOf(FaceTemplate("some_template")))
    return listOf(fingerprintReference, faceReference)
}

fun createEnrolmentRecordCreationEvent() = EnrolmentRecordCreationEvent(CREATED_AT, SOME_GUID1, DEFAULT_PROJECT_ID, DEFAULT_MODULE_ID, DEFAULT_USER_ID, listOf(FINGERPRINT, FACE), buildFakeBiometricReferences(), eventLabels)

fun createEnrolmentRecordDeletionEvent() = EnrolmentRecordDeletionEvent(CREATED_AT, SOME_GUID1, DEFAULT_PROJECT_ID, DEFAULT_MODULE_ID, DEFAULT_USER_ID, eventLabels)

fun createEnrolmentRecordMoveEvent() =
    EnrolmentRecordMoveEvent(
        CREATED_AT,
        EnrolmentRecordCreationInMove(SOME_GUID1, DEFAULT_PROJECT_ID, DEFAULT_MODULE_ID, DEFAULT_USER_ID, createBiometricReferences()),
        EnrolmentRecordDeletionInMove(SOME_GUID1, DEFAULT_PROJECT_ID, DEFAULT_MODULE_ID, DEFAULT_USER_ID),
        eventLabels
    )

private fun createBiometricReferences(): List<BiometricReference> {
    val fingerprintReference = FingerprintReference(listOf(FingerprintTemplate(0, "some_template", LEFT_3RD_FINGER)), hashMapOf("some_key" to "some_value"))
    val faceReference = FaceReference(listOf(FaceTemplate("some_template")))
    return listOf(fingerprintReference, faceReference)
}

fun createAlertScreenEvent() = AlertScreenEvent(CREATED_AT, BLUETOOTH_NOT_ENABLED, eventLabels)

fun createArtificialTerminationEvent() = ArtificialTerminationEvent(CREATED_AT, NEW_SESSION, eventLabels)

fun createAuthenticationEvent() = AuthenticationEvent(CREATED_AT, ENDED_AT, UserInfo(DEFAULT_PROJECT_ID, DEFAULT_USER_ID), AUTHENTICATED, eventLabels)

fun createAuthorizationEvent() = AuthorizationEvent(CREATED_AT, AUTHORIZED, AuthorizationPayload.UserInfo(DEFAULT_PROJECT_ID, DEFAULT_USER_ID), eventLabels)

fun createCandidateReadEvent() = CandidateReadEvent(CREATED_AT, ENDED_AT, SOME_GUID1, FOUND, NOT_FOUND, eventLabels)

fun createCompletionCheckEvent() = CompletionCheckEvent(CREATED_AT, true, eventLabels)

fun createConnectivitySnapshotEvent() = ConnectivitySnapshotEvent(CREATED_AT, "wifi", listOf(Connection("GPRS", CONNECTED)), eventLabels)

fun createConsentEvent() = ConsentEvent(CREATED_AT, ENDED_AT, INDIVIDUAL, ACCEPTED, eventLabels)

fun createEnrolmentEvent() = EnrolmentEvent(CREATED_AT, SOME_GUID1, eventLabels)

fun createFingerprintCaptureEvent(): FingerprintCaptureEvent {
    val fingerprint = Fingerprint(LEFT_THUMB, 8, "template")
    return FingerprintCaptureEvent(CREATED_AT, ENDED_AT, LEFT_THUMB, 10, BAD_QUALITY, fingerprint, SOME_GUID1, eventLabels)
}

fun createGuidSelectionEvent() = GuidSelectionEvent(CREATED_AT, SOME_GUID1, eventLabels)

fun createIntentParsingEvent() = IntentParsingEvent(CREATED_AT, COMMCARE, eventLabels)

fun createInvalidIntentEvent() = InvalidIntentEvent(CREATED_AT, "action", mapOf("extra_key" to "extra_value"), eventLabels)

fun createOneToManyMatchEvent(): OneToManyMatchEvent {
    val poolArg = MatchPool(PROJECT, 100)
    val resultArg = listOf(MatchEntry(SOME_GUID1, 0F))
    return OneToManyMatchEvent(CREATED_AT, ENDED_AT, poolArg, RANK_ONE, resultArg, eventLabels)
}

fun createOneToOneMatchEvent(): OneToOneMatchEvent {
    val matchEntry = MatchEntry(SOME_GUID1, 10F)
    return OneToOneMatchEvent(CREATED_AT, ENDED_AT, SOME_GUID1, RANK_ONE, matchEntry, eventLabels)
}

fun createPersonCreationEvent() = PersonCreationEvent(CREATED_AT, listOf(SOME_GUID1, SOME_GUID2), listOf(SOME_GUID1, SOME_GUID2), eventLabels)

fun createRefusalEvent() = RefusalEvent(CREATED_AT, ENDED_AT, OTHER, "other_text", eventLabels)

fun createScannerConnectionEvent() = ScannerConnectionEvent(CREATED_AT, ScannerInfo("scanner_id", "macaddress", VERO_1, "version"), eventLabels)

fun createScannerFirmwareUpdateEvent() =
    ScannerFirmwareUpdateEvent(CREATED_AT, ENDED_AT, "chip", "targetAppVersion", "error", eventLabels)

fun createSuspiciousIntentEvent() = SuspiciousIntentEvent(CREATED_AT, mapOf("extra_key" to "extra_value"), eventLabels)

fun createVero2InfoSnapshotEvent(): Vero2InfoSnapshotEvent {
    val vero2Version = Vero2Version(0, "cypressApp", "cypressApi", "stmApp", "stmApi", "un20App", "un20Api")
    val batteryInfo = BatteryInfo(0, 1, 2, 3)
    return Vero2InfoSnapshotEvent(CREATED_AT, vero2Version, batteryInfo, eventLabels)
}
