package com.simprints.clientapi.activities.commcare

import com.simprints.clientapi.activities.commcare.CommCareAction.*
import com.simprints.clientapi.activities.commcare.CommCareAction.CommCareActionFollowUpAction.ConfirmIdentity
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo
import com.simprints.clientapi.data.sharedpreferences.SharedPreferencesManager
import com.simprints.clientapi.domain.responses.EnrolResponse
import com.simprints.clientapi.domain.responses.ErrorResponse
import com.simprints.clientapi.domain.responses.IdentifyResponse
import com.simprints.clientapi.domain.responses.VerifyResponse
import com.simprints.clientapi.domain.responses.entities.MatchConfidence
import com.simprints.clientapi.domain.responses.entities.MatchResult
import com.simprints.clientapi.domain.responses.entities.Tier
import com.simprints.clientapi.exceptions.InvalidIntentActionException
import com.simprints.clientapi.requestFactories.ConfirmIdentityFactory
import com.simprints.clientapi.requestFactories.EnrolRequestFactory
import com.simprints.clientapi.requestFactories.IdentifyRequestFactory
import com.simprints.clientapi.requestFactories.RequestFactory.Companion.MOCK_SESSION_ID
import com.simprints.clientapi.requestFactories.VerifyRequestFactory
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.domain.models.session.DatabaseInfo
import com.simprints.id.data.db.event.domain.models.session.Device
import com.simprints.id.data.db.event.domain.models.session.Location
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent
import com.simprints.id.domain.SyncDestinationSetting
import com.simprints.id.domain.modality.Modes
import com.simprints.libsimprints.Constants
import com.simprints.testtools.unit.BaseUnitTestConfig
import io.kotlintest.shouldThrow
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import java.util.*

class CommCareCoSyncPresenterTest {

    companion object {
        private val INTEGRATION_INFO = IntegrationInfo.COMMCARE
        const val RETURN_FOR_FLOW_COMPLETED_CHECK = true
    }

    private val view = mockk<CommCareActivity>()
    private val jsonHelper = JsonHelper()
    private val syncDestinationSetting = SyncDestinationSetting.COMMCARE

    @Before
    fun setup() {
        BaseUnitTestConfig().rescheduleRxMainThread().coroutinesMainThread()
    }

    @Test
    fun startPresenterForRegister_ShouldRequestRegister() {
        val enrolmentExtractor = EnrolRequestFactory.getMockExtractor()
        every { view.enrolExtractor } returns enrolmentExtractor

        CommCarePresenter(
            view,
            Enrol,
            mockSessionManagerToCreateSession(),
            mockSharedPrefs(),
            mockk(),
            mockk(),
            jsonHelper,
            syncDestinationSetting
        ).apply {
            runBlocking { start() }
        }

        verify(exactly = 1) { view.sendSimprintsRequest(EnrolRequestFactory.getValidSimprintsRequest(INTEGRATION_INFO)) }
    }

    @Test
    fun startPresenterForIdentify_ShouldRequestIdentify() {
        runBlocking {
            val identifyExtractor = IdentifyRequestFactory.getMockExtractor()
            every { view.identifyExtractor } returns identifyExtractor

            CommCarePresenter(
                view,
                Identify,
                mockSessionManagerToCreateSession(),
                mockSharedPrefs(),
                mockk(relaxed = true),
                mockk(relaxed = true),
                jsonHelper,
                syncDestinationSetting
            ).apply {
                start()
            }

            verify(exactly = 1) {
                view.sendSimprintsRequest(
                    IdentifyRequestFactory.getValidSimprintsRequest(
                        INTEGRATION_INFO
                    )
                )
            }
        }
    }

