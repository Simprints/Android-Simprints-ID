package com.simprints.id.data.analytics.eventdata.controllers.remote.apiAdapters

import android.net.NetworkInfo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import com.simprints.id.data.analytics.eventdata.models.domain.events.*
import com.simprints.id.data.analytics.eventdata.models.domain.events.OneToManyMatchEvent.MatchPool
import com.simprints.id.data.analytics.eventdata.models.domain.events.OneToManyMatchEvent.MatchPoolType
import com.simprints.id.data.analytics.eventdata.models.domain.session.DatabaseInfo
import com.simprints.id.data.analytics.eventdata.models.domain.session.Device
import com.simprints.id.data.analytics.eventdata.models.domain.session.Location
import com.simprints.id.data.analytics.eventdata.models.domain.session.SessionEvents
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.session.callout.Callout
import com.simprints.id.session.callout.CalloutAction
import com.simprints.id.session.callout.CalloutParameters
import com.simprints.id.commontesttools.sessionEvents.*
import com.simprints.id.testtools.TestApplication
import com.simprints.id.tools.utils.SimNetworkUtils
import com.simprints.libsimprints.FingerIdentifier
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.*

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class SessionEventsAdapterFactoryTest {

    private val gsonWithAdapters = SessionEventsApiAdapterFactory().gson

    @Test
    fun validate_alertScreenEventApiModel() {
        val event = AlertScreenEvent(0, ALERT_TYPE.NOT_PAIRED)
        val json = gsonWithAdapters.toJsonTree(event).asJsonObject
        validateAlertScreenEventApiModel(json)
    }

    @Test
    fun validate_artificialTerminationEventApiModel() {
        val event = ArtificialTerminationEvent(0, ArtificialTerminationEvent.Reason.NEW_SESSION)
        val json = gsonWithAdapters.toJsonTree(event).asJsonObject

        validateArtificialTerminationEventApiModel(json)
    }

    @Test
    fun validate_authenticationEventApiModel() {
        val event = AuthenticationEvent(
            0,
            0,
            AuthenticationEvent.UserInfo("projectId", "userId"),
            AuthenticationEvent.Result.AUTHENTICATED)
        val json = gsonWithAdapters.toJsonTree(event).asJsonObject

        validateAuthenticationEventApiModel(json)
    }

    @Test
    fun validate_authorizationEventApiModel() {
        val event = AuthorizationEvent(
            10,
            AuthorizationEvent.Result.AUTHORIZED,
            AuthorizationEvent.UserInfo("projectId", "userId"))
        val json = gsonWithAdapters.toJsonTree(event).asJsonObject

        validateAuthorizationEventApiModel(json)
    }

    @Test
    fun validate_callbackEventApiModel() {
        val event = CallbackEvent(
            10,
            Callout(CalloutAction.VERIFY, CalloutParameters(setOf())))
        val json = gsonWithAdapters.toJsonTree(event).asJsonObject

        validateCallbackEventApiModel(json)
    }

    @Test
    fun validate_calloutEventApiModel() {
        val event = CalloutEvent(
            10,
            Callout(CalloutAction.VERIFY, CalloutParameters(setOf())))
        val json = gsonWithAdapters.toJsonTree(event).asJsonObject

        validateCalloutEventApiModel(json)
    }

    @Test
    fun validate_candidateReadEventApiModel() {
        val event = CandidateReadEvent(
            10,
            10,
            UUID.randomUUID().toString(),
            CandidateReadEvent.LocalResult.FOUND,
            CandidateReadEvent.RemoteResult.NOT_FOUND)

        val json = gsonWithAdapters.toJsonTree(event).asJsonObject
        validateCandidateReadEventApiModel(json)
    }

    @Test
    fun validate_connectivitySnapshotEventApiModel(){
        val event = ConnectivitySnapshotEvent(
            10,
            "GSM",
            listOf(SimNetworkUtils.Connection("WIFI", NetworkInfo.DetailedState.CONNECTED)))

        val json = gsonWithAdapters.toJsonTree(event).asJsonObject
        validateConnectivitySnapshotEventApiModel(json)
    }

    @Test
    fun validate_consentEventApiModel(){
        val event = ConsentEvent(
            10,
            10,
            ConsentEvent.Type.INDIVIDUAL,
            ConsentEvent.Result.ACCEPTED)

        val json = gsonWithAdapters.toJsonTree(event).asJsonObject
        validateConsentEventApiModel(json)
    }

    @Test
    fun validate_enrollmentEventApiModel(){
        val event = EnrollmentEvent(
            10,
            UUID.randomUUID().toString())

        val json = gsonWithAdapters.toJsonTree(event).asJsonObject
        validateEnrolmentEventApiModel(json)
    }

    @Test
    fun validate_fingerprintCaptureEventApiModel(){
        val event = FingerprintCaptureEvent(
            10,
            10,
            FingerIdentifier.LEFT_3RD_FINGER,
            10,
            FingerprintCaptureEvent.Result.BAD_QUALITY,
            FingerprintCaptureEvent.Fingerprint(10, "some_template".toByteArray().toString()))

        val json = gsonWithAdapters.toJsonTree(event).asJsonObject
        validateFingerprintCaptureEventApiModel(json)
    }

    @Test
    fun validate_guidSelectionEventApiModel(){
        val event = GuidSelectionEvent(
            10,
            UUID.randomUUID().toString())

        val json = gsonWithAdapters.toJsonTree(event).asJsonObject
        validateGuidSelectionEventApiModel(json)
    }

    @Test
    fun validate_oneToManyMatchEventApiModel(){
        val event = OneToManyMatchEvent(
            10,
            10,
            MatchPool(MatchPoolType.MODULE, 10),
            arrayOf(MatchEntry(UUID.randomUUID().toString(), 10F)))

        val json = gsonWithAdapters.toJsonTree(event).asJsonObject
        validateOneToManyMatchEventApiModel(json)
    }

    @Test
    fun validate_oneToOneMatchEventApiModel(){
        val event = OneToOneMatchEvent(
            10,
            10,
            UUID.randomUUID().toString(),
            MatchEntry(UUID.randomUUID().toString(), 10F))

        val json = gsonWithAdapters.toJsonTree(event).asJsonObject
        validateOneToOneMatchEventApiModel(json)
    }

    @Test
    fun validate_personCreationEventApiModel(){
        val event = PersonCreationEvent(
            10,
            listOf(UUID.randomUUID().toString()))

        val json = gsonWithAdapters.toJsonTree(event).asJsonObject
        validatePersonCreationEvent(json)
    }

    @Test
    fun validate_refusalEventApiModel(){
        val event = RefusalEvent(
            10,
            10,
            RefusalEvent.Answer.OTHER,
            "")

        val json = gsonWithAdapters.toJsonTree(event).asJsonObject
        validateRefusalEventApiModel(json)
    }

    @Test
    fun validate_databaseInfoApiModel(){
        val databaseInfo = DatabaseInfo(0 ,0)
        val json = gsonWithAdapters.toJsonTree(databaseInfo).asJsonObject
        validateDatabaseInfoApiModel(json)
    }

    @Test
    fun validate_deviceApiModel(){
        val device = Device("28","phone", "device_id")
        val json = gsonWithAdapters.toJsonTree(device).asJsonObject
        validateDeviceApiModel(json)
    }

    @Test
    fun validate_locationApiModel(){
        val location = Location(1.2, 2.4)
        val json = gsonWithAdapters.toJsonTree(location).asJsonObject
        validateLocationApiModel(json)
    }

    @Test
    fun validate_sessionApiModel(){
        val session = SessionEvents(
            "project_id",
            "appVersionName",
            "libVersionName",
            "en",
            Device("28","phone", "device_id"),
            0)
        session.events.add(AlertScreenEvent(0, ALERT_TYPE.NOT_PAIRED))

        val json = gsonWithAdapters.toJsonTree(session).asJsonObject
        validateSessionEventsApiModel(json)
    }
}
