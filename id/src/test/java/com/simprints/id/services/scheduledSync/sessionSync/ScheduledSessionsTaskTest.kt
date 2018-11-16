package com.simprints.id.services.scheduledSync.sessionSync

import com.google.common.truth.Truth
import com.google.gson.JsonObject
import com.nhaarman.mockito_kotlin.anyOrNull
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.eventData.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventData.controllers.remote.SessionsRemoteInterface
import com.simprints.id.data.analytics.eventData.controllers.remote.apiAdapters.SessionEventsApiAdapterFactory
import com.simprints.id.data.analytics.eventData.models.domain.session.SessionEvents
import com.simprints.id.di.DaggerForTests
import com.simprints.id.network.SimApiClient
import com.simprints.id.services.scheduledSync.sessionSync.ScheduledSessionsTask.Companion.BATCH_SIZE
import com.simprints.id.shared.anyNotNull
import com.simprints.id.shared.mock
import com.simprints.id.shared.sessionEvents.createFakeClosedSession
import com.simprints.id.shared.sessionEvents.createFakeSession
import com.simprints.id.shared.waitForCompletionAndAssertNoErrors
import com.simprints.id.shared.whenever
import com.simprints.id.testUtils.base.RxJavaTest
import com.simprints.id.testUtils.roboletric.TestApplication
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.TimeHelperImpl
import com.simprints.id.tools.json.JsonHelper
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class)
class ScheduledSessionsTaskTest : RxJavaTest, DaggerForTests() {

    private val projectId = "projectId"

    private val mockServer = MockWebServer()

    private val sessionsEventsManager: SessionEventsManager = mock()
    private val analyticsEventsManager: AnalyticsManager = mock()
    private val timeHelper: TimeHelper = TimeHelperImpl()
    private lateinit var sessionsRemoteInterface: SessionsRemoteInterface

    private var sessionsInFakeDb = mutableListOf<SessionEvents>()
    
    @Before
    override fun setUp() {
        mockServer.start()
        SessionsRemoteInterface.baseUrl = this.mockServer.url("/").toString()
        sessionsRemoteInterface = SimApiClient(
            SessionsRemoteInterface::class.java,
            SessionsRemoteInterface.baseUrl,
            "",
            SessionEventsApiAdapterFactory().gson).api
        sessionsInFakeDb.clear()
    }

    @Test
    fun manySessions_shouldBeUploadedInBatches(){
        val sessionsOverBatchSize = 1
        sessionsInFakeDb.addAll(createClosedSessions(BATCH_SIZE + sessionsOverBatchSize))
        mockSessionEventsManagerToReturnFakeSessions()
        mockSessionEventsManagerInsertOrUpdateSessions()
        mockSessionEventsManagerToDeleteSessions()
        enqueueResponses(mockSuccessfulResponseForSessionsUpload(), mockSuccessfulResponseForSessionsUpload())

        val testObserver = executeSync()
        testObserver.waitForCompletionAndAssertNoErrors()

        Truth.assertThat(mockServer.requestCount).isEqualTo(2)
        verifyBodyRequestHasSessions(BATCH_SIZE, mockServer.takeRequest())
        verifyBodyRequestHasSessions(sessionsOverBatchSize, mockServer.takeRequest())
        verify(sessionsEventsManager, times(1)).deleteSessions(openSession = false)
        verify(sessionsEventsManager, times(BATCH_SIZE + sessionsOverBatchSize)).insertOrUpdateSessionEvents(anyNotNull())
        Truth.assertThat(sessionsInFakeDb.size).isEqualTo(0)
    }

    @Test
    fun openSessions_shouldNotBeUploaded() {
        sessionsInFakeDb.addAll(createClosedSessions(2))
        val openSession = createFakeSession(timeHelper, projectId, "id", timeHelper.nowMinus(1000))
        sessionsInFakeDb.add(openSession)

        mockSessionEventsManagerToReturnFakeSessions()
        mockSessionEventsManagerInsertOrUpdateSessions()
        mockSessionEventsManagerToDeleteSessions()
        enqueueResponses(mockSuccessfulResponseForSessionsUpload())

        val testObserver = executeSync()
        testObserver.waitForCompletionAndAssertNoErrors()

        verifyBodyRequestHasSessions(2, mockServer.takeRequest())
        Truth.assertThat(sessionsInFakeDb.size).isEqualTo(1)
        Truth.assertThat(sessionsInFakeDb.first().isOpen()).isTrue()
    }

