package com.simprints.id.data.analytics.eventdata.controllers.remote.apiAdapters

import android.net.NetworkInfo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.FingerIdentifier
import com.simprints.id.commontesttools.sessionEvents.*
import com.simprints.id.data.analytics.eventdata.models.domain.events.*
import com.simprints.id.data.analytics.eventdata.models.domain.events.OneToManyMatchEvent.MatchPool
import com.simprints.id.data.analytics.eventdata.models.domain.events.OneToManyMatchEvent.MatchPoolType
import com.simprints.id.data.analytics.eventdata.models.domain.events.callout.ConfirmationCallout
import com.simprints.id.data.analytics.eventdata.models.domain.events.callout.EnrolmentCallout
import com.simprints.id.data.analytics.eventdata.models.domain.events.callout.IdentificationCallout
import com.simprints.id.data.analytics.eventdata.models.domain.events.callout.VerificationCallout
import com.simprints.id.data.analytics.eventdata.models.domain.session.DatabaseInfo
import com.simprints.id.data.analytics.eventdata.models.domain.session.Device
import com.simprints.id.data.analytics.eventdata.models.domain.session.Location
import com.simprints.id.data.analytics.eventdata.models.domain.session.SessionEvents
import com.simprints.id.data.analytics.eventdata.models.remote.events.*
import com.simprints.id.data.analytics.eventdata.models.remote.session.ApiDatabaseInfo
import com.simprints.id.data.analytics.eventdata.models.remote.session.ApiDevice
import com.simprints.id.data.analytics.eventdata.models.remote.session.ApiLocation
import com.simprints.id.data.analytics.eventdata.models.remote.session.ApiSessionEvents
import com.simprints.id.domain.alert.Alert
import com.simprints.id.domain.moduleapi.app.requests.AppIntegrationInfo
import com.simprints.id.domain.moduleapi.app.responses.AppVerifyResponse
import com.simprints.id.domain.moduleapi.app.responses.entities.MatchResult
import com.simprints.id.domain.moduleapi.app.responses.entities.Tier
import com.simprints.id.testtools.TestApplication
import com.simprints.id.tools.utils.SimNetworkUtils
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.*

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class SessionEventsAdapterFactoryTest {

    private val gsonWithAdapters = JsonHelper.gson

    @Test
    fun validate_alertScreenEventApiModel() {
        val event = AlertScreenEvent(0, Alert.NOT_PAIRED)
        val apiEvent = ApiAlertScreenEvent(event)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject
        validateAlertScreenEventApiModel(json)
    }

    @Test
    fun validate_artificialTerminationEventApiModel() {
        val event = ArtificialTerminationEvent(0, ArtificialTerminationEvent.Reason.NEW_SESSION)
        val apiEvent = ApiArtificialTerminationEvent(event)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateArtificialTerminationEventApiModel(json)
    }

    @Test
    fun validate_authenticationEventApiModel() {
        val event = AuthenticationEvent(
            0,
            0,
            AuthenticationEvent.UserInfo("projectId", "userId"),
            AuthenticationEvent.Result.AUTHENTICATED)
        val apiEvent = ApiAuthenticationEvent(event)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateAuthenticationEventApiModel(json)
    }

    @Test
    fun validate_authorizationEventApiModel() {
        val event = AuthorizationEvent(
            10,
            AuthorizationEvent.Result.AUTHORIZED,
            AuthorizationEvent.UserInfo("projectId", "userId"))
        val apiEvent = ApiAuthorizationEvent(event)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateAuthorizationEventApiModel(json)
    }

    @Test
    fun validate_calloutEventForVerificationApiModel() {
        val calloutVerification = VerificationCallout("projectId", "userId", "moduleId", "verifyGuid", "metadata")
        val calloutEvent = CalloutEvent(AppIntegrationInfo.ODK, 10, calloutVerification)
        val apiEvent = ApiCalloutEvent(calloutEvent)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateCalloutEventApiModel(json)
    }

    @Test
    fun validate_calloutEventForIdentificationApiModel() {
        val calloutVerification = IdentificationCallout("projectId", "userId", "moduleId", "metadata")
        val calloutEvent = CalloutEvent(AppIntegrationInfo.ODK, 10, calloutVerification)
        val apiEvent = ApiCalloutEvent(calloutEvent)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateCalloutEventApiModel(json)
    }

    @Test
    fun validate_calloutEventForConfirmationApiModel() {
        val calloutVerification = ConfirmationCallout("selectedGuid", "sessionId")
        val calloutEvent = CalloutEvent(AppIntegrationInfo.ODK, 10, calloutVerification)
        val apiEvent = ApiCalloutEvent(calloutEvent)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateCalloutEventApiModel(json)
    }

    @Test
    fun validate_calloutEventForEnrolmentApiModel() {
        val calloutVerification = EnrolmentCallout("projectId", "userId", "moduleId", "metadata")
        val calloutEvent = CalloutEvent(AppIntegrationInfo.STANDARD, 10, calloutVerification)
        val apiEvent = ApiCalloutEvent(calloutEvent)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateCalloutEventApiModel(json)
    }

    @Test
    fun validate_callbackEventForEnrolmentApiModel() {
        val callbackEnrolment = EnrolmentCallback("guid")
        val callbackEvent = CallbackEvent(10, callbackEnrolment)
        val apiEvent = ApiCallbackEvent(callbackEvent)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateCallbackEventApiModel(json)
    }

    @Test
    fun validate_callbackEventForIdentificationApiModel() {
        val callbackIdentification = IdentificationCallback("sessionId", getListOfCallbackComparisonScores())
        val callbackEvent = CallbackEvent(10, callbackIdentification)
        val apiEvent = ApiCallbackEvent(callbackEvent)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateCallbackEventApiModel(json)
    }

    @Test
    fun validate_callbackEventForVerificationApiModel() {
        val callbackVerification = VerificationCallback(CallbackComparisonScore("guid", 42, Tier.TIER_1))
        val callbackEvent = CallbackEvent(10, callbackVerification)
        val apiEvent = ApiCallbackEvent(callbackEvent)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateCallbackEventApiModel(json)
    }

    @Test
    fun validate_callbackEventForRefusalApiModel() {
        val callbackRefusal = RefusalCallback("reason", "extra")
        val callbackEvent = CallbackEvent(10, callbackRefusal)
        val apiEvent = ApiCallbackEvent(callbackEvent)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateCallbackEventApiModel(json)
    }

    private fun getListOfCallbackComparisonScores() = listOf(
        CallbackComparisonScore("guid1", 42, Tier.TIER_1),
        CallbackComparisonScore("guid2", 43, Tier.TIER_2),
        CallbackComparisonScore("guid3", 44, Tier.TIER_3)
    )

    @Test
    fun validate_candidateReadEventApiModel() {
        val event = CandidateReadEvent(
            10,
            10,
            UUID.randomUUID().toString(),
            CandidateReadEvent.LocalResult.FOUND,
            CandidateReadEvent.RemoteResult.NOT_FOUND)

        val apiEvent = ApiCandidateReadEvent(event)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject
        validateCandidateReadEventApiModel(json)
    }

    @Test
    fun validate_connectivitySnapshotEventApiModel() {
        val event = ConnectivitySnapshotEvent(
            10,
            "GSM",
            listOf(SimNetworkUtils.Connection("WIFI", NetworkInfo.DetailedState.CONNECTED)))

        val apiEvent = ApiConnectivitySnapshotEvent(event)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject
        validateConnectivitySnapshotEventApiModel(json)
    }

    @Test
    fun validate_consentEventApiModel() {
        val event = ConsentEvent(
            10,
            10,
            ConsentEvent.Type.INDIVIDUAL,
            ConsentEvent.Result.ACCEPTED)

        val apiEvent = ApiConsentEvent(event)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject
        validateConsentEventApiModel(json)
    }

    @Test
    fun validate_enrollmentEventApiModel() {
        val event = EnrolmentEvent(
            10,
            UUID.randomUUID().toString())

        val apiEvent = ApiEnrollmentEvent(event)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject
        validateEnrolmentEventApiModel(json)
    }

    @Test
    fun validate_fingerprintCaptureEventApiModel() {
        val event = FingerprintCaptureEvent(
            10,
            10,
            FingerIdentifier.LEFT_3RD_FINGER,
            10,
            FingerprintCaptureEvent.Result.BAD_QUALITY,
            FingerprintCaptureEvent.Fingerprint(10, "some_template".toByteArray().toString()))
        val apiEvent = ApiFingerprintCaptureEvent(event)

        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject
        validateFingerprintCaptureEventApiModel(json)
    }

    @Test
    fun validate_guidSelectionEventApiModel() {
        val event = GuidSelectionEvent(
            10,
            UUID.randomUUID().toString())
        val apiEvent = ApiGuidSelectionEvent(event)

        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject
        validateGuidSelectionEventApiModel(json)
    }

    @Test
    fun validate_oneToManyMatchEventApiModel() {
        val event = OneToManyMatchEvent(
            10,
            10,
            MatchPool(MatchPoolType.MODULE, 10),
            listOf(MatchEntry(UUID.randomUUID().toString(), 10F)))
        val apiEvent = ApiOneToManyMatchEvent(event)

        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject
        validateOneToManyMatchEventApiModel(json)
    }

    @Test
    fun validate_oneToOneMatchEventApiModel() {
        val event = OneToOneMatchEvent(
            10,
            10,
            UUID.randomUUID().toString(),
            MatchEntry(UUID.randomUUID().toString(), 10F))
        val apiEvent = ApiOneToOneMatchEvent(event)

        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject
        validateOneToOneMatchEventApiModel(json)
    }

    @Test
    fun validate_personCreationEventApiModel() {
        val event = PersonCreationEvent(
            10,
            listOf(UUID.randomUUID().toString()))
        val apiEvent = ApiPersonCreationEvent(event)

        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject
        validatePersonCreationEvent(json)
    }

    @Test
    fun validate_refusalEventApiModel() {
        val event = RefusalEvent(
            10,
            10,
            RefusalEvent.Answer.OTHER,
            "")
        val apiEvent = ApiRefusalEvent(event)

        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject
        validateRefusalEventApiModel(json)
    }

    @Test
    fun validate_databaseInfoApiModel() {
        val databaseInfo = DatabaseInfo(0, 0)
        val apiDatabaseInfo = ApiDatabaseInfo(databaseInfo)

        val json = gsonWithAdapters.toJsonTree(apiDatabaseInfo).asJsonObject
        validateDatabaseInfoApiModel(json)
    }

    @Test
    fun validate_deviceApiModel() {
        val device = Device("28", "phone", "device_id")
        val apiDevice = ApiDevice(device)
        val json = gsonWithAdapters.toJsonTree(apiDevice).asJsonObject
        validateDeviceApiModel(json)
    }

    @Test
    fun validate_locationApiModel() {
        val location = Location(1.2, 2.4)
        val apiLocation = ApiLocation(location)
        val json = gsonWithAdapters.toJsonTree(apiLocation).asJsonObject
        validateLocationApiModel(json)
    }

    @Test
    fun validate_suspiciousIntentEventApiModel() {
        val suspiciousIntentEvent = SuspiciousIntentEvent(mapOf("extraFieldKey" to "someUnexpectedField"))
        val apiSuspiciousIntentEvent = ApiSuspiciousIntentEvent(suspiciousIntentEvent)
        val json = gsonWithAdapters.toJsonTree(apiSuspiciousIntentEvent).asJsonObject
        validateSuspiciousIntentEventApiModel(json)
    }

    @Test
    fun validate_invalidEventApiModel() {
        val invalidIntentEvent = InvalidIntentEvent("action_should_be_enum", mapOf("projectId" to "someProject"))
        val apiInvalidIntentEvent = ApiInvalidIntentEvent(invalidIntentEvent)
        val json = gsonWithAdapters.toJsonTree(apiInvalidIntentEvent).asJsonObject
        validateInvalidEventApiModel(json)
    }

    @Test
    fun validate_sessionApiModel() {
        val session = SessionEvents(
            "project_id",
            "appVersionName",
            "libVersionName",
            "en",
            Device("28", "phone", "device_id"),
            0)
        session.addEvent(AlertScreenEvent(0, Alert.NOT_PAIRED))
        val apiSession = ApiSessionEvents(session)
        val json = gsonWithAdapters.toJsonTree(apiSession).asJsonObject
        validateSessionEventsApiModel(json)
    }
}
