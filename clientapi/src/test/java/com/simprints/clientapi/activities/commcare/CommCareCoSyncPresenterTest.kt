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
import com.simprints.eventsystem.event.domain.models.GuidSelectionEvent
import com.simprints.eventsystem.event.domain.models.RefusalEvent
import com.simprints.eventsystem.event.domain.models.callback.CallbackComparisonScore
import com.simprints.eventsystem.event.domain.models.callback.IdentificationCallbackEvent
import com.simprints.eventsystem.event.domain.models.callback.VerificationCallbackEvent
import com.simprints.eventsystem.event.domain.models.session.DatabaseInfo
import com.simprints.eventsystem.event.domain.models.session.Device
import com.simprints.eventsystem.event.domain.models.session.SessionCaptureEvent
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.GeneralConfiguration
import com.simprints.infra.config.domain.models.UpSynchronizationConfiguration.CoSyncUpSynchronizationConfiguration
import com.simprints.infra.config.domain.models.UpSynchronizationConfiguration.SimprintsUpSynchronizationConfiguration
import com.simprints.infra.config.domain.models.UpSynchronizationConfiguration.UpSynchronizationKind.*
import com.simprints.infra.enrolment.records.EnrolmentRecordManager
import com.simprints.infra.enrolment.records.domain.models.Subject
import com.simprints.libsimprints.Constants
import com.simprints.moduleapi.app.responses.IAppResponseTier.TIER_1
import io.kotest.assertions.throwables.shouldThrow
import io.mockk.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.*

class CommCareCoSyncPresenterTest {

    companion object {
        private val INTEGRATION_INFO = IntegrationInfo.COMMCARE
        const val RETURN_FOR_FLOW_COMPLETED_CHECK = true
    }

    private val view = mockk<CommCareActivity> {
        every { extras } returns mapOf(
            Pair(
                Constants.SIMPRINTS_PROJECT_ID,
                UUID.randomUUID().toString()
            )
        )
    }
    private val jsonHelper = JsonHelper

    @Test
    fun startPresenterForRegister_ShouldRequestRegister() {
        val enrolmentExtractor = EnrolRequestFactory.getMockExtractor()
        every { view.enrolExtractor } returns enrolmentExtractor

        getNewPresenter(Enrol, mockSessionManagerToCreateSession()).apply {
            runBlocking { start() }
        }

        verify(exactly = 1) {
            view.sendSimprintsRequest(
                EnrolRequestFactory.getValidSimprintsRequest(
                    INTEGRATION_INFO
                )
            )
        }
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

        getNewPresenter(
            Verify,
            mockSessionManagerToCreateSession()
        ).apply { runBlocking { start() } }

        verify(exactly = 1) {
            view.sendSimprintsRequest(
                VerifyRequestFactory.getValidSimprintsRequest(
                    INTEGRATION_INFO
                )
            )
        }
    }

