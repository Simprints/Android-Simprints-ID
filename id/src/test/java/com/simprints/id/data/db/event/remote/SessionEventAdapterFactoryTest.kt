package com.simprints.id.data.db.event.remote

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.commontesttools.sessionEvents.*
import com.simprints.id.data.db.event.local.models.*
import com.simprints.id.data.db.event.remote.events.*
import com.simprints.id.data.db.event.remote.events.callback.ApiCallbackEvent
import com.simprints.id.data.db.event.remote.events.callout.ApiCalloutEvent
import com.simprints.id.data.db.event.remote.events.session.ApiSessionCapture
import com.simprints.id.testtools.TestApplication
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class SessionEventAdapterFactoryTest {

    private val gsonWithAdapters = JsonHelper.gson

    @Test
    fun validate_alertScreenEventApiModel() {
        val event = createAlertScreenEvent()
        val apiEvent = ApiAlertScreen(event)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject
        validateAlertScreenEventApiModel(json)
    }

    @Test
    fun validate_artificialTerminationEventApiModel() {
        val event = createArtificialTerminationEvent()
        val apiEvent = ApiArtificialTerminationEvent(event)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateArtificialTerminationEventApiModel(json)
    }

    @Test
    fun validate_IntentParsingEventApiModel() {
        val event = createIntentParsingEvent()
        val apiEvent = ApiIntentParsingEvent(event)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateIntentParsingEventApiModel(json)
    }

    @Test
    fun validate_authenticationEventApiModel() {
        val event = createAuthenticationEvent()
        val apiEvent = ApiAuthenticationEvent(event)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateAuthenticationEventApiModel(json)
    }

    @Test
    fun validate_authorizationEventApiModel() {
        val event = createAuthorizationEvent()

        val apiEvent = ApiAuthorizationEvent(event)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateAuthorizationEventApiModel(json)
    }

    @Test
    fun validate_calloutEventForVerificationApiModel() {
        val calloutEvent = createVerificationCalloutEvent()
        val apiEvent = calloutEvent.fromDomainToApi()
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateCalloutEventApiModel(json)
    }

    @Test
    fun validate_calloutEventForIdentificationApiModel() {
        val calloutEvent = createIdentificationCalloutEvent()
        val apiEvent = calloutEvent.fromDomainToApi()
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateCalloutEventApiModel(json)
    }

    @Test
    fun validate_calloutEventForEnrolLastBiometricsModel() {
        val calloutEvent = createLastBiometricsEnrolmentCalloutEvent()
        val apiEvent = calloutEvent.fromDomainToApi()
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateCalloutEventApiModel(json)
    }

    @Test
    fun validate_calloutEventForConfirmationApiModel() {
        val calloutEvent = createConfirmationCalloutEvent()
        val apiEvent = calloutEvent.fromDomainToApi()
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateCalloutEventApiModel(json)
    }

    @Test
    fun validate_calloutEventForEnrolmentApiModel() {
        val calloutEvent = createEnrolmentCalloutEvent()
        val apiEvent = calloutEvent.fromDomainToApi()
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateCalloutEventApiModel(json)
    }

    @Test
    fun validate_callbackEventForEnrolmentApiModel() {
        val callbackEvent = createEnrolmentCallbackEvent()
        val apiEvent = callbackEvent.fromDomainToApi()
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateCallbackEventApiModel(json)
    }

    @Test
    fun validate_callbackEventForErrorApiModel() {
        val callbackEvent = createEnrolmentCallbackEvent()
        val apiEvent = callbackEvent.fromDomainToApi()
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateCallbackEventApiModel(json)
    }

    @Test
    fun validate_callbackEventForIdentificationApiModel() {
        val callbackEvent = createIdentificationCallbackEvent()
        val apiEvent = callbackEvent.fromDomainToApi()
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateCallbackEventApiModel(json)
    }

    @Test
    fun validate_callbackEventForVerificationApiModel() {
        val callbackEvent = createVerificationCallbackEvent()
        val apiEvent = callbackEvent.fromDomainToApi()
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateCallbackEventApiModel(json)
    }

    @Test
    fun validate_callbackEventForRefusalApiModel() {
        val callbackEvent = createRefusalCallbackEvent()
        val apiEvent = callbackEvent.fromDomainToApi()
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateCallbackEventApiModel(json)
    }

    @Test
    fun validate_callbackEventForConfirmationApiModel() {
        val callbackEvent = createConfirmationCallbackEvent()
        val apiEvent = callbackEvent.fromDomainToApi()
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateCallbackEventApiModel(json)
    }

    @Test
    fun validate_candidateReadEventApiModel() {
        val event = createCandidateReadEvent()
        val apiEvent = ApiCandidateReadEvent(event)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateCandidateReadEventApiModel(json)
    }

    @Test
    fun validate_connectivitySnapshotEventApiModel() {
        val event = createConnectivitySnapshotEvent()
        val apiEvent = ApiConnectivitySnapshotEvent(event)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateConnectivitySnapshotEventApiModel(json)
    }

    @Test
    fun validate_consentEventApiModel() {
        val event = createConsentEvent()
        val apiEvent = ApiConsentEvent(event)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateConsentEventApiModel(json)
    }

    @Test
    fun validate_enrolmentEventApiModel() {
        val event = createEnrolmentEvent()

        val apiEvent = ApiEnrolmentEvent(event)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject
        validateEnrolmentEventApiModel(json)
    }

    @Test
    fun validate_completionCheckEventApiModel() {
        val event = createCompletionCheckEvent()
        val apiEvent = ApiCompletionCheckEvent(event)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateCompletionCheckEventApiModel(json)
    }

    @Test
    fun validate_fingerprintCaptureEventApiModel() {
        val event = createFingerprintCaptureEvent()
        val apiEvent = ApiFingerprintCaptureEvent(event)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateFingerprintCaptureEventApiModel(json)
    }

    @Test
    fun validate_guidSelectionEventApiModel() {
        val event = createGuidSelectionEvent()
        val apiEvent = ApiGuidSelectionEvent(event)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateGuidSelectionEventApiModel(json)
    }

    @Test
    fun validate_oneToManyMatchEventApiModel() {
        val event = createOneToManyMatchEvent()
        val apiEvent = ApiOneToManyMatchEvent(event)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateOneToManyMatchEventApiModel(json)
    }

    @Test
    fun validate_oneToOneMatchEventApiModel() {
        val event = createOneToOneMatchEvent()
        val apiEvent = ApiOneToOneMatchEvent(event)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateOneToOneMatchEventApiModel(json)
    }

    @Test
    fun validate_personCreationEventApiModel() {
        val event = createPersonCreationEvent()
        val apiEvent = ApiPersonCreationEvent(event)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validatePersonCreationEvent(json)
    }

    @Test
    fun validate_refusalEventApiModel() {
        val event = createRefusalEvent()
        val apiEvent = ApiRefusalEvent(event)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateRefusalEventApiModel(json)
    }

    @Test
    fun validate_sessionCaptureEvent() {
        val event = createSessionCaptureEvent()
        val apiDatabaseInfo = ApiSessionCapture(event)
        val json = gsonWithAdapters.toJsonTree(apiDatabaseInfo).asJsonObject

        valicateSessionCaptureApiModel(json)
    }

    @Test
    fun validate_suspiciousIntentEventApiModel() {
        val suspiciousIntentEvent = createSuspiciousIntentEvent()
        val apiSuspiciousIntentEvent = ApiSuspiciousIntentEvent(suspiciousIntentEvent)
        val json = gsonWithAdapters.toJsonTree(apiSuspiciousIntentEvent).asJsonObject

        validateSuspiciousIntentEventApiModel(json)
    }

    @Test
    fun validate_scannerConnectionEventApiModel() {
        val scannerConnectionEvent = createScannerConnectionEvent()
        val apiScannerConnectionEvent = ApiScannerConnectionEvent(scannerConnectionEvent)
        val json = gsonWithAdapters.toJsonTree(apiScannerConnectionEvent).asJsonObject

        validateScannerConnectionEventApiModel(json)
    }

    @Test
    fun validate_vero2InfoSnapshotEvent() {
        val vero2InfoSnapshotEvent = createVero2InfoSnapshotEvent()
        val apiVero2InfoSnapshotEvent = ApiVero2InfoSnapshotEvent(vero2InfoSnapshotEvent)
        val json = gsonWithAdapters.toJsonTree(apiVero2InfoSnapshotEvent).asJsonObject

        validateVero2InfoSnapshotEventApiModel(json)
    }

    @Test
    fun validate_ScannerFirmwareUpdateEvent() {
        val scannerFirmwareUpdateEvent = createScannerFirmwareUpdateEvent()
        val apiScannerFirmwareUpdateEvent = ApiScannerFirmwareUpdateEvent(scannerFirmwareUpdateEvent)
        val json = gsonWithAdapters.toJsonTree(apiScannerFirmwareUpdateEvent).asJsonObject

        validateScannerFirmwareUpdateEventApiModel(json)
    }

    @Test
    fun validate_invalidEventApiModel() {
        val invalidIntentEvent = createInvalidIntentEvent()
        val apiInvalidIntentEvent = ApiInvalidIntentEvent(invalidIntentEvent)
        val json = gsonWithAdapters.toJsonTree(apiInvalidIntentEvent).asJsonObject
        validateInvalidEventApiModel(json)
    }
}
