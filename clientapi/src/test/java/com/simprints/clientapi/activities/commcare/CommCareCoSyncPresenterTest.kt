package com.simprints.clientapi.activities.commcare

import com.simprints.clientapi.activities.commcare.CommCareAction.*
import com.simprints.clientapi.activities.commcare.CommCareAction.CommCareActionFollowUpAction.ConfirmIdentity
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.controllers.core.eventData.model.IntegrationInfo
import com.simprints.clientapi.data.sharedpreferences.SharedPreferencesManager
import com.simprints.clientapi.domain.responses.*
import com.simprints.clientapi.domain.responses.entities.MatchConfidence
import com.simprints.clientapi.domain.responses.entities.MatchResult
import com.simprints.clientapi.domain.responses.entities.Tier
import com.simprints.clientapi.exceptions.InvalidIntentActionException
import com.simprints.clientapi.requestFactories.ConfirmIdentityFactory
import com.simprints.clientapi.requestFactories.EnrolRequestFactory
import com.simprints.clientapi.requestFactories.IdentifyRequestFactory
import com.simprints.clientapi.requestFactories.RequestFactory.Companion.MOCK_SESSION_ID
import com.simprints.clientapi.requestFactories.VerifyRequestFactory
import com.simprints.clientapi.tools.ClientApiTimeHelper
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.event.domain.models.GuidSelectionEvent
import com.simprints.id.data.db.event.domain.models.RefusalEvent
import com.simprints.id.data.db.event.domain.models.callback.CallbackComparisonScore
import com.simprints.id.data.db.event.domain.models.callback.IdentificationCallbackEvent
import com.simprints.id.data.db.event.domain.models.callback.VerificationCallbackEvent
import com.simprints.id.data.db.event.domain.models.session.DatabaseInfo
import com.simprints.id.data.db.event.domain.models.session.Device
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.db.subject.domain.Subject
import com.simprints.id.domain.SyncDestinationSetting
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.modality.Modes
import com.simprints.id.domain.moduleapi.app.responses.entities.Tier.TIER_1
import com.simprints.libsimprints.Constants
import com.simprints.testtools.common.syntax.anyNotNull
import com.simprints.testtools.unit.BaseUnitTestConfig
import io.kotlintest.matchers.types.shouldNotBeNull
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

    private val view = mockk<CommCareActivity> {
        every { extras?.get("projectId") } returns UUID.randomUUID().toString()
    }
    private val jsonHelper = JsonHelper()

    @Before
    fun setup() {
        BaseUnitTestConfig().rescheduleRxMainThread().coroutinesMainThread()
    }

    @Test
    fun startPresenterForRegister_ShouldRequestRegister() {
        val enrolmentExtractor = EnrolRequestFactory.getMockExtractor()
        every { view.enrolExtractor } returns enrolmentExtractor

        getNewPresenter(Enrol, mockSessionManagerToCreateSession()).apply {
            runBlocking { start() }
        }

        verify(exactly = 1) { view.sendSimprintsRequest(EnrolRequestFactory.getValidSimprintsRequest(INTEGRATION_INFO)) }
    }

    @Test
    fun startPresenterForIdentify_ShouldRequestIdentify() {
        val identifyExtractor = IdentifyRequestFactory.getMockExtractor()
        every { view.identifyExtractor } returns identifyExtractor

        getNewPresenter(Identify, mockSessionManagerToCreateSession()).apply {
            runBlocking { start() }
        }

        verify(exactly = 1) {
            view.sendSimprintsRequest(
                IdentifyRequestFactory.getValidSimprintsRequest(
                    INTEGRATION_INFO
                )
            )
        }
    }

    @Test
    fun startPresenterForVerify_ShouldRequestVerify() {
        val verificationExtractor = VerifyRequestFactory.getMockExtractor()
        every { view.verifyExtractor } returns verificationExtractor

        getNewPresenter(Verify, mockSessionManagerToCreateSession()).apply { runBlocking { start() } }

        verify(exactly = 1) { view.sendSimprintsRequest(VerifyRequestFactory.getValidSimprintsRequest(INTEGRATION_INFO)) }
    }

    @Test
    fun startPresenterForConfirmIdentify_ShouldRequestConfirmIdentify() {
        val confirmIdentify = ConfirmIdentityFactory.getMockExtractor()
        every { view.confirmIdentityExtractor } returns confirmIdentify
        every { view.extras } returns mapOf(Pair(Constants.SIMPRINTS_SESSION_ID, MOCK_SESSION_ID))

        getNewPresenter(ConfirmIdentity, mockSessionManagerToCreateSession()).apply { runBlocking { start() } }

        verify(exactly = 1) { view.sendSimprintsRequest(ConfirmIdentityFactory.getValidSimprintsRequest(INTEGRATION_INFO)) }
    }

    @Test
    fun startPresenterWithGarbage_ShouldReturnActionError() {
        getNewPresenter(Invalid, mockSessionManagerToCreateSession()).apply {
            runBlocking {
                shouldThrow<InvalidIntentActionException> {
                    start()
                }
            }
        }
    }

    @Test
    fun `handleRegistration should return valid registration with events`() {
        val registerId = UUID.randomUUID().toString()
        val sessionId = UUID.randomUUID().toString()

        val sessionEventsManagerMock = mockk<ClientApiSessionEventsManager>()
        coEvery { sessionEventsManagerMock.getCurrentSessionId() } returns sessionId
        coEvery { sessionEventsManagerMock.getAllEventsForSession(sessionId) } returns flowOf(sessionCaptureEvent)

        val subjectRepository = mockk<SubjectRepository>()
        coEvery { subjectRepository.load(any()) } returns flowOf()

        getNewPresenter(Enrol, sessionEventsManagerMock, subjectRepository = subjectRepository)
            .handleEnrolResponse(EnrolResponse(registerId))

        verify(exactly = 1) {
            view.returnRegistration(
                registerId,
                sessionId,
                RETURN_FOR_FLOW_COMPLETED_CHECK,
                "{\"events\":[${jsonHelper.toJson(sessionCaptureEvent)}]}",
                null
            )
        }
        coVerify(exactly = 1) { sessionEventsManagerMock.addCompletionCheckEvent(RETURN_FOR_FLOW_COMPLETED_CHECK) }
    }

    @Test
    fun `handleRegistration should return valid registration with events and actions`() {
        val projectId = UUID.randomUUID().toString()
        val registerId = UUID.randomUUID().toString()
        val sessionId = UUID.randomUUID().toString()

        val sessionEventsManagerMock = mockk<ClientApiSessionEventsManager>()
        coEvery { sessionEventsManagerMock.getCurrentSessionId() } returns sessionId
        coEvery { sessionEventsManagerMock.getAllEventsForSession(sessionId) } returns flowOf(sessionCaptureEvent)

        val subject = Subject(registerId, projectId, "thales", "mod1", Date(), null, emptyList(), emptyList())
        val subjectRepository = mockk<SubjectRepository>()
        coEvery { subjectRepository.load(any()) } returns flowOf(subject)

        getNewPresenter(Enrol, sessionEventsManagerMock, subjectRepository = subjectRepository)
            .handleEnrolResponse(EnrolResponse(registerId))

        verify(exactly = 1) {
            view.returnRegistration(
                registerId,
                sessionId,
                RETURN_FOR_FLOW_COMPLETED_CHECK,
                "{\"events\":[${jsonHelper.toJson(sessionCaptureEvent)}]}",
                match {
                    it.contains("{\"events\":[{\"id\":") // Can't verify the ID because it's created dynamically, so checking all the rest
                    it.contains("\"labels\":{\"projectId\":\"$projectId\",\"subjectId\":\"$registerId\",\"attendantId\":\"thales\",\"moduleIds\":[\"mod1\"],\"mode\":[\"FACE\"]},")
                    it.contains("\"payload\":{\"createdAt\":1,\"eventVersion\":2,\"subjectId\":\"$registerId\",\"projectId\":\"$projectId\",\"moduleId\":\"mod1\",\"attendantId\":\"thales\",\"biometricReferences\":[],\"type\":\"ENROLMENT_RECORD_CREATION\",\"endedAt\":0},")
                    it.contains("\"type\":\"ENROLMENT_RECORD_CREATION\"}]}")
                }
            )
        }
        coVerify(exactly = 1) { sessionEventsManagerMock.addCompletionCheckEvent(RETURN_FOR_FLOW_COMPLETED_CHECK) }
    }

    @Test
    fun `handleIdentification should return valid identification with events`() {
        val id1 = MatchResult(UUID.randomUUID().toString(), 100, Tier.TIER_1, MatchConfidence.HIGH)
        val id2 = MatchResult(UUID.randomUUID().toString(), 15, Tier.TIER_5, MatchConfidence.LOW)
        val idList = arrayListOf(id1, id2)
        val sessionId = UUID.randomUUID().toString()

        val sessionEventsManagerMock = mockk<ClientApiSessionEventsManager>()
        coEvery { sessionEventsManagerMock.getCurrentSessionId() } returns sessionId
        coEvery { sessionEventsManagerMock.getAllEventsForSession(sessionId) } returns flowOf(
            identificationCallbackEvent
        )

        getNewPresenter(Identify, sessionEventsManagerMock)
            .handleIdentifyResponse(IdentifyResponse(arrayListOf(id1, id2), sessionId))

        verify(exactly = 1) {
            view.returnIdentification(
                ArrayList(idList.map {
                    com.simprints.libsimprints.Identification(
                        it.guidFound,
                        it.confidenceScore,
                        com.simprints.libsimprints.Tier.valueOf(it.tier.name)
                    )
                }),
                sessionId,
                "{\"events\":[${jsonHelper.toJson(identificationCallbackEvent)}]}"
            )
        }
    }

    @Test
    fun `handleConfirmationResponse should return valid confirmation with events`() {
        val sessionId = UUID.randomUUID().toString()

        val sessionEventsManagerMock = mockk<ClientApiSessionEventsManager>()
        coEvery { sessionEventsManagerMock.getCurrentSessionId() } returns sessionId
        coEvery { sessionEventsManagerMock.getAllEventsForSession(sessionId) } returns flowOf(guidSelectionEvent)

        getNewPresenter(Enrol, sessionEventsManagerMock)
            .handleConfirmationResponse(ConfirmationResponse(true))

        verify(exactly = 1) {
            view.returnConfirmation(
                true,
                sessionId,
                "{\"events\":[${jsonHelper.toJson(guidSelectionEvent)}]}"
            )
        }
        coVerify(exactly = 1) { sessionEventsManagerMock.addCompletionCheckEvent(RETURN_FOR_FLOW_COMPLETED_CHECK) }
    }

    @Test
    fun `handleVerification should return valid verification with events`() {
        val verification =
            VerifyResponse(MatchResult(UUID.randomUUID().toString(), 100, Tier.TIER_1, MatchConfidence.HIGH))
        val sessionId = UUID.randomUUID().toString()

        val sessionEventsManagerMock = mockk<ClientApiSessionEventsManager>()
        coEvery { sessionEventsManagerMock.getCurrentSessionId() } returns sessionId
        coEvery { sessionEventsManagerMock.getAllEventsForSession(sessionId) } returns flowOf(verificationCallbackEvent)

        getNewPresenter(Verify, sessionEventsManagerMock).handleVerifyResponse(verification)

        verify(exactly = 1) {
            view.returnVerification(
                verification.matchResult.confidenceScore,
                com.simprints.libsimprints.Tier.valueOf(verification.matchResult.tier.name),
                verification.matchResult.guidFound,
                sessionId,
                RETURN_FOR_FLOW_COMPLETED_CHECK,
                "{\"events\":[${jsonHelper.toJson(verificationCallbackEvent)}]}"
            )
        }
    }

    @Test
    fun `handleResponseError should return error to client with events`() {
        val error = ErrorResponse(ErrorResponse.Reason.INVALID_USER_ID)
        val sessionId = UUID.randomUUID().toString()
        val sessionEventsManagerMock = mockk<ClientApiSessionEventsManager>()
        coEvery { sessionEventsManagerMock.getCurrentSessionId() } returns sessionId
        coEvery { sessionEventsManagerMock.getAllEventsForSession(sessionId) } returns flowOf()

        getNewPresenter(Invalid, sessionEventsManagerMock).handleResponseError(error)

        verify(exactly = 1) {
            view.returnErrorToClient(error, RETURN_FOR_FLOW_COMPLETED_CHECK, sessionId, "{\"events\":[]}")
        }
    }

    @Test
    fun `handleRefusalResponse should return valid refusal with events`() {
        val sessionId = UUID.randomUUID().toString()

        val sessionEventsManagerMock = mockk<ClientApiSessionEventsManager>()
        coEvery { sessionEventsManagerMock.getCurrentSessionId() } returns sessionId
        coEvery { sessionEventsManagerMock.getAllEventsForSession(sessionId) } returns flowOf(refusalEvent)

        getNewPresenter(Enrol, sessionEventsManagerMock)
            .handleRefusalResponse(RefusalFormResponse("APP_NOT_WORKING", ""))

        verify(exactly = 1) {
            view.returnExitForms(
                "APP_NOT_WORKING",
                "",
                sessionId,
                RETURN_FOR_FLOW_COMPLETED_CHECK,
                "{\"events\":[${jsonHelper.toJson(refusalEvent)}]}"
            )
        }
        coVerify(exactly = 1) { sessionEventsManagerMock.addCompletionCheckEvent(RETURN_FOR_FLOW_COMPLETED_CHECK) }
    }

    @Test
    fun `handleRefusalResponse should delete events - if only sync to commcare`() {
        val sessionId = UUID.randomUUID().toString()

        val sessionEventsManagerMock = mockk<ClientApiSessionEventsManager>()
        coEvery { sessionEventsManagerMock.getCurrentSessionId() } returns sessionId
        coEvery { sessionEventsManagerMock.getAllEventsForSession(sessionId) } returns flowOf(refusalEvent)

        getNewPresenter(Enrol, sessionEventsManagerMock)
            .handleRefusalResponse(RefusalFormResponse("APP_NOT_WORKING", ""))

        verify(exactly = 1) {
            view.returnExitForms(
                "APP_NOT_WORKING",
                "",
                sessionId,
                RETURN_FOR_FLOW_COMPLETED_CHECK,
                "{\"events\":[${jsonHelper.toJson(refusalEvent)}]}"
            )
        }
        coVerify(exactly = 1) { sessionEventsManagerMock.addCompletionCheckEvent(RETURN_FOR_FLOW_COMPLETED_CHECK) }
        coVerify(exactly = 1) { sessionEventsManagerMock.deleteSessionEvents(sessionId) }
    }

    @Test
    fun `handleRefusalResponse shouldnt delete events - if sync has simprints`() {
        val sessionId = UUID.randomUUID().toString()

        val sessionEventsManagerMock = mockk<ClientApiSessionEventsManager>()
        coEvery { sessionEventsManagerMock.getCurrentSessionId() } returns sessionId
        coEvery { sessionEventsManagerMock.getAllEventsForSession(sessionId) } returns flowOf(refusalEvent)
        val sharedPreferences = mockSharedPrefs().apply {
            every { this@apply.syncDestinationSettings } returns listOf(
                SyncDestinationSetting.COMMCARE,
                SyncDestinationSetting.SIMPRINTS
            )
        }

        getNewPresenter(
            Enrol,
            sessionEventsManagerMock,
            sharedPreferences
        ).handleRefusalResponse(RefusalFormResponse("APP_NOT_WORKING", ""))

        verify(exactly = 1) {
            view.returnExitForms(
                "APP_NOT_WORKING",
                "",
                sessionId,
                RETURN_FOR_FLOW_COMPLETED_CHECK,
                "{\"events\":[${jsonHelper.toJson(refusalEvent)}]}"
            )
        }
        coVerify(exactly = 1) { sessionEventsManagerMock.addCompletionCheckEvent(RETURN_FOR_FLOW_COMPLETED_CHECK) }
        coVerify(exactly = 0) { sessionEventsManagerMock.deleteSessionEvents(sessionId) }
    }

    private fun mockSessionManagerToCreateSession() = mockk<ClientApiSessionEventsManager>().apply {
        coEvery { this@apply.getCurrentSessionId() } returns "session_id"
        coEvery { this@apply.createSession(any()) } returns "session_id"
    }

    private fun mockSharedPrefs() = mockk<SharedPreferencesManager>().apply {
        coEvery { this@apply.peekSessionId() } returns "sessionId"
        coEvery { this@apply.popSessionId() } returns "sessionId"
        every { this@apply.syncDestinationSettings } returns listOf(SyncDestinationSetting.COMMCARE)
        every { this@apply.modalities } returns listOf(Modality.FACE)
    }

    private fun mockTimeHelper() = mockk<ClientApiTimeHelper> {
        every { now() } returns 1
    }

    private fun getNewPresenter(
        action: CommCareAction,
        clientApiSessionEventsManager: ClientApiSessionEventsManager,
        sharedPreferencesManager: SharedPreferencesManager = mockSharedPrefs(),
        subjectRepository: SubjectRepository = mockk()
    ): CommCarePresenter = CommCarePresenter(
        view,
        action,
        clientApiSessionEventsManager,
        sharedPreferencesManager,
        jsonHelper,
        subjectRepository,
        mockTimeHelper(),
        mockk(),
        mockk()
    )

    private val sessionCaptureEvent = SessionCaptureEvent(
        id = UUID.randomUUID().toString(),
        projectId = "23BGBiWsFmHutLGgLotu",
        createdAt = System.currentTimeMillis(),
        modalities = listOf(Modes.FACE),
        appVersionName = "test",
        libVersionName = "2020.3.0",
        language = "en",
        device = Device(
            androidSdkVersion = "29",
            deviceModel = "HUAWEI_VOG - L09",
            deviceId = "b88d3b6bc2765a52"
        ),
        databaseInfo = DatabaseInfo(sessionCount = 0, recordCount = 0)
    )

    private val refusalEvent = RefusalEvent(
        createdAt = 2,
        endTime = 4,
        reason = RefusalEvent.RefusalPayload.Answer.APP_NOT_WORKING,
        otherText = ""
    )

    private val identificationCallbackEvent = IdentificationCallbackEvent(
        createdAt = 2,
        sessionId = UUID.randomUUID().toString(),
        scores = listOf(
            CallbackComparisonScore(UUID.randomUUID().toString(), 1, TIER_1)
        )
    )

    private val guidSelectionEvent = GuidSelectionEvent(
        2,
        UUID.randomUUID().toString()
    )

    private val verificationCallbackEvent = VerificationCallbackEvent(
        2,
        CallbackComparisonScore(UUID.randomUUID().toString(), 1, TIER_1)
    )
}