    @Test
    fun startPresenterForConfirmIdentify_ShouldRequestConfirmIdentify() {
        val confirmIdentify = ConfirmIdentityFactory.getMockExtractor()
        every { view.confirmIdentityExtractor } returns confirmIdentify
        every { view.extras } returns mapOf(Pair(Constants.SIMPRINTS_SESSION_ID, MOCK_SESSION_ID))
        val sessionEventsManager = mockSessionManagerToCreateSession().also {
            coEvery { it.isSessionHasIdentificationCallback(any()) } returns true
            coEvery { it.getCurrentSessionId() } returns MOCK_SESSION_ID
        }

        getNewPresenter(ConfirmIdentity, sessionEventsManager).apply { runBlocking { start() } }

        verify(exactly = 1) {
            view.sendSimprintsRequest(
                ConfirmIdentityFactory.getValidSimprintsRequest(
                    INTEGRATION_INFO
                )
            )
        }
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
    fun `handleRegistration should return valid registration with no events`() =
        runTest(UnconfinedTestDispatcher()) {
            val registerId = UUID.randomUUID().toString()
            val sessionId = UUID.randomUUID().toString()

            val subject =
                Subject(
                    registerId,
                    "projectId",
                    "thales",
                    "mod1",
                    Date(),
                    null,
                    emptyList(),
                    emptyList()
                )
            val enrolmentRecordManager = mockk<EnrolmentRecordManager>()
            coEvery { enrolmentRecordManager.load(any()) } returns flowOf(subject)

            val prefs = mockk<SharedPreferencesManager> {
                coEvery { peekSessionId() } returns sessionId
                coEvery { popSessionId() } returns sessionId
            }
            val configManager = mockk<ConfigManager> {
                coEvery { getProjectConfiguration() } returns mockk {
                    every { synchronization } returns mockk {
                        every { up } returns mockk {
                            every { coSync } returns CoSyncUpSynchronizationConfiguration(
                                ONLY_ANALYTICS
                            )
                        }
                    }
                }
            }

            val sessionEventsManagerMock = mockk<ClientApiSessionEventsManager>()
            coEvery { sessionEventsManagerMock.getCurrentSessionId() } returns sessionId
            coEvery { sessionEventsManagerMock.getAllEventsForSession(sessionId) } returns flowOf(
                sessionCaptureEvent
            )

            getNewPresenter(
                Enrol,
                sessionEventsManagerMock,
                enrolmentRecordManager = enrolmentRecordManager,
                coroutineScope = this,
                sharedPreferencesManager = prefs,
                configManager = configManager,
            )
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
            coVerify(exactly = 1) {
                sessionEventsManagerMock.addCompletionCheckEvent(
                    RETURN_FOR_FLOW_COMPLETED_CHECK
                )
            }
            coVerify { sessionEventsManagerMock.closeCurrentSessionNormally() }
        }

    @Test
    fun `handleRegistration should return valid registration with events and actions when cosync biometrics is true`() =
        runTest(UnconfinedTestDispatcher()) {
            val projectId = UUID.randomUUID().toString()
            val registerId = UUID.randomUUID().toString()
            val sessionId = UUID.randomUUID().toString()

            val sessionEventsManagerMock = mockk<ClientApiSessionEventsManager>()
            coEvery { sessionEventsManagerMock.getCurrentSessionId() } returns sessionId
            coEvery { sessionEventsManagerMock.getAllEventsForSession(sessionId) } returns flowOf(
                sessionCaptureEvent
            )

            val subject =
                Subject(
                    registerId,
                    projectId,
                    "thales",
                    "mod1",
                    Date(),
                    null,
                    emptyList(),
                    emptyList()
                )
            val enrolmentRecordManager = mockk<EnrolmentRecordManager>()
            coEvery { enrolmentRecordManager.load(any()) } returns flowOf(subject)

            val prefs = mockk<SharedPreferencesManager> {
                coEvery { peekSessionId() } returns "sessionId"
                coEvery { popSessionId() } returns "sessionId"
            }
            val configManager = mockk<ConfigManager> {
                coEvery { getProjectConfiguration() } returns mockk {
                    every { synchronization } returns mockk {
                        every { up } returns mockk {
                            every { coSync } returns CoSyncUpSynchronizationConfiguration(
                                ONLY_BIOMETRICS
                            )
                        }
                    }
                }
            }


            getNewPresenter(
                Enrol,
                sessionEventsManagerMock,
                enrolmentRecordManager = enrolmentRecordManager,
                coroutineScope = this,
                sharedPreferencesManager = prefs,
                configManager = configManager
            ).handleEnrolResponse(EnrolResponse(registerId))

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
            coVerify(exactly = 1) {
                sessionEventsManagerMock.addCompletionCheckEvent(
                    RETURN_FOR_FLOW_COMPLETED_CHECK
                )
            }
            coVerify { sessionEventsManagerMock.closeCurrentSessionNormally() }
        }

    @Test
    fun `handleRegistration should return valid registration with events and actions when cosync all data is true`() =
        runTest(UnconfinedTestDispatcher()) {
            val projectId = UUID.randomUUID().toString()
            val registerId = UUID.randomUUID().toString()
            val sessionId = UUID.randomUUID().toString()

            val sessionEventsManagerMock = mockk<ClientApiSessionEventsManager>()
            coEvery { sessionEventsManagerMock.getCurrentSessionId() } returns sessionId
            coEvery { sessionEventsManagerMock.getAllEventsForSession(sessionId) } returns flowOf(
                sessionCaptureEvent
            )

            val subject =
                Subject(
                    registerId,
                    projectId,
                    "thales",
                    "mod1",
                    Date(),
                    null,
                    emptyList(),
                    emptyList()
                )
            val enrolmentRecordManager = mockk<EnrolmentRecordManager>()
            coEvery { enrolmentRecordManager.load(any()) } returns flowOf(subject)

            val prefs = mockk<SharedPreferencesManager> {
                coEvery { peekSessionId() } returns "sessionId"
                coEvery { popSessionId() } returns "sessionId"
            }

            val configManager = mockk<ConfigManager> {
                coEvery { getProjectConfiguration() } returns mockk {
                    every { synchronization } returns mockk {
                        every { up } returns mockk {
                            every { coSync } returns CoSyncUpSynchronizationConfiguration(ALL)
                        }
                    }
                }
            }

            getNewPresenter(
                Enrol,
                sessionEventsManagerMock,
                enrolmentRecordManager = enrolmentRecordManager,
                coroutineScope = this,
                sharedPreferencesManager = prefs,
                configManager = configManager
            ).handleEnrolResponse(EnrolResponse(registerId))

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
            coVerify(exactly = 1) {
                sessionEventsManagerMock.addCompletionCheckEvent(
                    RETURN_FOR_FLOW_COMPLETED_CHECK
                )
            }
            coVerify { sessionEventsManagerMock.closeCurrentSessionNormally() }
        }

    @Test
    fun `handleIdentification should return valid identification with events`() {
        val id1 =
            MatchResult(UUID.randomUUID().toString(), 100, Tier.TIER_1, MatchConfidence.HIGH)
        val id2 =
            MatchResult(UUID.randomUUID().toString(), 15, Tier.TIER_5, MatchConfidence.LOW)
        val idList = arrayListOf(id1, id2)
        val sessionId = UUID.randomUUID().toString()

        val sessionEventsManagerMock = mockk<ClientApiSessionEventsManager>()
        coEvery { sessionEventsManagerMock.getCurrentSessionId() } returns sessionId
        coEvery { sessionEventsManagerMock.getAllEventsForSession(sessionId) } returns flowOf(
            identificationCallbackEvent
        )

        runTest {
            getNewPresenter(Identify, sessionEventsManagerMock, coroutineScope = this)
                .handleIdentifyResponse(IdentifyResponse(arrayListOf(id1, id2), sessionId))
        }

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
        coVerify(exactly = 0) { sessionEventsManagerMock.closeCurrentSessionNormally() }
    }

    @Test
    fun `handleConfirmationResponse should return valid confirmation with events`() {
        val sessionId = UUID.randomUUID().toString()

        val sessionEventsManagerMock = mockk<ClientApiSessionEventsManager>()
        coEvery { sessionEventsManagerMock.getCurrentSessionId() } returns sessionId
        coEvery { sessionEventsManagerMock.getAllEventsForSession(sessionId) } returns flowOf(
            guidSelectionEvent
        )

        runTest {
            getNewPresenter(Enrol, sessionEventsManagerMock, coroutineScope = this)
                .handleConfirmationResponse(ConfirmationResponse(true))
        }

        verify(exactly = 1) {
            view.returnConfirmation(
                true,
                sessionId,
                "{\"events\":[${jsonHelper.toJson(guidSelectionEvent)}]}"
            )
        }
        coVerify(exactly = 1) {
            sessionEventsManagerMock.addCompletionCheckEvent(
                RETURN_FOR_FLOW_COMPLETED_CHECK
            )
        }
        coVerify(exactly = 0) { sessionEventsManagerMock.closeCurrentSessionNormally() }
    }

    @Test
    fun `handleVerification should return valid verification with events`() {
        val verification =
            VerifyResponse(
                MatchResult(
                    UUID.randomUUID().toString(),
                    100,
                    Tier.TIER_1,
                    MatchConfidence.HIGH
                )
            )
        val sessionId = UUID.randomUUID().toString()

        val sessionEventsManagerMock = mockk<ClientApiSessionEventsManager>()
        coEvery { sessionEventsManagerMock.getCurrentSessionId() } returns sessionId
        coEvery { sessionEventsManagerMock.getAllEventsForSession(sessionId) } returns flowOf(
            verificationCallbackEvent
        )

        runTest {
            getNewPresenter(
                Verify,
                sessionEventsManagerMock,
                coroutineScope = this
            ).handleVerifyResponse(verification)
        }

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
        coVerify { sessionEventsManagerMock.closeCurrentSessionNormally() }
    }

    @Test
    fun `handleResponseError should return error to client with events`() {
        val error = ErrorResponse(ErrorResponse.Reason.INVALID_USER_ID)
        val sessionId = UUID.randomUUID().toString()
        val sessionEventsManagerMock = mockk<ClientApiSessionEventsManager>()
        coEvery { sessionEventsManagerMock.getCurrentSessionId() } returns sessionId
        coEvery { sessionEventsManagerMock.getAllEventsForSession(sessionId) } returns flowOf()

        runTest {
            getNewPresenter(
                Invalid,
                sessionEventsManagerMock,
                coroutineScope = this
            ).handleResponseError(error)
        }

        verify(exactly = 1) {
            view.returnErrorToClient(
                error,
                RETURN_FOR_FLOW_COMPLETED_CHECK,
                sessionId,
                "{\"events\":[]}"
            )
        }
        coVerify { sessionEventsManagerMock.closeCurrentSessionNormally() }
    }

    @Test
    fun `handleResponseError should return error to client with no events`() {
        val error = ErrorResponse(ErrorResponse.Reason.INVALID_USER_ID)
        val sessionId = UUID.randomUUID().toString()
        val sessionEventsManagerMock = mockk<ClientApiSessionEventsManager>()
        coEvery { sessionEventsManagerMock.getCurrentSessionId() } returns sessionId
        coEvery { sessionEventsManagerMock.getAllEventsForSession(sessionId) } returns flowOf()

        val configManager = mockk<ConfigManager> {
            coEvery { getProjectConfiguration() } returns mockk {
                every { synchronization } returns mockk {
                    every { up } returns mockk {
                        every { coSync } returns CoSyncUpSynchronizationConfiguration(NONE)
                    }
                }
            }
        }
        runTest {
            getNewPresenter(
                Invalid,
                sessionEventsManagerMock,
                coroutineScope = this,
                configManager = configManager
            ).handleResponseError(error)
        }

        verify(exactly = 1) {
            view.returnErrorToClient(
                error,
                RETURN_FOR_FLOW_COMPLETED_CHECK,
                sessionId,
                null
            )
        }
        coVerify { sessionEventsManagerMock.closeCurrentSessionNormally() }
    }

    @Test
    fun `handleRefusalResponse should return valid refusal with events`() {
        val sessionId = UUID.randomUUID().toString()

        val sessionEventsManagerMock = mockk<ClientApiSessionEventsManager>()
        coEvery { sessionEventsManagerMock.getCurrentSessionId() } returns sessionId
        coEvery { sessionEventsManagerMock.getAllEventsForSession(sessionId) } returns flowOf(
            refusalEvent
        )

        runTest {
            getNewPresenter(Enrol, sessionEventsManagerMock, coroutineScope = this)
                .handleRefusalResponse(RefusalFormResponse("APP_NOT_WORKING", ""))
        }

        verify(exactly = 1) {
            view.returnExitForms(
                "APP_NOT_WORKING",
                "",
                sessionId,
                RETURN_FOR_FLOW_COMPLETED_CHECK,
                "{\"events\":[${jsonHelper.toJson(refusalEvent)}]}"
            )
        }
        coVerify(exactly = 1) {
            sessionEventsManagerMock.addCompletionCheckEvent(
                RETURN_FOR_FLOW_COMPLETED_CHECK
            )
        }
        coVerify { sessionEventsManagerMock.closeCurrentSessionNormally() }
    }

    @Test
    fun `handleRefusalResponse should delete events - if can sync to commcare`() {
        val sessionId = UUID.randomUUID().toString()

        val sessionEventsManagerMock = mockk<ClientApiSessionEventsManager>()
        coEvery { sessionEventsManagerMock.getCurrentSessionId() } returns sessionId
        coEvery { sessionEventsManagerMock.getAllEventsForSession(sessionId) } returns flowOf(
            refusalEvent
        )

        runTest {
            getNewPresenter(Enrol, sessionEventsManagerMock, coroutineScope = this)
                .handleRefusalResponse(RefusalFormResponse("APP_NOT_WORKING", ""))
        }

        verify(exactly = 1) {
            view.returnExitForms(
                "APP_NOT_WORKING",
                "",
                sessionId,
                RETURN_FOR_FLOW_COMPLETED_CHECK,
                "{\"events\":[${jsonHelper.toJson(refusalEvent)}]}"
            )
        }
        coVerify(exactly = 1) {
            sessionEventsManagerMock.addCompletionCheckEvent(
                RETURN_FOR_FLOW_COMPLETED_CHECK
            )
        }
        coVerify { sessionEventsManagerMock.closeCurrentSessionNormally() }
    }

    @Test
    fun `handleRefusalResponse should not delete events - if can sync to simprints`() {
        val sessionId = UUID.randomUUID().toString()

        val sessionEventsManagerMock = mockk<ClientApiSessionEventsManager>()
        coEvery { sessionEventsManagerMock.getCurrentSessionId() } returns sessionId
        coEvery { sessionEventsManagerMock.getAllEventsForSession(sessionId) } returns flowOf(
            refusalEvent
        )

        val configManager = mockk<ConfigManager> {
            coEvery { getProjectConfiguration() } returns mockk {
                every { synchronization } returns mockk {
                    every { up } returns mockk {
                        every { coSync } returns CoSyncUpSynchronizationConfiguration(ALL)
                        every { simprints } returns SimprintsUpSynchronizationConfiguration(ALL)
                    }
                }
            }
        }

        runTest {
            getNewPresenter(
                Enrol,
                sessionEventsManagerMock,
                configManager = configManager,
                coroutineScope = this
            ).handleRefusalResponse(RefusalFormResponse("APP_NOT_WORKING", ""))
        }

        verify(exactly = 1) {
            view.returnExitForms(
                "APP_NOT_WORKING",
                "",
                sessionId,
                RETURN_FOR_FLOW_COMPLETED_CHECK,
                "{\"events\":[${jsonHelper.toJson(refusalEvent)}]}"
            )
        }
        coVerify(exactly = 1) {
            sessionEventsManagerMock.addCompletionCheckEvent(
                RETURN_FOR_FLOW_COMPLETED_CHECK
            )
        }
        coVerify(exactly = 0) { sessionEventsManagerMock.deleteSessionEvents(sessionId) }
        coVerify { sessionEventsManagerMock.closeCurrentSessionNormally() }
    }

    @Test
    fun `handleRefusalResponse should delete events - if cannot sync to simprints`() {
        val sessionId = UUID.randomUUID().toString()

        val sessionEventsManagerMock = mockk<ClientApiSessionEventsManager>()
        coEvery { sessionEventsManagerMock.getCurrentSessionId() } returns sessionId
        coEvery { sessionEventsManagerMock.getAllEventsForSession(sessionId) } returns flowOf(
            refusalEvent
        )
        val configManager = mockk<ConfigManager> {
            coEvery { getProjectConfiguration() } returns mockk {
                every { synchronization } returns mockk {
                    every { up } returns mockk {
                        every { coSync } returns CoSyncUpSynchronizationConfiguration(ALL)
                        every { simprints } returns SimprintsUpSynchronizationConfiguration(NONE)
                    }
                }
            }
        }

        runTest {
            getNewPresenter(
                Enrol,
                sessionEventsManagerMock,
                configManager = configManager,
                coroutineScope = this
            ).handleRefusalResponse(RefusalFormResponse("APP_NOT_WORKING", ""))
        }

        verify(exactly = 1) {
            view.returnExitForms(
                "APP_NOT_WORKING",
                "",
                sessionId,
                RETURN_FOR_FLOW_COMPLETED_CHECK,
                "{\"events\":[${jsonHelper.toJson(refusalEvent)}]}"
            )
        }
        coVerify(exactly = 1) {
            sessionEventsManagerMock.addCompletionCheckEvent(
                RETURN_FOR_FLOW_COMPLETED_CHECK
            )
        }
        coVerify(exactly = 1) { sessionEventsManagerMock.deleteSessionEvents(sessionId) }
        coVerify { sessionEventsManagerMock.closeCurrentSessionNormally() }
    }

    private fun mockSessionManagerToCreateSession() = mockk<ClientApiSessionEventsManager>().apply {
        coEvery { this@apply.getCurrentSessionId() } returns "session_id"
        coEvery { this@apply.createSession(any()) } returns "session_id"
    }

    private fun mockSharedPrefs() = mockk<SharedPreferencesManager> {
        coEvery { peekSessionId() } returns "sessionId"
        coEvery { popSessionId() } returns "sessionId"
    }

    private fun mockTimeHelper() = mockk<ClientApiTimeHelper> {
        every { now() } returns 1
    }

    private fun getNewPresenter(
        action: CommCareAction,
        clientApiSessionEventsManager: ClientApiSessionEventsManager,
        sharedPreferencesManager: SharedPreferencesManager = mockSharedPrefs(),
        enrolmentRecordManager: EnrolmentRecordManager = mockk(),
        coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
        configManager: ConfigManager = mockk(),
    ): CommCarePresenter = CommCarePresenter(
        view,
        action,
        clientApiSessionEventsManager,
        sharedPreferencesManager,
        jsonHelper,
        enrolmentRecordManager,
        mockTimeHelper(),
        mockk(),
        configManager,
        coroutineScope
    )

    private val sessionCaptureEvent = SessionCaptureEvent(
        id = UUID.randomUUID().toString(),
        projectId = "23BGBiWsFmHutLGgLotu",
        createdAt = System.currentTimeMillis(),
        modalities = listOf(GeneralConfiguration.Modality.FACE),
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
