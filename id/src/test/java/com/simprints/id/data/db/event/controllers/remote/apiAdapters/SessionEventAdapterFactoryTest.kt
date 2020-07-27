package com.simprints.id.data.db.event.controllers.remote.apiAdapters

import android.net.NetworkInfo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.utils.randomUUID
import com.simprints.id.commontesttools.sessionEvents.*
import com.simprints.id.data.db.subject.domain.FingerIdentifier
import com.simprints.id.data.db.event.domain.events.*
import com.simprints.id.data.db.event.domain.events.OneToManyMatchEvent.MatchPool
import com.simprints.id.data.db.event.domain.events.OneToManyMatchEvent.MatchPoolType
import com.simprints.id.data.db.event.domain.events.callback.*
import com.simprints.id.data.db.event.domain.events.callout.*
import com.simprints.id.data.db.event.domain.events.session.DatabaseInfo
import com.simprints.id.data.db.event.domain.events.session.Device
import com.simprints.id.data.db.event.domain.events.session.Location
import com.simprints.id.data.db.event.domain.events.session.SessionCaptureEvent
import com.simprints.id.data.db.event.domain.models.AlertScreenEvent
import com.simprints.id.data.db.event.remote.events.*
import com.simprints.id.data.db.event.remote.events.callback.ApiCallbackEvent
import com.simprints.id.data.db.event.remote.events.callout.ApiCalloutEvent
import com.simprints.id.data.db.event.remote.session.ApiDatabaseInfo
import com.simprints.id.data.db.event.remote.session.ApiDevice
import com.simprints.id.data.db.event.remote.session.ApiLocation
import com.simprints.id.data.db.event.remote.session.ApiSessionCapture
import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse
import com.simprints.id.domain.moduleapi.app.responses.entities.Tier
import com.simprints.id.testtools.TestApplication
import com.simprints.id.tools.utils.SimNetworkUtils
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.*

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class SessionEventAdapterFactoryTest {

    private val gsonWithAdapters = JsonHelper.gson

    @Test
    fun validate_alertScreenEventApiModel() {
        val event = AlertScreenEvent(0, AlertScreenEvent.AlertScreenEventType.INVALID_SELECTED_ID)
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
    fun validate_IntentParsingEventApiModel() {
        val event = IntentParsingEvent(0, IntentParsingEvent.IntegrationInfo.STANDARD)
        val apiEvent = ApiIntentParsingEvent(event)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateIntentParsingEventApiModel(json)
    }

    @Test
    fun validate_authenticationEventApiModel() {
        val event = AuthenticationEvent(
            0,
            0,
            AuthenticationEvent.UserInfo("projectId", "userId"),
            AuthenticationEvent.Result.AUTHENTICATED
        )
        val apiEvent = ApiAuthenticationEvent(event)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateAuthenticationEventApiModel(json)
    }

    @Test
    fun validate_authorizationEventApiModel() {
        val event = AuthorizationEvent(
            10,
            AuthorizationEvent.Result.AUTHORIZED,
            AuthorizationEvent.UserInfo("projectId", "userId")
        )
        val apiEvent = ApiAuthorizationEvent(event)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateAuthorizationEventApiModel(json)
    }

    @Test
    fun validate_calloutEventForVerificationApiModel() {
        val calloutEvent = VerificationCalloutEvent(
            10,
            "projectId", "userId", "moduleId",
            "verifyGuid", "metadata"
        )

        val apiEvent = ApiCalloutEvent(calloutEvent)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateCalloutEventApiModel(json)
    }

    @Test
    fun validate_calloutEventForIdentificationApiModel() {
        val calloutEvent = IdentificationCalloutEvent(
            10,
            "projectId", "userId", "moduleId", "metadata"
        )

        val apiEvent = ApiCalloutEvent(calloutEvent)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateCalloutEventApiModel(json)
    }

    @Test
    fun validate_calloutEventForEnrolLastBiometricsModel() {
        val calloutEvent = EnrolmentLastBiometricsCalloutEvent(
            10,
            "projectId", "userId", "moduleId", "metadata", "sessionId"
        )

        val apiEvent = ApiCalloutEvent(calloutEvent)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateCalloutEventApiModel(json)
    }

    @Test
    fun validate_calloutEventForConfirmationApiModel() {
        val calloutEvent = ConfirmationCalloutEvent(
            10,
            "projectId", "selectedGuid", "sessionId"
        )
        val apiEvent = ApiCalloutEvent(calloutEvent)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateCalloutEventApiModel(json)
    }

    @Test
    fun validate_calloutEventForEnrolmentApiModel() {
        val calloutEvent = EnrolmentCalloutEvent(
            10,
            "projectId", "userId", "moduleId", "metadata"
        )

        val apiEvent = ApiCalloutEvent(calloutEvent)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateCalloutEventApiModel(json)
    }

    @Test
    fun validate_callbackEventForEnrolmentApiModel() {
        val callbackEvent = EnrolmentCallbackEvent(10, "guid")
        val apiEvent = ApiCallbackEvent(callbackEvent)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateCallbackEventApiModel(json)
    }

    @Test
    fun validate_callbackEventForErrorApiModel() {
        val callbackEvent = ErrorCallbackEvent(10, AppErrorResponse.Reason.BLUETOOTH_NOT_SUPPORTED)
        val apiEvent = ApiCallbackEvent(callbackEvent)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateCallbackEventApiModel(json)
    }

    @Test
    fun validate_callbackEventForIdentificationApiModel() {
        val callbackEvent =
            IdentificationCallbackEvent(10, "sessionId", getListOfCallbackComparisonScores())
        val apiEvent = ApiCallbackEvent(callbackEvent)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateCallbackEventApiModel(json)
    }

    @Test
    fun validate_callbackEventForVerificationApiModel() {
        val callbackEvent =
            VerificationCallbackEvent(10, CallbackComparisonScore("guid", 42, Tier.TIER_1))
        val apiEvent = ApiCallbackEvent(callbackEvent)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateCallbackEventApiModel(json)
    }

    @Test
    fun validate_callbackEventForRefusalApiModel() {
        val callbackEvent = RefusalCallbackEvent(10, "REFUSED_DATA_CONCERNS", "extra")
        val apiEvent = ApiCallbackEvent(callbackEvent)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject

        validateCallbackEventApiModel(json)
    }

    @Test
    fun validate_callbackEventForConfirmationApiModel() {
        val callbackEvent = ConfirmationCallbackEvent(10, true)
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
            CandidateReadEvent.RemoteResult.NOT_FOUND
        )

        val apiEvent = ApiCandidateReadEvent(event)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject
        validateCandidateReadEventApiModel(json)
    }

    @Test
    fun validate_connectivitySnapshotEventApiModel() {
        val event = ConnectivitySnapshotEvent(
            10,
            "GSM",
            listOf(SimNetworkUtils.Connection("WIFI", NetworkInfo.DetailedState.CONNECTED))
        )

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
            ConsentEvent.Result.ACCEPTED
        )

        val apiEvent = ApiConsentEvent(event)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject
        validateConsentEventApiModel(json)
    }

    @Test
    fun validate_enrolmentEventApiModel() {
        val event = EnrolmentEvent(
            10,
            UUID.randomUUID().toString()
        )

        val apiEvent = ApiEnrolmentEvent(event)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject
        validateEnrolmentEventApiModel(json)
    }

    @Test
    fun validate_completionCheckEventApiModel() {
        val event = CompletionCheckEvent(
            10,
            true
        )

        val apiEvent = ApiCompletionCheckEvent(event)
        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject
        validateCompletionCheckEventApiModel(json)
    }

    @Test
    fun validate_fingerprintCaptureEventApiModel() {
        val event = FingerprintCaptureEvent(
            10,
            10,
            FingerIdentifier.LEFT_3RD_FINGER,
            10,
            FingerprintCaptureEvent.Result.BAD_QUALITY,
            FingerprintCaptureEvent.Fingerprint(
                FingerIdentifier.LEFT_3RD_FINGER,
                10,
                "some_template".toByteArray().toString()
            ),
            randomUUID()
        )
        val apiEvent = ApiFingerprintCaptureEvent(event)

        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject
        validateFingerprintCaptureEventApiModel(json)
    }

    @Test
    fun validate_guidSelectionEventApiModel() {
        val event = GuidSelectionEvent(
            10,
            UUID.randomUUID().toString()
        )
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
            listOf(MatchEntry(UUID.randomUUID().toString(), 10F))
        )
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
            MatchEntry(UUID.randomUUID().toString(), 10F)
        )
        val apiEvent = ApiOneToOneMatchEvent(event)

        val json = gsonWithAdapters.toJsonTree(apiEvent).asJsonObject
        validateOneToOneMatchEventApiModel(json)
    }

    @Test
    fun validate_personCreationEventApiModel() {
        val event = PersonCreationEvent(
            10,
            listOf(UUID.randomUUID().toString())
        )
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
            ""
        )
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
        val suspiciousIntentEvent =
            SuspiciousIntentEvent(0, mapOf("extraFieldKey" to "someUnexpectedField"))
        val apiSuspiciousIntentEvent = ApiSuspiciousIntentEvent(suspiciousIntentEvent)
        val json = gsonWithAdapters.toJsonTree(apiSuspiciousIntentEvent).asJsonObject
        validateSuspiciousIntentEventApiModel(json)
    }

    @Test
    fun validate_scannerConnectionEventApiModel() {
        val scannerConnectionEvent = ScannerConnectionEvent(0,
            ScannerConnectionEvent.ScannerInfo("scannerId", "macAddress",
            ScannerConnectionEvent.ScannerGeneration.VERO_2, "2"))

        val apiScannerConnectionEvent = ApiScannerConnectionEvent(scannerConnectionEvent)
        val json = gsonWithAdapters.toJsonTree(apiScannerConnectionEvent).asJsonObject

        validateScannerConnectionEventApiModel(json)
    }

    @Test
    fun validate_vero2InfoSnapshotEvent() {
        val vero2InfoSnapshotEvent = Vero2InfoSnapshotEvent(0,
            Vero2InfoSnapshotEvent.Vero2Version(Int.MAX_VALUE.toLong() + 1, "1.23",
        "api", "stmApp", "stmApi", "un20App", "un20Api"),
            Vero2InfoSnapshotEvent.BatteryInfo(70, 15, 1, 37))

        val apiVero2InfoSnapshotEvent = ApiVero2InfoSnapshotEvent(vero2InfoSnapshotEvent)
        val json = gsonWithAdapters.toJsonTree(apiVero2InfoSnapshotEvent).asJsonObject

        validateVero2InfoSnapshotEventApiModel(json)
    }

    @Test
    fun validate_ScannerFirmwareUpdateEvent() {
        val scannerFirmwareUpdateEvent = ScannerFirmwareUpdateEvent(0, 0, "stm",
            "targetApp", "failureReason")

        val apiScannerFirmwareUpdateEvent = ApiScannerFirmwareUpdateEvent(scannerFirmwareUpdateEvent)
        val json = gsonWithAdapters.toJsonTree(apiScannerFirmwareUpdateEvent).asJsonObject

        validateScannerFirmwareUpdateEventApiModel(json)
    }

    @Test
    fun validate_invalidEventApiModel() {
        val invalidIntentEvent =
            InvalidIntentEvent(0, "some_action", mapOf("projectId" to "someProject"))
        val apiInvalidIntentEvent = ApiInvalidIntentEvent(invalidIntentEvent)
        val json = gsonWithAdapters.toJsonTree(apiInvalidIntentEvent).asJsonObject
        validateInvalidEventApiModel(json)
    }

    @Test
    fun validate_sessionApiModel() {
        val session = SessionCaptureEvent(
            "project_id",
            "appVersionName",
            "libVersionName",
            "en",
            Device("28", "phone", "device_id"),
            0,
            DatabaseInfo(0, 0)
        )
        session.addEvent(
            AlertScreenEvent(
                0,
                AlertScreenEvent.AlertScreenEventType.INVALID_SELECTED_ID
            )
        )
        val apiSession = ApiSessionCapture(session)
        val json = gsonWithAdapters.toJsonTree(apiSession).asJsonObject
        validateSessionEventsApiModel(json)
    }
}