    @Test
    fun startPresenterForVerify_ShouldRequestVerify() {
        val verificationExtractor = VerifyRequestFactory.getMockExtractor()
        every { view.verifyExtractor } returns verificationExtractor

        CommCarePresenter(
            view,
            Verify,
            mockSessionManagerToCreateSession(),
            mockSharedPrefs(),
            mockk(),
            mockk(),
            jsonHelper,
            syncDestinationSetting
        ).apply { runBlocking { start() } }

        verify(exactly = 1) { view.sendSimprintsRequest(VerifyRequestFactory.getValidSimprintsRequest(INTEGRATION_INFO)) }
    }

    @Test
    fun startPresenterForConfirmIdentify_ShouldRequestConfirmIdentify() {
        val confirmIdentify = ConfirmIdentityFactory.getMockExtractor()
        every { view.confirmIdentityExtractor } returns confirmIdentify
        every { view.extras } returns mapOf(Pair(Constants.SIMPRINTS_SESSION_ID, MOCK_SESSION_ID))

        CommCarePresenter(
            view,
            ConfirmIdentity,
            mockSessionManagerToCreateSession(),
            mockSharedPrefs(),
            mockk(),
            mockk(),
            jsonHelper,
            syncDestinationSetting
        ).apply { runBlocking { start() } }

        verify(exactly = 1) { view.sendSimprintsRequest(ConfirmIdentityFactory.getValidSimprintsRequest(INTEGRATION_INFO)) }
    }

    @Test
    fun startPresenterWithGarbage_ShouldReturnActionError() {
        CommCarePresenter(
            view,
            Invalid,
            mockSessionManagerToCreateSession(),
            mockSharedPrefs(),
            mockk(),
            mockk(),
            jsonHelper,
            syncDestinationSetting
        ).apply {
            runBlocking {
                shouldThrow<InvalidIntentActionException> {
                    start()
                }
            }
        }
    }

    @Test
    fun handleRegistration_ShouldReturnValidRegistration() {
        val registerId = UUID.randomUUID().toString()
        val sessionId = UUID.randomUUID().toString()

        val sessionEventsManagerMock = mockk<ClientApiSessionEventsManager>()
        coEvery { sessionEventsManagerMock.getCurrentSessionId() } returns sessionId
        coEvery { sessionEventsManagerMock.getAllEventsForSession(sessionId) } returns flowOf(sessionCaptureEvent)

        CommCarePresenter(
            view,
            Enrol,
            sessionEventsManagerMock,
            mockSharedPrefs(),
            mockk(),
            mockk(),
            jsonHelper,
            syncDestinationSetting
        ).handleEnrolResponse(EnrolResponse(registerId))

        verify(exactly = 1) {
            view.returnRegistration(
                registerId,
                sessionId,
                RETURN_FOR_FLOW_COMPLETED_CHECK,
                "{\"events\":[${jsonHelper.toJson(sessionCaptureEvent)}]}"
            )
        }
        coVerify(exactly = 1) { sessionEventsManagerMock.addCompletionCheckEvent(RETURN_FOR_FLOW_COMPLETED_CHECK) }
    }

    @Test
    fun handleIdentification_ShouldReturnValidIdentification() {
        val id1 = MatchResult(UUID.randomUUID().toString(), 100, Tier.TIER_1, MatchConfidence.HIGH)
        val id2 = MatchResult(UUID.randomUUID().toString(), 15, Tier.TIER_5, MatchConfidence.LOW)
        val idList = arrayListOf(id1, id2)
        val sessionId = UUID.randomUUID().toString()

        CommCarePresenter(
            view,
            Identify,
            mockk(),
            mockSharedPrefs(),
            mockk(),
            mockk(),
            jsonHelper,
            syncDestinationSetting
        ).handleIdentifyResponse(
            IdentifyResponse(arrayListOf(id1, id2), sessionId)
        )

        verify(exactly = 1) {
            view.returnIdentification(
                ArrayList(idList.map {
                    com.simprints.libsimprints.Identification(
                        it.guidFound,
                        it.confidenceScore,
                        com.simprints.libsimprints.Tier.valueOf(it.tier.name)
                    )
                }), sessionId
            )
        }
    }

