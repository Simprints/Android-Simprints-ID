package com.simprints.id.services.scheduledSync.sessionSync

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.gson.JsonObject
import com.simprints.id.activities.ShadowAndroidXMultiDex
import com.simprints.id.data.analytics.eventData.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventData.controllers.remote.SessionsRemoteInterface
import com.simprints.id.data.analytics.eventData.controllers.remote.apiAdapters.SessionEventsApiAdapterFactory
import com.simprints.id.data.analytics.eventData.models.domain.session.SessionEvents
import com.simprints.id.exceptions.safe.session.NoSessionsFoundException
import com.simprints.id.network.SimApiClient
import com.simprints.testframework.common.syntax.mock
import com.simprints.id.commontesttools.sessionEvents.createFakeClosedSession
import com.simprints.id.commontesttools.sessionEvents.createFakeSession
import com.simprints.id.commontesttools.sessionEvents.mockSessionEventsManager
import com.simprints.testframework.unit.reactive.RxJavaTest
import com.simprints.id.testtools.roboletric.TestApplication
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.TimeHelperImpl
import com.simprints.id.tools.json.JsonHelper
import com.simprints.testframework.common.syntax.waitForCompletionAndAssertNoErrors
import io.reactivex.observers.TestObserver
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class SessionEventsUploaderTaskTest : RxJavaTest {

    private val mockServer = MockWebServer()

    private val sessionsEventsManagerMock: SessionEventsManager = mock()
    private val timeHelper: TimeHelper = TimeHelperImpl()
    private lateinit var sessionsRemoteInterface: SessionsRemoteInterface

    private var sessionsInFakeDb = mutableListOf<SessionEvents>()

    @Before
    fun setUp() {
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
        val openSession = createFakeSession(timeHelper, "bWOFHInKA2YaQwrxZ7uJ", "id", timeHelper.nowMinus(1000))
        sessionsInFakeDb.add(openSession)

        enqueueResponses(mockSuccessfulResponseForSessionsUpload())

        val testObserver = executeUpload()
        testObserver.waitForCompletionAndAssertNoErrors()

        verifyBodyRequestHasSessions(2, mockServer.takeRequest())
        assertThat(sessionsInFakeDb.size).isEqualTo(1)
        assertThat(sessionsInFakeDb.first().isOpen()).isTrue()
    }

    @Test
    fun expiredOpenSessions_shouldBeClosedAndUploaded() {
        val openSession = createFakeSession(timeHelper, "bWOFHInKA2YaQwrxZ7uJ", "id", timeHelper.nowMinus(SessionEvents.GRACE_PERIOD + 1000))
        sessionsInFakeDb.add(openSession)
        enqueueResponses(mockSuccessfulResponseForSessionsUpload())

        val testObserver = executeUpload()
        testObserver.waitForCompletionAndAssertNoErrors()

        verifyBodyRequestHasSessions(1, mockServer.takeRequest())
        assertThat(sessionsInFakeDb.size).isEqualTo(0)
    }

    @Test
    fun closeSessions_shouldBeUploaded() {
        sessionsInFakeDb.addAll(createClosedSessions(2))
        enqueueResponses(mockSuccessfulResponseForSessionsUpload())

        val testObserver = executeUpload()
        testObserver.waitForCompletionAndAssertNoErrors()

        verifyBodyRequestHasSessions(2, mockServer.takeRequest())
        assertThat(sessionsInFakeDb.size).isEqualTo(0)
    }

    @Test
    fun uploadABatch_shouldDeleteOnlyItsSessions() {
        sessionsInFakeDb.addAll(createClosedSessions(2))
        enqueueResponses(mockSuccessfulResponseForSessionsUpload())

        val testObserver = executeUpload()
        sessionsInFakeDb.addAll(createClosedSessions(1))
        testObserver.waitForCompletionAndAssertNoErrors()

        verifyBodyRequestHasSessions(2, mockServer.takeRequest())
        assertThat(sessionsInFakeDb.size).isEqualTo(1)
    }

    @Test
    fun noCloseSessions_shouldThrownAnException() {
        val testObserver = executeUpload()
        testObserver.awaitTerminalEvent()

        assertThat(testObserver.errorCount()).isEqualTo(1)
        assertThat(testObserver.errors().first()).isInstanceOf(NoSessionsFoundException::class.java)
    }

    @Test
    fun failingUploadSessions_shouldRetryMaxTwice() {
        sessionsInFakeDb.addAll(createClosedSessions(2))
        enqueueResponses(mockFailureResponseForSessionsUpload())
        enqueueResponses(mockFailureResponseForSessionsUpload())
        enqueueResponses(mockFailureResponseForSessionsUpload())
        enqueueResponses(mockSuccessfulResponseForSessionsUpload())

        val testObserver = executeUpload()
        testObserver.awaitTerminalEvent()

        assertThat(testObserver.errorCount()).isEqualTo(1)
        assertThat(mockServer.requestCount).isEqualTo(3)
    }

    @Test
    fun failingUploadSessionsForBadRequest_shouldNotBeRetried() {
        testUploadRequestIsNotRetriedForServerErrorCode(400)
    }

    @Test
    fun failingUploadSessionsForUnauthorized_shouldNotBeRetried() {
        testUploadRequestIsNotRetriedForServerErrorCode(401)
    }

    @Test
    fun failingUploadSessionsForForbidden_shouldNotBeRetried() {
        testUploadRequestIsNotRetriedForServerErrorCode(403)
    }

    @Test
    fun failingUploadSessionsForNotFound_shouldNotBeRetried() {
        testUploadRequestIsNotRetriedForServerErrorCode(404)
    }

    private fun testUploadRequestIsNotRetriedForServerErrorCode(code: Int) {
        sessionsInFakeDb.addAll(createClosedSessions(2))
        enqueueResponses(mockFailureResponseForSessionsUpload(code))
        enqueueResponses(mockSuccessfulResponseForSessionsUpload())

        val testObserver = executeUpload()
        testObserver.awaitTerminalEvent()

        assertThat(testObserver.errorCount()).isEqualTo(1)
        assertThat(mockServer.requestCount).isEqualTo(1)
    }

    private fun executeUpload(): TestObserver<Void> {
        val syncTask = SessionEventsUploaderTask(
            "bWOFHInKA2YaQwrxZ7uJ",
            sessionsInFakeDb.map { it.id },
            sessionsEventsManagerMock,
            timeHelper,
            sessionsRemoteInterface)

        return syncTask.execute().test()
    }

    private fun mockSuccessfulResponseForSessionsUpload() = MockResponse().apply {
        setResponseCode(201)
    }

    private fun mockFailureResponseForSessionsUpload(code: Int = 500) = MockResponse().apply {
        setResponseCode(code)
        setBody(Exception().toString())
    }

    private fun verifyBodyRequestHasSessions(nSessions: Int, request: RecordedRequest) {
        val firstBodyRequest = request.body.readUtf8()
        val firstBodyJson = JsonHelper.gson.fromJson(firstBodyRequest, JsonObject::class.java)
        assertThat(firstBodyJson.get("sessions").asJsonArray.size()).isEqualTo(nSessions)
    }

    private fun enqueueResponses(vararg responses: MockResponse) {
        responses.iterator().forEach {
            mockServer.enqueue(it)
        }
    }

    private fun createClosedSessions(nSessions: Int) =
        mutableListOf<SessionEvents>().apply {
            repeat(nSessions) { this.add(createFakeClosedSession(timeHelper, "bWOFHInKA2YaQwrxZ7uJ")) }
        }
}
