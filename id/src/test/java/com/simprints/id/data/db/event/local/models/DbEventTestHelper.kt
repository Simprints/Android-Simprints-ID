package com.simprints.id.data.db.event.local.models

import android.net.NetworkInfo.DetailedState.CONNECTED
import android.os.Build
import android.os.Build.VERSION
import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_METADATA
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.data.db.event.EventRepositoryImpl
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
import com.simprints.id.data.db.event.domain.models.OneToManyMatchEvent.OneToManyMatchPayload.MatchPool
import com.simprints.id.data.db.event.domain.models.OneToManyMatchEvent.OneToManyMatchPayload.MatchPoolType.PROJECT
import com.simprints.id.data.db.event.domain.models.RefusalEvent.RefusalPayload.Answer.OTHER
import com.simprints.id.data.db.event.domain.models.ScannerConnectionEvent.ScannerConnectionPayload.ScannerGeneration.VERO_1
import com.simprints.id.data.db.event.domain.models.ScannerConnectionEvent.ScannerConnectionPayload.ScannerInfo
import com.simprints.id.data.db.event.domain.models.Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload.BatteryInfo
import com.simprints.id.data.db.event.domain.models.Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload.Vero2Version
import com.simprints.id.data.db.event.domain.models.callback.*
import com.simprints.id.data.db.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload
import com.simprints.id.data.db.event.domain.models.callback.ErrorCallbackEvent.ErrorCallbackPayload.Reason.DIFFERENT_PROJECT_ID_SIGNED_IN
import com.simprints.id.data.db.event.domain.models.callout.*
import com.simprints.id.data.db.event.domain.models.session.DatabaseInfo
import com.simprints.id.data.db.event.domain.models.session.Device
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent
import com.simprints.id.data.db.subject.domain.FingerIdentifier.LEFT_THUMB
import com.simprints.id.domain.moduleapi.app.responses.entities.Tier.TIER_1
import com.simprints.id.orchestrator.SOME_GUID1
import com.simprints.id.orchestrator.SOME_GUID2
import com.simprints.id.tools.utils.SimNetworkUtils.Connection
import java.util.*

fun createConfirmationCallbackEvent() = ConfirmationCallbackEvent(CREATED_AT, true, SOME_GUID1)
fun verifyConfirmationCallbackEvents(event1: ConfirmationCallbackEvent, event2: ConfirmationCallbackEvent) {
    val payload1 = event1.payload
    val payload2 = event2.payload
    verifyEvents(event1, event2)
    verifyPayloads(payload1, payload2)
    assertThat(payload1.identificationOutcome).isEqualTo(payload2.identificationOutcome)
}

fun createEnrolmentCallbackEvent() = EnrolmentCallbackEvent(CREATED_AT, SOME_GUID1, SOME_GUID1)
fun verifyEnrolmentCallbackEvents(event1: EnrolmentCallbackEvent, event2: EnrolmentCallbackEvent) {
    val payload1 = event1.payload
    val payload2 = event2.payload
    verifyEvents(event1, event2)
    verifyPayloads(payload1, payload2)
    assertThat(payload1.guid).isEqualTo(payload2.guid)
}

fun createErrorCallbackEvent() = ErrorCallbackEvent(CREATED_AT, DIFFERENT_PROJECT_ID_SIGNED_IN, SOME_GUID1)
fun verifyErrorCallbackEvents(event1: ErrorCallbackEvent, event2: ErrorCallbackEvent) {
    val payload1 = event1.payload
    val payload2 = event2.payload as ErrorCallbackPayload
    verifyEvents(event1, event2)
    verifyPayloads(payload1, payload2)
    assertThat(payload1.reason).isEqualTo(payload2.reason)
}

fun createIdentificationCallbackEvent(): IdentificationCallbackEvent {
    val comparisonScore = CallbackComparisonScore(SOME_GUID1, 1, TIER_1)
    return IdentificationCallbackEvent(CREATED_AT, SOME_GUID1, listOf(comparisonScore))
}