    @Test
    fun handleVerification_ShouldReturnValidVerification() {
        val verification =
            VerifyResponse(MatchResult(UUID.randomUUID().toString(), 100, Tier.TIER_1, MatchConfidence.HIGH))
        val sessionId = UUID.randomUUID().toString()

        val sessionEventsManagerMock = mockk<ClientApiSessionEventsManager>()
        coEvery { sessionEventsManagerMock.getCurrentSessionId() } returns sessionId

        CommCarePresenter(
            view,
            Verify,
            sessionEventsManagerMock,
            mockSharedPrefs(),
            mockk(),
            mockk(),
            jsonHelper,
            syncDestinationSetting
        ).handleVerifyResponse(verification)

        verify(exactly = 1) {
            view.returnVerification(
                verification.matchResult.confidenceScore,
                com.simprints.libsimprints.Tier.valueOf(verification.matchResult.tier.name),
                verification.matchResult.guidFound,
                sessionId,
                RETURN_FOR_FLOW_COMPLETED_CHECK
            )
        }
    }

    @Test
    fun handleResponseError_ShouldCallActionError() {
        val error = ErrorResponse(ErrorResponse.Reason.INVALID_USER_ID)
        val sessionId = UUID.randomUUID().toString()
        val sessionEventsManagerMock = mockk<ClientApiSessionEventsManager>()
        coEvery { sessionEventsManagerMock.getCurrentSessionId() } returns sessionId
        coEvery { sessionEventsManagerMock.getAllEventsForSession(sessionId) } returns flowOf()

        CommCarePresenter(
            view,
            Invalid,
            sessionEventsManagerMock,
            mockSharedPrefs(),
            mockk(),
            mockk(),
            jsonHelper,
            syncDestinationSetting
        ).handleResponseError(error)

        verify(exactly = 1) {
            view.returnErrorToClient(error, RETURN_FOR_FLOW_COMPLETED_CHECK, sessionId, "{\"events\":[]}")
        }
    }

    private fun mockSessionManagerToCreateSession() = mockk<ClientApiSessionEventsManager>().apply {
        coEvery { this@apply.getCurrentSessionId() } returns "session_id"
        coEvery { this@apply.createSession(any()) } returns "session_id"
    }

    private fun mockSharedPrefs() = mockk<SharedPreferencesManager>().apply {
        coEvery { this@apply.peekSessionId() } returns "sessionId"
        coEvery { this@apply.popSessionId() } returns "sessionId"
    }

    private val sessionCaptureEvent = SessionCaptureEvent(
        id = "98ba1e99-5eed-458a-bdc4-8ac69429b91a",
        type = EventType.SESSION_CAPTURE,
        labels = EventLabels(
            projectId = "23BGBiWsFmHutLGgLotu",
            sessionId = "98ba1e99-5eed-458a-bdc4-8ac69429b91a",
            deviceId = "b88d3b6bc2765a52"
        ),
        payload = SessionCaptureEvent.SessionCapturePayload(
            eventVersion = 1,
            id = "98ba1e99-5eed-458a-bdc4-8ac69429b91a",
            projectId = "23BGBiWsFmHutLGgLotu",
            createdAt = 1611315466612,
            modalities = listOf(Modes.FACE),
            appVersionName = "development - build - dev",
            libVersionName = "2020.3.0",
            language = "en",
            device = Device(
                androidSdkVersion = "29",
                deviceModel = "HUAWEI_VOG - L09",
                deviceId = "b88d3b6bc2765a52"
            ),
            databaseInfo = DatabaseInfo(sessionCount = 7, recordCount = 17),
            location = Location(latitude = 52.2145821, longitude = 0.1381978),
            analyticsId = null,
            endedAt = 0,
            uploadedAt = 0,
            type = EventType.SESSION_CAPTURE
        )
    )

}
