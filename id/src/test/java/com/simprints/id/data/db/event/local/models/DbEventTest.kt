package com.simprints.id.data.db.event.local.models

import com.simprints.id.data.db.event.domain.models.*
import com.simprints.id.data.db.event.domain.models.callback.*
import com.simprints.id.data.db.event.domain.models.callout.*
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent
import org.junit.Test

class DbEventTest {

    @Test
    fun convert_ConfirmationCallbackEvent() {
        val original = createConfirmationCallbackEvent()
        val transformed = original.fromDomainToDb()

        verifyConfirmationCallbackEvents(original, transformed.fromDbToDomain() as ConfirmationCallbackEvent)
    }

    @Test
    fun convert_EnrolmentCallbackEvent() {
        val original = createEnrolmentCallbackEvent()
        val transformed = original.fromDomainToDb()

        verifyEnrolmentCallbackEvents(original, transformed.fromDbToDomain() as EnrolmentCallbackEvent)
    }

    @Test
    fun convert_ErrorCallbackEvent() {
        val original = createErrorCallbackEvent()
        val transformed = original.fromDomainToDb()

        verifyErrorCallbackEvents(original, transformed.fromDbToDomain() as ErrorCallbackEvent)
    }

    @Test
    fun convert_IdentificationCallbackEvent() {
        val original = createIdentificationCallbackEvent()
        val transformed = original.fromDomainToDb()

        verifyIdentificationCallbackEvents(original, transformed.fromDbToDomain() as IdentificationCallbackEvent)
    }

    @Test
    fun convert_RefusalCallbackEvent() {
        val original = createRefusalCallbackEvent()
        val transformed = original.fromDomainToDb()

        verifyRefusalCallbackEvents(original, transformed.fromDbToDomain() as RefusalCallbackEvent)
    }

    @Test
    fun convert_VerificationCallbackEvent() {
        val original = createVerificationCallbackEvent()
        val transformed = original.fromDomainToDb()

        verifyVerificationCallbackEvents(original, transformed.fromDbToDomain() as VerificationCallbackEvent)
    }

    @Test
    fun convert_ConfirmationCalloutEvent() {
        val original = createConfirmationCalloutEvent()
        val transformed = original.fromDomainToDb()

        verifyConfirmationCalloutEvents(original, transformed.fromDbToDomain() as ConfirmationCalloutEvent)
    }

    @Test
    fun convert_EnrolmentCalloutEvent() {
        val original = createEnrolmentCalloutEvent()
        val transformed = original.fromDomainToDb()

        verifyEnrolmentCalloutEvents(original, transformed.fromDbToDomain() as EnrolmentCalloutEvent)
    }

    @Test
    fun convert_EnrolmentLastBiometricsCalloutEvent() {
        val original = createLastBiometricsEnrolmentCalloutEvent()
        val transformed = original.fromDomainToDb()

        verifyEnrolmentLastBiometricsCalloutEvents(original, transformed.fromDbToDomain() as EnrolmentLastBiometricsCalloutEvent)
    }

    @Test
    fun convert_IdentificationCalloutEvent() {
        val original = createIdentificationCalloutEvent()
        val transformed = original.fromDomainToDb()

        verifyIdentificationCalloutEvents(original, transformed.fromDbToDomain() as IdentificationCalloutEvent)
    }

    @Test
    fun convert_VerificationCalloutEvent() {
        val original = createVerificationCalloutEvent()
        val transformed = original.fromDomainToDb()

        verifyVerificationCalloutEvents(original, transformed.fromDbToDomain() as VerificationCalloutEvent)
    }

    @Test
    fun convert_SessionCaptureEvent() {
        val original = createSessionCaptureEvent()
        val transformed = original.fromDomainToDb()

        verifySessionCaptureEvents(original, transformed.fromDbToDomain() as SessionCaptureEvent)
    }

    @Test
    fun convert_AlertScreenEvent() {
        val original = createAlertScreenEvent()
        val transformed = original.fromDomainToDb()

        verifyAlertScreenEvents(original, transformed.fromDbToDomain() as AlertScreenEvent)
    }

    @Test
    fun convert_ArtificialTerminationEvent() {
        val original = createArtificialTerminationEvent()
        val transformed = original.fromDomainToDb()

        verifyArtificialTerminationEvents(original, transformed.fromDbToDomain() as ArtificialTerminationEvent)
    }

    @Test
    fun convert_AuthenticationEvent() {
        val original = createAuthenticationEvent()
        val transformed = original.fromDomainToDb()

        verifyAuthenticationEvents(original, transformed.fromDbToDomain() as AuthenticationEvent)
    }