fun verifyIdentificationCallbackEvents(event1: IdentificationCallbackEvent, event2: IdentificationCallbackEvent) {
    val payload1 = event1.payload
    val payload2 = event2.payload
    verifyEvents(event1, event2)
    verifyPayloads(payload1, payload2)
    assertThat(payload1.sessionId).isEqualTo(payload2.sessionId)
    assertThat(payload1.scores).containsAllIn(payload2.scores)
}

fun createRefusalCallbackEvent() = RefusalCallbackEvent(CREATED_AT, "some_reason", "extra", SOME_GUID1)
fun verifyRefusalCallbackEvents(event1: RefusalCallbackEvent, event2: RefusalCallbackEvent) {
    val payload1 = event1.payload
    val payload2 = event2.payload
    verifyEvents(event1, event2)
    verifyPayloads(payload1, payload2)
    assertThat(payload1.reason).isEqualTo(payload2.reason)
    assertThat(payload1.extra).isEqualTo(payload2.extra)
}

fun createVerificationCallbackEvent(): VerificationCallbackEvent {
    val comparisonScore = CallbackComparisonScore(SOME_GUID1, 1, TIER_1)
    return VerificationCallbackEvent(CREATED_AT, comparisonScore, SOME_GUID1)
}

fun verifyVerificationCallbackEvents(event1: VerificationCallbackEvent, event2: VerificationCallbackEvent) {
    val payload1 = event1.payload
    val payload2 = event2.payload
    verifyEvents(event1, event2)
    verifyPayloads(payload1, payload2)
    assertThat(payload1.score).isEqualTo(payload2.score)
}

fun createConfirmationCalloutEvent() = ConfirmationCalloutEvent(CREATED_AT, DEFAULT_PROJECT_ID, SOME_GUID1, SOME_GUID2)
fun verifyConfirmationCalloutEvents(event1: ConfirmationCalloutEvent, event2: ConfirmationCalloutEvent) {
    val payload1 = event1.payload
    val payload2 = event2.payload
    verifyEvents(event1, event2)
    verifyPayloads(payload1, payload2)
    assertThat(payload1.selectedGuid).isEqualTo(payload2.selectedGuid)
    assertThat(payload1.sessionId).isEqualTo(payload2.sessionId)
}

fun createEnrolmentCalloutEvent() = EnrolmentCalloutEvent(CREATED_AT, DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, DEFAULT_METADATA, SOME_GUID1)
fun verifyEnrolmentCalloutEvents(event1: EnrolmentCalloutEvent, event2: EnrolmentCalloutEvent) {
    val payload1 = event1.payload
    val payload2 = event2.payload
    verifyEvents(event1, event2)
    verifyPayloads(payload1, payload2)
    assertThat(payload1.projectId).isEqualTo(payload2.projectId)
    assertThat(payload1.userId).isEqualTo(payload2.userId)
    assertThat(payload1.moduleId).isEqualTo(payload2.moduleId)
    assertThat(payload1.metadata).isEqualTo(payload2.metadata)
}

fun createIdentificationCalloutEvent() = IdentificationCalloutEvent(CREATED_AT, DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, DEFAULT_METADATA, SOME_GUID1)
fun verifyIdentificationCalloutEvents(event1: IdentificationCalloutEvent, event2: IdentificationCalloutEvent) {
    val payload1 = event1.payload
    val payload2 = event2.payload
    verifyEvents(event1, event2)
    verifyPayloads(payload1, payload2)
    assertThat(payload1.projectId).isEqualTo(payload2.projectId)
    assertThat(payload1.userId).isEqualTo(payload2.userId)
    assertThat(payload1.moduleId).isEqualTo(payload2.moduleId)
    assertThat(payload1.metadata).isEqualTo(payload2.metadata)
}

