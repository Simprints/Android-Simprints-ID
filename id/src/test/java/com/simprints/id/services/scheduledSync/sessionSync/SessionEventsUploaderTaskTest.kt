package com.simprints.id.services.scheduledSync.sessionSync

import com.google.common.truth.Truth
import com.google.gson.JsonObject
import com.nhaarman.mockito_kotlin.anyOrNull
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.simprints.id.data.analytics.eventData.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventData.controllers.remote.SessionsRemoteInterface
import com.simprints.id.data.analytics.eventData.controllers.remote.apiAdapters.SessionEventsApiAdapterFactory
import com.simprints.id.data.analytics.eventData.models.domain.session.SessionEvents
import com.simprints.id.di.DaggerForTests
import com.simprints.id.exceptions.safe.session.NoSessionsFoundException
import com.simprints.id.exceptions.safe.session.SessionUploadFailureException
import com.simprints.id.network.SimApiClient
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncMasterTask.Companion.BATCH_SIZE
import com.simprints.id.shared.mock
import com.simprints.id.shared.sessionEvents.createFakeClosedSession
import com.simprints.id.shared.sessionEvents.createFakeSession
import com.simprints.id.shared.sessionEvents.mockSessionEventsManager
import com.simprints.id.shared.waitForCompletionAndAssertNoErrors
import com.simprints.id.testUtils.base.RxJavaTest
import com.simprints.id.testUtils.roboletric.TestApplication
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.TimeHelperImpl
import com.simprints.id.tools.json.JsonHelper
import io.reactivex.observers.TestObserver
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class)
class SessionEventsUploaderTaskTest : RxJavaTest, DaggerForTests() {

    private val projectId = "projectId"

    private val mockServer = MockWebServer()

    private val sessionsEventsManagerMock: SessionEventsManager = mock()
    private val timeHelper: TimeHelper = TimeHelperImpl()
    private lateinit var sessionsRemoteInterface: SessionsRemoteInterface

    private var sessionsInFakeDb = mutableListOf<SessionEvents>()

    @Before
    override fun setUp() {
        ShadowLog.stream = System.out

        mockServer.start()
        SessionsRemoteInterface.baseUrl = this.mockServer.url("/").toString()
        sessionsRemoteInterface = SimApiClient(
            SessionsRemoteInterface::class.java,
            SessionsRemoteInterface.baseUrl,
            "",
            SessionEventsApiAdapterFactory().gson).api
        sessionsInFakeDb.clear()

        mockSessionEventsManager(sessionsEventsManagerMock, sessionsInFakeDb)
    }

    @Test
    fun openSessions_shouldNotBeUploaded() {
        sessionsInFakeDb.addAll(createClosedSessions(2))
        val openSession = createFakeSession(timeHelper, projectId, "id", timeHelper.nowMinus(1000))
        sessionsInFakeDb.add(openSession)

        enqueueResponses(mockSuccessfulResponseForSessionsUpload())

        val testObserver = executeUpload()
        testObserver.waitForCompletionAndAssertNoErrors()

        verifyBodyRequestHasSessions(2, mockServer.takeRequest())
        Truth.assertThat(sessionsInFakeDb.size).isEqualTo(1)
        Truth.assertThat(sessionsInFakeDb.first().isOpen()).isTrue()
    }

    @Test
    fun expiredOpenSessions_shouldBeClosedAndUploaded() {
        val openSession = createFakeSession(timeHelper, projectId, "id", timeHelper.nowMinus(SessionEvents.GRACE_PERIOD + 1000))
        sessionsInFakeDb.add(openSession)
        enqueueResponses(mockSuccessfulResponseForSessionsUpload())

        val testObserver = executeUpload()
        testObserver.waitForCompletionAndAssertNoErrors()

        verifyBodyRequestHasSessions(1, mockServer.takeRequest())
        Truth.assertThat(sessionsInFakeDb.size).isEqualTo(0)
    }

