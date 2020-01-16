package com.simprints.id.services.scheduledSync.sessionSync

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.network.SimApiClient
import com.simprints.id.commontesttools.DefaultTestConstants
import com.simprints.id.commontesttools.sessionEvents.createFakeClosedSession
import com.simprints.id.commontesttools.sessionEvents.createFakeOpenSession
import com.simprints.id.commontesttools.sessionEvents.createFakeOpenSessionButExpired
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.controllers.remote.SessionsRemoteInterface
import com.simprints.id.data.analytics.eventdata.models.domain.session.SessionEvents
import com.simprints.id.exceptions.safe.session.NoSessionsFoundException
import com.simprints.id.exceptions.safe.session.SessionUploadFailureRetryException
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.TimeHelperImpl
import com.simprints.testtools.common.syntax.anyNotNull
import com.simprints.testtools.common.syntax.awaitAndAssertSuccess
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.whenever
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import retrofit2.Response
import retrofit2.adapter.rxjava2.Result

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class SessionEventsUploaderTaskTest {

    private val sessionsEventsManagerMock: SessionEventsManager = mock()
    private val timeHelper: TimeHelper = TimeHelperImpl()
    private lateinit var sessionsRemoteInterfaceSpy: SessionsRemoteInterface

    @Before
    fun setUp() {
        UnitTestConfig(this)
            .rescheduleRxMainThread()
            .setupFirebase()

        ShadowLog.stream = System.out

        sessionsRemoteInterfaceSpy = spy(SimApiClient(
            SessionsRemoteInterface::class.java,
            SessionsRemoteInterface.baseUrl,
            "").api)

        whenever(sessionsEventsManagerMock.deleteSessions(anyNotNull(), anyNotNull(), anyNotNull(), anyNotNull())).thenReturn(Completable.complete())
        whenever(sessionsEventsManagerMock.insertOrUpdateSessionEvents(anyNotNull())).thenReturn(Completable.complete())
    }

    @Test
    fun openSessionsExpired_shouldBeClosedReadyToBeUploaded() {
        createTask().apply {
            val sessions = listOf(
                createFakeOpenSession(timeHelper),
                createFakeOpenSessionButExpired(timeHelper)
            )

            val closingSessionsTask = Single.just(sessions)
                .closeOpenSessionsAndUpdateUploadTime()
                .test()
            closingSessionsTask.awaitAndAssertSuccess()

            val outputSessions = closingSessionsTask.values().first()
            outputSessions.apply {
                assertThat(size).isEqualTo(2)
                assertThat(first().isClosed()).isFalse()
                assertThat(first().relativeEndTime).isEqualTo(0)
                assertThat(get(1).isClosed()).isTrue()
                assertThat(get(1).relativeUploadTime).isNotEqualTo(0)
            }
        }
    }

    @Test
    fun closedSessions_shouldBeFilteredOutToBeUploaded() {
        createTask().apply {
            val sessions = listOf(
                createFakeOpenSession(timeHelper),
                createFakeClosedSession(timeHelper)
            )

            val filterTask = Single.just(sessions)
                .filterClosedSessions()
                .test()
            filterTask.awaitAndAssertSuccess()

            val outputSessions = filterTask.values().first()
            outputSessions.apply {
                assertThat(size).isEqualTo(1)
                assertThat(first().isClosed()).isTrue()
            }
        }
    }

    @Test
    fun sessions_shouldBeUploaded() {
        createTask().apply {
            val sessions = listOf(
                createFakeClosedSession(timeHelper),
                createFakeClosedSession(timeHelper))

            doReturn(Single.just(createSuccessUploadResponse())).`when`(sessionsRemoteInterfaceSpy).uploadSessions(anyNotNull(), anyNotNull())
            val uploadTask = Single.just(sessions)
                .uploadClosedSessionsOrThrowIfNoSessions(DefaultTestConstants.DEFAULT_PROJECT_ID)
                .test()

            uploadTask.awaitAndAssertSuccess()
        }
    }

    @Test
    fun uploadResponse201_shouldSuccess() {
        createTask().apply {

            val checkResponseTask = Single.just(createSuccessUploadResponse())
                .checkUploadSucceedAndRetryIfNecessary()
                .test()

            checkResponseTask.awaitAndAssertSuccess()
        }
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

    @Test
    fun failingUploadSessions_shouldBeRetriedMaxThreeTimes() {
        val testObserver = testUploadRequestIsNotRetriedForServerErrorCode(500, 3)
        assertThat(testObserver?.errorCount()).isEqualTo(1)
        assertThat(testObserver?.errors()?.first()).isInstanceOf(SessionUploadFailureRetryException::class.java)
    }

    @Test
    fun sessionsAfterUpload_shouldBeDeleted() {
        spy(createTask()).apply {

            val sessions = listOf(
                createFakeClosedSession(timeHelper),
                createFakeClosedSession(timeHelper),
                createFakeOpenSession(timeHelper))

            val filterTask = Single.just(sessions)
                .deleteSessionsFromDb()
                .test()
            filterTask.awaitAndAssertSuccess()

            verify(sessionsEventsManagerMock, times(sessions.size))
                .deleteSessions(isNull() as String?, anyString(), anyBoolean(), isNull() as Long?)
        }
    }

    @Test
    fun noCloseSessions_shouldThrownAnException() {
        val testObserver = executeUpload(listOf())
        testObserver.awaitTerminalEvent()

        assertThat(testObserver.errorCount()).isEqualTo(1)
        assertThat(testObserver.errors().first()).isInstanceOf(NoSessionsFoundException::class.java)
    }

    private fun testUploadRequestIsNotRetriedForServerErrorCode(code: Int, callsExpected: Int = 1): TestObserver<Void>? {
        spy(createTask()).apply {
            var requests = 0
            val checkResponseTask = Single.fromCallable {
                requests++
                createFailureUploadResponse(code)
            }
                .checkUploadSucceedAndRetryIfNecessary()
                .test()

            checkResponseTask.awaitTerminalEvent()
            assertThat(requests).isEqualTo(callsExpected)
            return checkResponseTask
        }
    }

    private fun createSuccessUploadResponse() =
        Result.response<Void>(Response.success<Void>(null, okhttp3.Response.Builder() //
            .code(201)
            .message("OK")
            .protocol(Protocol.HTTP_1_1)
            .request(Request.Builder().url("http://localhost/").build())
            .build()))


    private fun createFailureUploadResponse(code: Int = 500) =
        Result.response<Void>(Response.error(ResponseBody.create("application/json".toMediaTypeOrNull(), ""), okhttp3.Response.Builder() //
            .code(code)
            .message("AppResponse.reason()")
            .protocol(Protocol.HTTP_1_1)
            .request(Request.Builder().url("http://localhost/").build())
            .build()))

    private fun executeUpload(sessions: List<SessionEvents>): TestObserver<Void> {
        return createTask().execute(DefaultTestConstants.DEFAULT_PROJECT_ID, sessions).test()
    }

    private fun createTask() =
        SessionEventsUploaderTask(
            sessionsEventsManagerMock,
            timeHelper,
            sessionsRemoteInterfaceSpy)

}