fun createLastBiometricsEnrolmentCalloutEvent() = EnrolmentLastBiometricsCalloutEvent(CREATED_AT, DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, DEFAULT_METADATA, SOME_GUID1)
fun verifyEnrolmentLastBiometricsCalloutEvents(event1: EnrolmentLastBiometricsCalloutEvent, event2: EnrolmentLastBiometricsCalloutEvent) {
    val payload1 = event1.payload
    val payload2 = event2.payload
    verifyEvents(event1, event2)
    verifyPayloads(payload1, payload2)
    assertThat(payload1.projectId).isEqualTo(payload2.projectId)
    assertThat(payload1.userId).isEqualTo(payload2.userId)
    assertThat(payload1.moduleId).isEqualTo(payload2.moduleId)
    assertThat(payload1.metadata).isEqualTo(payload2.metadata)
}

fun createVerificationCalloutEvent() = VerificationCalloutEvent(CREATED_AT, DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, DEFAULT_METADATA, SOME_GUID1)
fun verifyVerificationCalloutEvents(event1: VerificationCalloutEvent, event2: VerificationCalloutEvent) {
    val payload1 = event1.payload
    val payload2 = event2.payload
    verifyEvents(event1, event2)
    verifyPayloads(payload1, payload2)
    assertThat(payload1.projectId).isEqualTo(payload2.projectId)
    assertThat(payload1.userId).isEqualTo(payload2.userId)
    assertThat(payload1.moduleId).isEqualTo(payload2.moduleId)
    assertThat(payload1.metadata).isEqualTo(payload2.metadata)
}

fun createSessionCaptureEvent() =
    SessionCaptureEvent(
        CREATED_AT,
        UUID.randomUUID().toString(),
        EventRepositoryImpl.PROJECT_ID_FOR_NOT_SIGNED_IN,
        "appVersionName",
        "libVersionName",
        "EN",
        Device(
            VERSION.SDK_INT.toString(),
            Build.MANUFACTURER + "_" + Build.MODEL,
            SOME_GUID1),
        DatabaseInfo(0))

fun verifySessionCaptureEvents(event1: SessionCaptureEvent, event2: SessionCaptureEvent) {
    val payload1 = event1.payload
    val payload2 = event2.payload
    verifyEvents(event1, event2)
    verifyPayloads(payload1, payload2)
    assertThat(payload1.projectId).isEqualTo(payload2.projectId)
    assertThat(payload1.uploadTime).isEqualTo(payload2.uploadTime)
    assertThat(payload1.libVersionName).isEqualTo(payload2.libVersionName)
    assertThat(payload1.analyticsId).isEqualTo(payload2.analyticsId)
    assertThat(payload1.appVersionName).isEqualTo(payload2.appVersionName)
    assertThat(payload1.databaseInfo).isEqualTo(payload2.databaseInfo)
    assertThat(payload1.device).isEqualTo(payload2.device)
    assertThat(payload1.id).isEqualTo(payload2.id)
    assertThat(payload1.language).isEqualTo(payload2.language)
    assertThat(payload1.location).isEqualTo(payload2.location)
}

fun createAlertScreenEvent() = AlertScreenEvent(CREATED_AT, BLUETOOTH_NOT_ENABLED, SOME_GUID1)
fun verifyAlertScreenEvents(event1: AlertScreenEvent, event2: AlertScreenEvent) {
    val payload1 = event1.payload
    val payload2 = event2.payload
    verifyEvents(event1, event2)
    verifyPayloads(payload1, payload2)
    assertThat(payload1.alertType).isEqualTo(payload2.alertType)
}

fun createArtificialTerminationEvent() = ArtificialTerminationEvent(CREATED_AT, NEW_SESSION, SOME_GUID1)
fun verifyArtificialTerminationEvents(event1: ArtificialTerminationEvent, event2: ArtificialTerminationEvent) {
    val payload1 = event1.payload
    val payload2 = event2.payload
    verifyEvents(event1, event2)
    verifyPayloads(payload1, payload2)
    assertThat(payload1.reason).isEqualTo(payload2.reason)
}

