package com.simprints.id.data.db.event.local.models

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DbEventTest {

    @Test
    fun convert_ConfirmationCallbackEvent() {
        val original = createConfirmationCallbackEvent()
        val transformed = original.fromDomainToDb().fromDbToDomain()
        assertThat(original).isEqualTo(transformed)
    }

    @Test
    fun convert_EnrolmentCallbackEvent() {
        val original = createEnrolmentCallbackEvent()
        val transformed = original.fromDomainToDb().fromDbToDomain()

        assertThat(original).isEqualTo(transformed)
    }

    @Test
    fun convert_ErrorCallbackEvent() {
        val original = createErrorCallbackEvent()
        val transformed = original.fromDomainToDb().fromDbToDomain()

        assertThat(original).isEqualTo(transformed)
    }

    @Test
    fun convert_IdentificationCallbackEvent() {
        val original = createIdentificationCallbackEvent()
        val transformed = original.fromDomainToDb().fromDbToDomain()

        assertThat(original).isEqualTo(transformed)
    }

    @Test
    fun convert_RefusalCallbackEvent() {
        val original = createRefusalCallbackEvent()
        val transformed = original.fromDomainToDb().fromDbToDomain()

        assertThat(original).isEqualTo(transformed)
    }

    @Test
    fun convert_VerificationCallbackEvent() {
        val original = createVerificationCallbackEvent()
        val transformed = original.fromDomainToDb().fromDbToDomain()

        assertThat(original).isEqualTo(transformed)
    }

    @Test
    fun convert_ConfirmationCalloutEvent() {
        val original = createConfirmationCalloutEvent()
        val transformed = original.fromDomainToDb().fromDbToDomain()

        assertThat(original).isEqualTo(transformed)
    }

    @Test
    fun convert_EnrolmentCalloutEvent() {
        val original = createEnrolmentCalloutEvent()
        val transformed = original.fromDomainToDb().fromDbToDomain()

        assertThat(original).isEqualTo(transformed)
    }

    @Test
    fun convert_EnrolmentLastBiometricsCalloutEvent() {
        val original = createLastBiometricsEnrolmentCalloutEvent()
        val transformed = original.fromDomainToDb().fromDbToDomain()

        assertThat(original).isEqualTo(transformed)
    }

    @Test
    fun convert_IdentificationCalloutEvent() {
        val original = createIdentificationCalloutEvent()
        val transformed = original.fromDomainToDb().fromDbToDomain()

        assertThat(original).isEqualTo(transformed)
    }

    @Test
    fun convert_VerificationCalloutEvent() {
        val original = createVerificationCalloutEvent()
        val transformed = original.fromDomainToDb().fromDbToDomain()

        assertThat(original).isEqualTo(transformed)
    }

    @Test
    fun convert_FaceCaptureConfirmationEvent() {
        val original = createFaceCaptureConfirmationEvent()
        val transformed = original.fromDomainToDb().fromDbToDomain()

        assertThat(original).isEqualTo(transformed)
    }

    @Test
    fun convert_FaceCaptureEvent() {
        val original = createFaceCaptureEvent()
        val transformed = original.fromDomainToDb().fromDbToDomain()

        assertThat(original).isEqualTo(transformed)
    }

    @Test
    fun convert_FaceCaptureRetryEvent() {
        val original = createFaceCaptureRetryEvent()
        val transformed = original.fromDomainToDb().fromDbToDomain()

        assertThat(original).isEqualTo(transformed)
    }

    @Test
    fun convert_FaceFallbackCaptureEvent() {
        val original = createFaceFallbackCaptureEvent()
        val transformed = original.fromDomainToDb().fromDbToDomain()

        assertThat(original).isEqualTo(transformed)
    }

    @Test
    fun convert_FaceOnboardingCompleteEvent() {
        val original = createFaceOnboardingCompleteEvent()
        val transformed = original.fromDomainToDb().fromDbToDomain()

        assertThat(original).isEqualTo(transformed)
    }

    @Test
    fun convert_SessionCaptureEvent() {
        val original = createSessionCaptureEvent()
        val transformed = (original.fromDomainToDb().fromDbToDomain())

        assertThat(original).isEqualTo(transformed)
    }

    @Test
    fun convert_AlertScreenEvent() {
        val original = createAlertScreenEvent()
        val transformed = original.fromDomainToDb().fromDbToDomain()

        assertThat(original).isEqualTo(transformed)
    }

    @Test
    fun convert_ArtificialTerminationEvent() {
        val original = createArtificialTerminationEvent()
        val transformed = original.fromDomainToDb().fromDbToDomain()

        assertThat(original).isEqualTo(transformed)
    }

    @Test
    fun convert_AuthenticationEvent() {
        val original = createAuthenticationEvent()
        val transformed = original.fromDomainToDb().fromDbToDomain()

        assertThat(original).isEqualTo(transformed)
    }

    @Test
    fun convert_AuthorizationEvent() {
        val original = createAuthorizationEvent()
        val transformed = original.fromDomainToDb().fromDbToDomain()

        assertThat(original).isEqualTo(transformed)
    }

    @Test
    fun convert_CandidateReadEvent() {
        val original = createCandidateReadEvent()
        val transformed = original.fromDomainToDb().fromDbToDomain()

        assertThat(original).isEqualTo(transformed)
    }

    @Test
    fun convert_CompletionCheckEvent() {
        val original = createCompletionCheckEvent()
        val transformed = original.fromDomainToDb().fromDbToDomain()

        assertThat(original).isEqualTo(transformed)
    }

    @Test
    fun convert_ConnectivitySnapshotEvent() {
        val original = createConnectivitySnapshotEvent()
        val transformed = original.fromDomainToDb().fromDbToDomain()

        assertThat(original).isEqualTo(transformed)
    }

    @Test
    fun convert_ConsentEvent() {
        val original = createConsentEvent()
        val transformed = original.fromDomainToDb().fromDbToDomain()

        assertThat(original).isEqualTo(transformed)
    }

    @Test
    fun convert_EnrolmentEvent() {
        val original = createEnrolmentEvent()
        val transformed = original.fromDomainToDb().fromDbToDomain()

        assertThat(original).isEqualTo(transformed)
    }

    @Test
    fun convert_FingerprintCaptureEvent() {
        val original = createFingerprintCaptureEvent()
        val transformed = original.fromDomainToDb().fromDbToDomain()

        assertThat(original).isEqualTo(transformed)
    }

    @Test
    fun convert_GuidSelectionEvent() {
        val original = createGuidSelectionEvent()
        val transformed = original.fromDomainToDb().fromDbToDomain()

        assertThat(original).isEqualTo(transformed)
    }

    @Test
    fun covert_IntentParsingEvent() {
        val original = createIntentParsingEvent()
        val transformed = original.fromDomainToDb().fromDbToDomain()

        assertThat(original).isEqualTo(transformed)
    }

    @Test
    fun convert_InvalidIntentEvent() {
        val original = createInvalidIntentEvent()
        val transformed = original.fromDomainToDb().fromDbToDomain()

        assertThat(original).isEqualTo(transformed)
    }

    @Test
    fun convert_OneToManyMatchEvent() {
        val original = createOneToManyMatchEvent()
        val transformed = original.fromDomainToDb().fromDbToDomain()

        assertThat(original).isEqualTo(transformed)
    }

    @Test
    fun convert_OneToOneMatchEvent() {
        val original = createOneToOneMatchEvent()
        val transformed = original.fromDomainToDb().fromDbToDomain()

        assertThat(original).isEqualTo(transformed)
    }

    @Test
    fun convert_PersonCreationEvent() {
        val original = createPersonCreationEvent()
        val transformed = original.fromDomainToDb().fromDbToDomain()

        assertThat(original).isEqualTo(transformed)
    }

    @Test
    fun convert_RefusalEvent() {
        val original = createRefusalEvent()
        val transformed = original.fromDomainToDb().fromDbToDomain()

        assertThat(original).isEqualTo(transformed)
    }

    @Test
    fun convert_ScannerConnectionEvent() {
        val original = createScannerConnectionEvent()
        val transformed = original.fromDomainToDb().fromDbToDomain()

        assertThat(original).isEqualTo(transformed)
    }

    @Test
    fun convert_ScannerFirmwareUpdateEvent() {
        val original = createScannerFirmwareUpdateEvent()
        val transformed = original.fromDomainToDb().fromDbToDomain()

        assertThat(original).isEqualTo(transformed)
    }

    @Test
    fun convert_SuspiciousIntentEvent() {
        val original = createSuspiciousIntentEvent()
        val transformed = original.fromDomainToDb().fromDbToDomain()

        assertThat(original).isEqualTo(transformed)
    }

    @Test
    fun convert_Vero2InfoSnapshotEvent() {
        val original = createVero2InfoSnapshotEvent()
        val transformed = original.fromDomainToDb().fromDbToDomain()

        assertThat(original).isEqualTo(transformed)
    }
}
