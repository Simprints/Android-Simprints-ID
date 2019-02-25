package com.simprints.id.services.scheduledSync.sessionSync

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.sessionEvents.createFakeClosedSession
import com.simprints.id.commontesttools.state.mockSessionEventsManager
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.controllers.remote.SessionsRemoteInterface
import com.simprints.id.data.analytics.eventdata.models.domain.session.SessionEvents
import com.simprints.id.exceptions.safe.session.NoSessionsFoundException
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncMasterTask.Companion.BATCH_SIZE
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.TimeHelperImpl
import com.simprints.testtools.common.syntax.*
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.AdditionalAnswers
import org.mockito.Mockito
import org.mockito.Mockito.spy
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class SessionEventsMasterTaskTest {

    private val projectId = "projectId"

    private val sessionsRemoteInterfaceMock: SessionsRemoteInterface = mock()
    private val sessionsEventsManagerMock: SessionEventsManager = mock()
    private val crashReportManagerMock: CrashReportManager = mock()
    private val timeHelper: TimeHelper = TimeHelperImpl()
    private var sessionsInFakeDb = mutableListOf<SessionEvents>()

    @Before
    fun setUp() {
        UnitTestConfig(this).rescheduleRxMainThread()

        sessionsInFakeDb.clear()
        mockSessionEventsManager(sessionsEventsManagerMock, sessionsInFakeDb)
    }


    @Test
    fun manySessions_shouldGroupedInBatches() {
        with(createMasterTask()) {
            val testObserver = Single.just(createClosedSessions(BATCH_SIZE + 1).toList())
                .createBatches()
                .test()

            testObserver.awaitAndAssertSuccess()

            with(testObserver.values()) {
                assertThat(size).isEqualTo(2)
                assertThat(first().size).isEqualTo(BATCH_SIZE)
                assertThat(get(1).size).isEqualTo(1)
            }
        }
    }

    @Test
    fun manySessions_shouldBeUploadInBatches() {
        with(spy(createMasterTask())) {
            mockOneSucceedingAndOneFailingUploadTask(this, NoSessionsFoundException())
            sessionsInFakeDb.addAll(createClosedSessions(BATCH_SIZE + 1))

            val testObserver = this.execute().test()
        testObserver.awaitAndAssertSuccess()

            verifyNever(crashReportManagerMock) { logExceptionOrThrowable(anyNotNull()) }
        }
    }

    @Test
    fun manySessions_shouldGenerateMultipleTasks() {
        with(spy(createMasterTask())) {
            var batchedUploaded = 0
            val sessionsFirstBatch = createClosedSessions(BATCH_SIZE).toList()
            val sessionsSecondBatch = createClosedSessions(1)
            val batchesToUpload = Observable.fromIterable(listOf(sessionsFirstBatch, sessionsSecondBatch))
            Mockito.doReturn(Completable.complete().doOnComplete { batchedUploaded++ })
                .`when`(this).createUploadBatchTaskCompletable(anyNotNull())

            val testObserver = batchesToUpload
                .executeUploaderTask()
                .test()

            testObserver.awaitAndAssertSuccess()
            assertThat(batchedUploaded).isEqualTo(2)
        }
    }

    @Test
    fun someUploadBatchFails_shouldLogTheException() {
        with(spy(createMasterTask())) {
            mockOneSucceedingAndOneFailingUploadTask(this)

            val testObserver =
                Observable.fromIterable(listOf(createClosedSessions(BATCH_SIZE).toList(), createClosedSessions(1)))
                    .executeUploaderTask()
                    .test()

            testObserver.awaitAndAssertSuccess()
            verifyOnce(crashReportManagerMock) { logExceptionOrThrowable(anyNotNull()) }
        }
    }

    @Test
    fun someUploadBatchFailsDueToNoSessionsFoundException_shouldNoLogTheException() {
        with(spy(createMasterTask())) {
            mockOneSucceedingAndOneFailingUploadTask(this, NoSessionsFoundException())

            val testObserver =
                Observable.fromIterable(listOf(createClosedSessions(BATCH_SIZE).toList(), createClosedSessions(1)))
                    .executeUploaderTask()
                    .test()

            testObserver.awaitAndAssertSuccess()
            verifyNever(crashReportManagerMock) { logExceptionOrThrowable(anyNotNull()) }
        }
    }


    private fun mockOneSucceedingAndOneFailingUploadTask(masterTaskSpy: SessionEventsSyncMasterTask, t: Throwable = IOException("network error")) {
        Mockito.doAnswer(AdditionalAnswers.returnsElementsOf<Completable>(
            mutableListOf(Completable.complete(), Completable.error(t))
        )).`when`(masterTaskSpy).createUploadBatchTaskCompletable(anyNotNull())
    }

    private fun createMasterTask(): SessionEventsSyncMasterTask =
        SessionEventsSyncMasterTask(
            projectId,
            sessionsEventsManagerMock,
            timeHelper,
            sessionsRemoteInterfaceMock,
            crashReportManagerMock
        )

    private fun createClosedSessions(nSessions: Int) =
        mutableListOf<SessionEvents>().apply {
            repeat(nSessions) { this.add(createFakeClosedSession(timeHelper, projectId)) }
        }
}