fun createAuthenticationEvent() = AuthenticationEvent(CREATED_AT, ENDED_AT, UserInfo(DEFAULT_PROJECT_ID, DEFAULT_USER_ID), AUTHENTICATED, SOME_GUID1)
fun verifyAuthenticationEvents(event1: AuthenticationEvent, event2: AuthenticationEvent) {
    val payload1 = event1.payload
    val payload2 = event2.payload
    verifyEvents(event1, event2)
    verifyPayloads(payload1, payload2)
    assertThat(payload1.userInfo).isEqualTo(payload2.userInfo)
    assertThat(payload1.result).isEqualTo(payload2.result)
}

fun createAuthorizationEvent() = AuthorizationEvent(CREATED_AT, AUTHORIZED, AuthorizationPayload.UserInfo(DEFAULT_PROJECT_ID, DEFAULT_USER_ID), SOME_GUID1)
fun verifyAuthorizationEvents(event1: AuthorizationEvent, event2: AuthorizationEvent) {
    val payload1 = event1.payload
    val payload2 = event2.payload
    verifyEvents(event1, event2)
    verifyPayloads(payload1, payload2)
    assertThat(payload1.userInfo).isEqualTo(payload2.userInfo)
    assertThat(payload1.result).isEqualTo(payload2.result)
}

fun createCandidateReadEvent() = CandidateReadEvent(CREATED_AT, ENDED_AT, SOME_GUID1, FOUND, NOT_FOUND, SOME_GUID1)
fun verifyCandidateReadEvents(event1: CandidateReadEvent, event2: CandidateReadEvent) {
    val payload1 = event1.payload
    val payload2 = event2.payload
    verifyEvents(event1, event2)
    verifyPayloads(payload1, payload2)
    assertThat(payload1.candidateId).isEqualTo(payload2.candidateId)
    assertThat(payload1.localResult).isEqualTo(payload2.localResult)
    assertThat(payload1.remoteResult).isEqualTo(payload2.remoteResult)
}

fun createCompletionCheckEvent() = CompletionCheckEvent(CREATED_AT, true, SOME_GUID1)
fun verifyCompletionCheckEvents(event1: CompletionCheckEvent, event2: CompletionCheckEvent) {
    val payload1 = event1.payload
    val payload2 = event2.payload
    verifyEvents(event1, event2)
    verifyPayloads(payload1, payload2)
    assertThat(payload1.completed).isEqualTo(payload2.completed)
}

fun createConnectivitySnapshotEvent() = ConnectivitySnapshotEvent(CREATED_AT, "wifi", listOf(Connection("GPRS", CONNECTED)))
fun verifyConnectivitySnapshotEvents(event1: ConnectivitySnapshotEvent, event2: ConnectivitySnapshotEvent) {
    val payload1 = event1.payload
    val payload2 = event2.payload
    verifyEvents(event1, event2)
    verifyPayloads(payload1, payload2)
    assertThat(payload1.networkType).isEqualTo(payload2.networkType)
    assertThat(payload1.connections).containsAllIn(payload2.connections)
}

fun createConsentEvent() = ConsentEvent(CREATED_AT, ENDED_AT, INDIVIDUAL, ACCEPTED, SOME_GUID1)
fun verifyConsentEvents(event1: ConsentEvent, event2: ConsentEvent) {
    val payload1 = event1.payload
    val payload2 = event2.payload
    verifyEvents(event1, event2)
    verifyPayloads(payload1, payload2)
    assertThat(payload1.consentType).isEqualTo(payload2.consentType)
    assertThat(payload1.result).isEqualTo(payload2.result)
}

fun createEnrolmentEvent() = EnrolmentEvent(CREATED_AT, SOME_GUID1, SOME_GUID2)
fun verifyEnrolmentEvents(event1: EnrolmentEvent, event2: EnrolmentEvent) {
    val payload1 = event1.payload
    val payload2 = event2.payload
    verifyEvents(event1, event2)
    verifyPayloads(payload1, payload2)
    assertThat(payload1.personId).isEqualTo(payload2.personId)
}

fun createFingerprintCaptureEvent(): FingerprintCaptureEvent {
    val fingerprint = Fingerprint(LEFT_THUMB, 8, "template")
    return FingerprintCaptureEvent(CREATED_AT, ENDED_AT, LEFT_THUMB, 10, BAD_QUALITY, fingerprint, SOME_GUID1, SOME_GUID1)
}