    @Test
    fun openSessionsExpired_shouldBeClosedAndUploaded() {
        sessionsInFakeDb.addAll(createClosedSessions(2))
        val openSession = createFakeSession(timeHelper, projectId, "id", timeHelper.nowMinus(SessionEvents.GRACE_PERIOD + 1000))
        sessionsInFakeDb.add(openSession)

        mockSessionEventsManagerToReturnFakeSessions()
        mockSessionEventsManagerInsertOrUpdateSessions()
        mockSessionEventsManagerToDeleteSessions()
        enqueueResponses(mockSuccessfulResponseForSessionsUpload())

        val testObserver = executeSync()
        testObserver.waitForCompletionAndAssertNoErrors()

        verifyBodyRequestHasSessions(3, mockServer.takeRequest())
        Truth.assertThat(sessionsInFakeDb.size).isEqualTo(0)
    }

    @Test
    fun aBatchFailed_shouldStillDeleteSessions() {
        val sessionsOverBatchSize = 1
        sessionsInFakeDb.addAll(createClosedSessions(BATCH_SIZE + sessionsOverBatchSize))
        mockSessionEventsManagerToReturnFakeSessions()
        mockSessionEventsManagerInsertOrUpdateSessions()
        mockSessionEventsManagerToDeleteSessions()

        enqueueResponses(mockSuccessfulResponseForSessionsUpload(), mockFailureResponseForSessionsUpload())

        val testObserver = executeSync()
        testObserver.awaitTerminalEvent()

        Truth.assertThat(testObserver.errorCount()).isEqualTo(1)
        Truth.assertThat(mockServer.requestCount).isEqualTo(2)
        verifyBodyRequestHasSessions(BATCH_SIZE, mockServer.takeRequest())
        verifyBodyRequestHasSessions(sessionsOverBatchSize, mockServer.takeRequest())
        verify(sessionsEventsManager, times(1)).deleteSessions(openSession = false)
        Truth.assertThat(sessionsInFakeDb.size).isEqualTo(0)
    }

    private fun executeSync(): TestObserver<Void> {
        val syncTask = ScheduledSessionsTask(sessionsEventsManager, timeHelper, sessionsRemoteInterface, analyticsEventsManager)
        return syncTask.syncSessions(projectId).test()
    }

    private fun mockSuccessfulResponseForSessionsUpload() = MockResponse().apply {
        setResponseCode(201)
    }

    private fun mockFailureResponseForSessionsUpload() = MockResponse().apply {
        setResponseCode(500)
        setBody(Exception().toString())
    }

    private fun verifyBodyRequestHasSessions(nSessions: Int, request: RecordedRequest){
        val firstBodyRequest = request.body.readUtf8()
        val firstBodyJson = JsonHelper.gson.fromJson(firstBodyRequest, JsonObject::class.java)
        Truth.assertThat(firstBodyJson.get("sessions").asJsonArray.size()).isEqualTo(nSessions)
    }

    private fun enqueueResponses(vararg responses: MockResponse) {
        responses.iterator().forEach {
            mockServer.enqueue(it)
        }
    }

    private fun mockSessionEventsManagerToReturnFakeSessions() {
        whenever(sessionsEventsManager.loadSessions(anyNotNull(), anyOrNull())).thenAnswer {
            Single.just(arrayListOf<SessionEvents>().apply { addAll(sessionsInFakeDb ) })
        }
    }

    private fun mockSessionEventsManagerToDeleteSessions() {
       whenever(sessionsEventsManager.deleteSessions(anyOrNull(), anyOrNull())).thenAnswer {
           it.arguments[0]?.let { projectIdToRemove -> sessionsInFakeDb.removeIf { session -> session.projectId == projectIdToRemove } }
           it.arguments[1]?.let { isOpen -> sessionsInFakeDb.removeIf { session -> session.isOpen() == isOpen } }
           Completable.complete()
       }
    }

    private fun mockSessionEventsManagerInsertOrUpdateSessions() {
        whenever(sessionsEventsManager.insertOrUpdateSessionEvents(anyNotNull())).thenAnswer {
            val newSession = it.arguments[0] as SessionEvents
            sessionsInFakeDb.removeIf { session -> session.id == newSession.id }
            sessionsInFakeDb.add(newSession)
            Completable.complete()
        }
    }

    private fun createClosedSessions(nSessions: Int) =
        mutableListOf<SessionEvents>().apply {
            repeat(nSessions) { this.add(createFakeClosedSession(timeHelper, projectId)) }
        }
}