    @Test
    fun closeSessions_shouldBeUploaded() {
        sessionsInFakeDb.addAll(createClosedSessions(2))
        enqueueResponses(mockSuccessfulResponseForSessionsUpload())

        val testObserver = executeUpload()
        testObserver.waitForCompletionAndAssertNoErrors()

        verifyBodyRequestHasSessions(2, mockServer.takeRequest())
        Truth.assertThat(sessionsInFakeDb.size).isEqualTo(0)
    }

    @Test
    fun uploadABatch_shouldDeleteOnlyItsSessions() {
        sessionsInFakeDb.addAll(createClosedSessions(2))
        enqueueResponses(mockSuccessfulResponseForSessionsUpload())

        val testObserver = executeUpload()
        sessionsInFakeDb.addAll(createClosedSessions(1))
        testObserver.waitForCompletionAndAssertNoErrors()

        verifyBodyRequestHasSessions(2, mockServer.takeRequest())
        Truth.assertThat(sessionsInFakeDb.size).isEqualTo(1)
    }

    @Test
    fun uploadFailsForServerError_shouldDeleteOldSessions() {
        sessionsInFakeDb.addAll(createClosedSessions(BATCH_SIZE))
        sessionsInFakeDb.forEach { it.startTime = timeHelper.nowMinus(8, TimeUnit.DAYS) }
        sessionsInFakeDb.addAll(createClosedSessions(1))

        enqueueResponses(mockFailureResponseForSessionsUpload())

        val testObserver = executeUpload()
        testObserver.awaitTerminalEvent()

        Truth.assertThat(testObserver.errorCount()).isEqualTo(1)
        Truth.assertThat(testObserver.errors().first()).isInstanceOf(SessionUploadFailureException::class.java)
        Truth.assertThat(mockServer.requestCount).isEqualTo(1)

        verifyBodyRequestHasSessions(BATCH_SIZE + 1, mockServer.takeRequest())

        verify(sessionsEventsManagerMock, times(BATCH_SIZE + 1)).deleteSessions(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())
        Truth.assertThat(sessionsInFakeDb.size).isEqualTo(1)
    }

    @Test
    fun noCloseSessions_shouldThrownAnException() {
        val testObserver = executeUpload()
        testObserver.awaitTerminalEvent()

        Truth.assertThat(testObserver.errorCount()).isEqualTo(1)
        Truth.assertThat(testObserver.errors().first()).isInstanceOf(NoSessionsFoundException::class.java)
    }

    private fun executeUpload(): TestObserver<Void> {
        val syncTask = SessionEventsUploaderTask(
            projectId,
            sessionsInFakeDb.map { it.id }.toTypedArray(),
            sessionsEventsManagerMock,
            timeHelper,
            sessionsRemoteInterface)

        return syncTask.execute().test()
    }

    private fun mockSuccessfulResponseForSessionsUpload() = MockResponse().apply {
        setResponseCode(201)
    }

    private fun mockFailureResponseForSessionsUpload() = MockResponse().apply {
        setResponseCode(500)
        setBody(Exception().toString())
    }

    private fun verifyBodyRequestHasSessions(nSessions: Int, request: RecordedRequest) {
        val firstBodyRequest = request.body.readUtf8()
        val firstBodyJson = JsonHelper.gson.fromJson(firstBodyRequest, JsonObject::class.java)
        Truth.assertThat(firstBodyJson.get("sessions").asJsonArray.size()).isEqualTo(nSessions)
    }

    private fun enqueueResponses(vararg responses: MockResponse) {
        responses.iterator().forEach {
            mockServer.enqueue(it)
        }
    }

    private fun createClosedSessions(nSessions: Int) =
        mutableListOf<SessionEvents>().apply {
            repeat(nSessions) { this.add(createFakeClosedSession(timeHelper, projectId)) }
        }
}