fun verifyFingerprintCaptureEvents(event1: FingerprintCaptureEvent, event2: FingerprintCaptureEvent) {
    val payload1 = event1.payload
    val payload2 = event2.payload
    verifyEvents(event1, event2)
    verifyPayloads(payload1, payload2)
    assertThat(payload1.result).isEqualTo(payload2.result)
    assertThat(payload1.qualityThreshold).isEqualTo(payload2.qualityThreshold)
    assertThat(payload1.finger).isEqualTo(payload2.finger)
    assertThat(payload1.fingerprint).isEqualTo(payload2.fingerprint)
}

fun createGuidSelectionEvent() = GuidSelectionEvent(CREATED_AT, SOME_GUID1, SOME_GUID2)
fun verifyGuidSelectionEvents(event1: GuidSelectionEvent, event2: GuidSelectionEvent) {
    val payload1 = event1.payload
    val payload2 = event2.payload
    verifyEvents(event1, event2)
    verifyPayloads(payload1, payload2)
    assertThat(payload1.selectedId).isEqualTo(payload2.selectedId)
}

fun createIntentParsingEvent() = IntentParsingEvent(CREATED_AT, COMMCARE, SOME_GUID2)
fun verifyIntentParsingEvents(event1: IntentParsingEvent, event2: IntentParsingEvent) {
    val payload1 = event1.payload
    val payload2 = event2.payload
    verifyEvents(event1, event2)
    verifyPayloads(payload1, payload2)
    assertThat(payload1.integration).isEqualTo(payload2.integration)
}

fun createInvalidIntentEvent() = InvalidIntentEvent(CREATED_AT, "action", mapOf("extra_key" to "extra_value"), SOME_GUID2)
fun verifyInvalidIntentEvents(event1: InvalidIntentEvent, event2: InvalidIntentEvent) {
    val payload1 = event1.payload
    val payload2 = event2.payload
    verifyEvents(event1, event2)
    verifyPayloads(payload1, payload2)
    assertThat(payload1.action).isEqualTo(payload2.action)
    assertThat(payload1.extras).isEqualTo(payload2.extras)
}

fun createOneToManyMatchEvent(): OneToManyMatchEvent {
    val poolArg = MatchPool(PROJECT, 100)
    val resultArg = listOf(MatchEntry(SOME_GUID1, 0F))
    return OneToManyMatchEvent(CREATED_AT, ENDED_AT, poolArg, resultArg, SOME_GUID1)
}

fun verifyOneToManyMatchEvents(event1: OneToManyMatchEvent, event2: OneToManyMatchEvent) {
    val payload1 = event1.payload
    val payload2 = event2.payload
    verifyEvents(event1, event2)
    verifyPayloads(payload1, payload2)
    assertThat(payload1.pool).isEqualTo(payload2.pool)
    assertThat(payload1.result).isEqualTo(payload2.result)
}

fun createOneToOneMatchEvent(): OneToOneMatchEvent {
    val matchEntry = MatchEntry(SOME_GUID1, 10F)
    return OneToOneMatchEvent(CREATED_AT, ENDED_AT, SOME_GUID1, matchEntry, SOME_GUID1)
}

fun verifyOneToOneMatchEvents(event1: OneToOneMatchEvent, event2: OneToOneMatchEvent) {
    val payload1 = event1.payload
    val payload2 = event2.payload
    verifyEvents(event1, event2)
    verifyPayloads(payload1, payload2)
    assertThat(payload1.candidateId).isEqualTo(payload2.candidateId)
    assertThat(payload1.result).isEqualTo(payload2.result)
}

fun createPersonCreationEvent() = PersonCreationEvent(CREATED_AT, listOf(SOME_GUID1, SOME_GUID2), SOME_GUID2)
fun verifyPersonCreationEvents(event1: PersonCreationEvent, event2: PersonCreationEvent) {
    val payload1 = event1.payload
    val payload2 = event2.payload
    verifyEvents(event1, event2)
    verifyPayloads(payload1, payload2)
    assertThat(payload1.fingerprintCaptureIds).containsAllIn(payload2.fingerprintCaptureIds)
}