    @Test
    fun convert_AuthorizationEvent() {
        val original = createAuthorizationEvent()
        val transformed = original.fromDomainToDb()

        verifyAuthorizationEvents(original, transformed.fromDbToDomain() as AuthorizationEvent)
    }

    @Test
    fun convert_CandidateReadEvent() {
        val original = createCandidateReadEvent()
        val transformed = original.fromDomainToDb()

        verifyCandidateReadEvents(original, transformed.fromDbToDomain() as CandidateReadEvent)
    }

    @Test
    fun convert_CompletionCheckEvent() {
        val original = createCompletionCheckEvent()
        val transformed = original.fromDomainToDb()

        verifyCompletionCheckEvents(original, transformed.fromDbToDomain() as CompletionCheckEvent)
    }

    @Test
    fun convert_ConnectivitySnapshotEvent() {
        val original = createConnectivitySnapshotEvent()
        val transformed = original.fromDomainToDb()

        verifyConnectivitySnapshotEvents(original, transformed.fromDbToDomain() as ConnectivitySnapshotEvent)
    }

    @Test
    fun convert_ConsentEvent() {
        val original = createConsentEvent()
        val transformed = original.fromDomainToDb()

        verifyConsentEvents(original, transformed.fromDbToDomain() as ConsentEvent)
    }

    @Test
    fun convert_EnrolmentEvent() {
        val original = createEnrolmentEvent()
        val transformed = original.fromDomainToDb()

        verifyEnrolmentEvents(original, transformed.fromDbToDomain() as EnrolmentEvent)
    }

    @Test
    fun convert_FingerprintCaptureEvent() {
        val original = createFingerprintCaptureEvent()
        val transformed = original.fromDomainToDb()

        verifyFingerprintCaptureEvents(original, transformed.fromDbToDomain() as FingerprintCaptureEvent)
    }

    @Test
    fun convert_GuidSelectionEvent() {
        val original = createGuidSelectionEvent()
        val transformed = original.fromDomainToDb()

        verifyGuidSelectionEvents(original, transformed.fromDbToDomain() as GuidSelectionEvent)
    }

    @Test
    fun covert_IntentParsingEvent() {
        val original = createIntentParsingEvent()
        val transformed = original.fromDomainToDb()

        verifyIntentParsingEvents(original, transformed.fromDbToDomain() as IntentParsingEvent)
    }

    @Test
    fun convert_InvalidIntentEvent() {
        val original = createInvalidIntentEvent()
        val transformed = original.fromDomainToDb()

        verifyInvalidIntentEvents(original, transformed.fromDbToDomain() as InvalidIntentEvent)
    }

    @Test
    fun convert_OneToManyMatchEvent() {
        val original = createOneToManyMatchEvent()
        val transformed = original.fromDomainToDb()

        verifyOneToManyMatchEvents(original, transformed.fromDbToDomain() as OneToManyMatchEvent)
    }

    @Test
    fun convert_OneToOneMatchEvent() {
        val original = createOneToOneMatchEvent()
        val transformed = original.fromDomainToDb()

        verifyOneToOneMatchEvents(original, transformed.fromDbToDomain() as OneToOneMatchEvent)
    }

    @Test
    fun convert_PersonCreationEvent() {
        val original = createPersonCreationEvent()
        val transformed = original.fromDomainToDb()

        verifyPersonCreationEvents(original, transformed.fromDbToDomain() as PersonCreationEvent)
    }

    @Test
    fun convert_RefusalEvent() {
        val original = createRefusalEvent()
        val transformed = original.fromDomainToDb()

        verifyRefusalEvents(original, transformed.fromDbToDomain() as RefusalEvent)
    }

    @Test
    fun convert_ScannerConnectionEvent() {
        val original = createScannerConnectionEvent()
        val transformed = original.fromDomainToDb()

        verifyScannerConnectionEvents(original, transformed.fromDbToDomain() as ScannerConnectionEvent)
    }

    @Test
    fun convert_ScannerFirmwareUpdateEvent() {
        val original = createScannerFirmwareUpdateEvent()
        val transformed = original.fromDomainToDb()

        verifyScannerFirmwareUpdateEvents(original, transformed.fromDbToDomain() as ScannerFirmwareUpdateEvent)
    }

    @Test
    fun convert_SuspiciousIntentEvent() {
        val original = createSuspiciousIntentEvent()
        val transformed = original.fromDomainToDb()

        verifySuspiciousIntentEvents(original, transformed.fromDbToDomain() as SuspiciousIntentEvent)
    }

    @Test
    fun convert_Vero2InfoSnapshotEvent() {
        val original = createVero2InfoSnapshotEvent()
        val transformed = original.fromDomainToDb()

        verifyVero2InfoSnapshotEvents(original, transformed.fromDbToDomain() as Vero2InfoSnapshotEvent)
    }
}
