package com.simprints.id.data.db.event.remote

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.commontesttools.sessionEvents.*
import com.simprints.id.data.db.event.local.models.*
import com.simprints.id.data.db.event.remote.models.fromApiToDomain
import com.simprints.id.data.db.event.remote.models.fromDomainToApi
import com.simprints.id.testtools.TestApplication
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class ApiEventTest {

    private val gsonWithAdapters = JsonHelper.gson

    @Test
    fun validate_alertScreenEventApiModel() {
        val event = createAlertScreenEvent()
        val apiEvent = event.fromDomainToApi()
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateAlertScreenEventApiModel(json)
        assertThat(event).isEqualTo(apiEvent.fromApiToDomain())
    }

    @Test
    fun validate_artificialTerminationEventApiModel() {
        val event = createArtificialTerminationEvent()
        val apiEvent = event.fromDomainToApi()
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateArtificialTerminationEventApiModel(json)
        assertThat(event).isEqualTo(apiEvent.fromApiToDomain())
    }

    @Test
    fun validate_IntentParsingEventApiModel() {
        val event = createIntentParsingEvent()
        val apiEvent = event.fromDomainToApi()
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateIntentParsingEventApiModel(json)
        assertThat(event).isEqualTo(apiEvent.fromApiToDomain())
    }

    @Test
    fun validate_authenticationEventApiModel() {
        val event = createAuthenticationEvent()
        val apiEvent = event.fromDomainToApi()
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateAuthenticationEventApiModel(json)
        assertThat(event).isEqualTo(apiEvent.fromApiToDomain())
    }

    @Test
    fun validate_authorizationEventApiModel() {
        val event = createAuthorizationEvent()
        val apiEvent = event.fromDomainToApi()
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateAuthorizationEventApiModel(json)
        assertThat(event).isEqualTo(apiEvent.fromApiToDomain())
    }

    @Test
    fun validate_calloutEventForVerificationApiModel() {
        val event = createVerificationCalloutEvent()
        val apiEvent = event.fromDomainToApi()
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateCalloutEventApiModel(json)
        assertThat(event).isEqualTo(apiEvent.fromApiToDomain())
    }

    @Test
    fun validate_calloutEventForIdentificationApiModel() {
        val event = createIdentificationCalloutEvent()
        val apiEvent = event.fromDomainToApi()
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateCalloutEventApiModel(json)
        assertThat(event).isEqualTo(apiEvent.fromApiToDomain())
    }

    @Test
    fun validate_calloutEventForEnrolLastBiometricsModel() {
        val event = createLastBiometricsEnrolmentCalloutEvent()
        val apiEvent = event.fromDomainToApi()
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateCalloutEventApiModel(json)
        assertThat(event).isEqualTo(apiEvent.fromApiToDomain())
    }

    @Test
    fun validate_calloutEventForConfirmationApiModel() {
        val event = createConfirmationCalloutEvent()
        val apiEvent = event.fromDomainToApi()
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateCalloutEventApiModel(json)
        assertThat(event).isEqualTo(apiEvent.fromApiToDomain())
    }

    @Test
    fun validate_calloutEventForEnrolmentApiModel() {
        val event = createEnrolmentCalloutEvent()
        val apiEvent = event.fromDomainToApi()
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateCalloutEventApiModel(json)
        assertThat(event).isEqualTo(apiEvent.fromApiToDomain())
    }

    @Test
    fun validate_callbackEventForEnrolmentApiModel() {
        val event = createEnrolmentCallbackEvent()
        val apiEvent = event.fromDomainToApi()
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateCallbackEventApiModel(json)
        assertThat(event).isEqualTo(apiEvent.fromApiToDomain())
    }

    @Test
    fun validate_callbackEventForErrorApiModel() {
        val event = createEnrolmentCallbackEvent()
        val apiEvent = event.fromDomainToApi()
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateCallbackEventApiModel(json)
        assertThat(event).isEqualTo(apiEvent.fromApiToDomain())
    }

    @Test
    fun validate_callbackEventForIdentificationApiModel() {
        val event = createIdentificationCallbackEvent()
        val apiEvent = event.fromDomainToApi()
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateCallbackEventApiModel(json)
        assertThat(event).isEqualTo(apiEvent.fromApiToDomain())
    }

    @Test
    fun validate_callbackEventForVerificationApiModel() {
        val event = createVerificationCallbackEvent()
        val apiEvent = event.fromDomainToApi()
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateCallbackEventApiModel(json)
        assertThat(event).isEqualTo(apiEvent.fromApiToDomain())
    }

    @Test
    fun validate_callbackEventForRefusalApiModel() {
        val event = createRefusalCallbackEvent()
        val apiEvent = event.fromDomainToApi()
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateCallbackEventApiModel(json)
        assertThat(event).isEqualTo(apiEvent.fromApiToDomain())
    }

    @Test
    fun validate_callbackEventForConfirmationApiModel() {
        val event = createConfirmationCallbackEvent()
        val apiEvent = event.fromDomainToApi()
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateCallbackEventApiModel(json)
        assertThat(event).isEqualTo(apiEvent.fromApiToDomain())
    }

    @Test
    fun validate_candidateReadEventApiModel() {
        val event = createCandidateReadEvent()
        val apiEvent = event.fromDomainToApi()
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateCandidateReadEventApiModel(json)
        assertThat(event).isEqualTo(apiEvent.fromApiToDomain())
    }

    @Test
    fun validate_connectivitySnapshotEventApiModel() {
        val event = createConnectivitySnapshotEvent()
        val apiEvent = event.fromDomainToApi()
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateConnectivitySnapshotEventApiModel(json)
        assertThat(event).isEqualTo(apiEvent.fromApiToDomain())
    }

    @Test
    fun validate_consentEventApiModel() {
        val event = createConsentEvent()
        val apiEvent = event.fromDomainToApi()
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateConsentEventApiModel(json)
        assertThat(event).isEqualTo(apiEvent.fromApiToDomain())
    }

    @Test
    fun validate_enrolmentEventApiModel() {
        val event = createEnrolmentEvent()
        val apiEvent = event.fromDomainToApi()
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateEnrolmentEventApiModel(json)
        assertThat(event).isEqualTo(apiEvent.fromApiToDomain())
    }

    @Test
    fun validate_completionCheckEventApiModel() {
        val event = createCompletionCheckEvent()
        val apiEvent = event.fromDomainToApi()
        val json = gsonWithAdapters.toJsonTree(event).asJsonObject

        validateCompletionCheckEventApiModel(json)
        assertThat(event).isEqualTo(apiEvent.fromApiToDomain())
    }

    @Test
    fun validate_fingerprintCaptureEventApiModel() {
        val event = createFingerprintCaptureEvent()
        val apiEvent = event.fromDomainToApi()
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateFingerprintCaptureEventApiModel(json)
        assertThat(event).isEqualTo(apiEvent.fromApiToDomain())
    }

    @Test
    fun validate_guidSelectionEventApiModel() {
        val event = createGuidSelectionEvent()
        val apiEvent = event.fromDomainToApi()
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateGuidSelectionEventApiModel(json)
        assertThat(event).isEqualTo(apiEvent.fromApiToDomain())
    }

    @Test
    fun validate_oneToManyMatchEventApiModel() {
        val event = createOneToManyMatchEvent()
        val apiEvent = event.fromDomainToApi()
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateOneToManyMatchEventApiModel(json)
        assertThat(event).isEqualTo(apiEvent.fromApiToDomain())
    }

    @Test
    fun validate_oneToOneMatchEventApiModel() {
        val event = createOneToOneMatchEvent()
        val apiEvent = event.fromDomainToApi()
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateOneToOneMatchEventApiModel(json)
        assertThat(event).isEqualTo(apiEvent.fromApiToDomain())
    }

    @Test
    fun validate_personCreationEventApiModel() {
        val event = createPersonCreationEvent()
        val apiEvent = event.fromDomainToApi()
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validatePersonCreationEvent(json)
        assertThat(event).isEqualTo(apiEvent.fromApiToDomain())
    }

    @Test
    fun validate_refusalEventApiModel() {
        val event = createRefusalEvent()
        val apiEvent = event.fromDomainToApi()
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateRefusalEventApiModel(json)
        assertThat(event).isEqualTo(apiEvent.fromApiToDomain())
    }

    @Test
    fun validate_sessionCaptureEvent() {
        val event = createSessionCaptureEvent()
        val apiEvent = event.fromDomainToApi()
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        valicateSessionCaptureApiModel(json)
        assertThat(event).isEqualTo(apiEvent.fromApiToDomain())
    }

    @Test
    fun validate_suspiciousIntentEventApiModel() {
        val event = createSuspiciousIntentEvent()
        val apiEvent = event.fromDomainToApi()
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateSuspiciousIntentEventApiModel(json)
        assertThat(event).isEqualTo(apiEvent.fromApiToDomain())
    }

    @Test
    fun validate_scannerConnectionEventApiModel() {
        val event = createScannerConnectionEvent()
        val apiEvent = event.fromDomainToApi()
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateScannerConnectionEventApiModel(json)
        assertThat(event).isEqualTo(apiEvent.fromApiToDomain())
    }

    @Test
    fun validate_vero2InfoSnapshotEvent() {
        val event = createVero2InfoSnapshotEvent()
        val apiEvent = event.fromDomainToApi()
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateVero2InfoSnapshotEventApiModel(json)
        assertThat(event).isEqualTo(apiEvent.fromApiToDomain())
    }

    @Test
    fun validate_ScannerFirmwareUpdateEvent() {
        val event = createScannerFirmwareUpdateEvent()
        val apiEvent = event.fromDomainToApi()
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateScannerFirmwareUpdateEventApiModel(json)
        assertThat(event).isEqualTo(apiEvent.fromApiToDomain())
    }

    @Test
    fun validate_invalidEventApiModel() {
        val event = createInvalidIntentEvent()
        val apiEvent = event.fromDomainToApi()
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject
        validateInvalidEventApiModel(json)
        assertThat(event).isEqualTo(apiEvent.fromApiToDomain())
    }
}