fun createRefusalEvent() = RefusalEvent(CREATED_AT, ENDED_AT, OTHER, "other_text", SOME_GUID2)
fun verifyRefusalEvents(event1: RefusalEvent, event2: RefusalEvent) {
    val payload1 = event1.payload
    val payload2 = event2.payload
    verifyEvents(event1, event2)
    verifyPayloads(payload1, payload2)
    assertThat(payload1.reason).isEqualTo(payload2.reason)
    assertThat(payload1.otherText).isEqualTo(payload2.otherText)
}

fun createScannerConnectionEvent() = ScannerConnectionEvent(CREATED_AT, ScannerInfo("scanner_id", "macaddress", VERO_1, "version"), SOME_GUID2)
fun verifyScannerConnectionEvents(event1: ScannerConnectionEvent, event2: ScannerConnectionEvent) {
    val payload1 = event1.payload
    val payload2 = event2.payload
    verifyEvents(event1, event2)
    verifyPayloads(payload1, payload2)
    assertThat(payload1.scannerInfo).isEqualTo(payload2.scannerInfo)
}

fun createScannerFirmwareUpdateEvent() = ScannerFirmwareUpdateEvent(CREATED_AT, ENDED_AT, "chip", "targetAppVersion", "error", SOME_GUID1)
fun verifyScannerFirmwareUpdateEvents(event1: ScannerFirmwareUpdateEvent, event2: ScannerFirmwareUpdateEvent) {
    val payload1 = event1.payload
    val payload2 = event2.payload
    verifyEvents(event1, event2)
    verifyPayloads(payload1, payload2)
    assertThat(payload1.chip).isEqualTo(payload2.chip)
    assertThat(payload1.targetAppVersion).isEqualTo(payload2.targetAppVersion)
    assertThat(payload1.failureReason).isEqualTo(payload2.failureReason)
}

fun createSuspiciousIntentEvent() = SuspiciousIntentEvent(CREATED_AT, mapOf("extra_key" to "extra_value"), SOME_GUID1)
fun verifySuspiciousIntentEvents(event1: SuspiciousIntentEvent, event2: SuspiciousIntentEvent) {
    val payload1 = event1.payload
    val payload2 = event2.payload
    verifyEvents(event1, event2)
    verifyPayloads(payload1, payload2)
    assertThat(payload1.unexpectedExtras).isEqualTo(payload2.unexpectedExtras)
}

fun createVero2InfoSnapshotEvent(): Vero2InfoSnapshotEvent {
    val vero2Version = Vero2Version(0, "cypressApp", "cypressApi", "stmApp", "stmApi", "un20App", "un20Api")
    val batteryInfo = BatteryInfo(0, 1, 2, 3)
    return Vero2InfoSnapshotEvent(CREATED_AT, vero2Version, batteryInfo)
}
fun verifyVero2InfoSnapshotEvents(event1: Vero2InfoSnapshotEvent, event2: Vero2InfoSnapshotEvent) {
    val payload1 = event1.payload
    val payload2 = event2.payload
    verifyEvents(event1, event2)
    verifyPayloads(payload1, payload2)
    assertThat(payload1.version).isEqualTo(payload2.version)
    assertThat(payload1.battery).isEqualTo(payload2.battery)
}

fun verifyPayloads(payload1: EventPayload, payload2: EventPayload) {
    assertThat(payload1.type).isEqualTo(payload2.type)
    assertThat(payload1.endedAt).isEqualTo(payload2.endedAt)
    assertThat(payload1.createdAt).isEqualTo(payload2.createdAt)
    assertThat(payload1.eventVersion).isEqualTo(payload2.eventVersion)
}

fun verifyEvents(event1: Event, event2: Event) {
    assertThat(event1.type).isEqualTo(event2.type)
    assertThat(event1.id).isEqualTo(event2.id)
    assertThat(event1.labels).isEqualTo(event2.labels)
}
