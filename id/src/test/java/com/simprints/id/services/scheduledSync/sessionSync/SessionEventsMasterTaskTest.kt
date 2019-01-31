package com.simprints.id.services.scheduledSync.sessionSync

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.simprints.id.activities.ShadowAndroidXMultiDex
import com.simprints.id.data.analytics.eventData.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventData.models.domain.session.SessionEvents
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncMasterTask.Companion.BATCH_SIZE
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncMasterTask.Companion.SESSIONS_IDS_KEY
import com.simprints.id.shared.anyNotNull
import com.simprints.id.shared.mock
import com.simprints.id.shared.sessionEvents.createFakeClosedSession
import com.simprints.id.shared.sessionEvents.mockSessionEventsManager
import com.simprints.id.shared.waitForCompletionAndAssertNoErrors
import com.simprints.id.testUtils.base.RxJavaTest
import com.simprints.id.testUtils.roboletric.TestApplication
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.TimeHelperImpl
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.robolectric.annotation.Config


@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class SessionEventsMasterTaskTest: RxJavaTest {

    private val projectId = "projectId"


    private val sessionsEventsManagerMock: SessionEventsManager = mock()
    private val timeHelper: TimeHelper = TimeHelperImpl()
    private val workManager: WorkManager = mock()

    private var sessionsInFakeDb = mutableListOf<SessionEvents>()

    @Before
    fun setUp() {
        sessionsInFakeDb.clear()
        mockSessionEventsManager(sessionsEventsManagerMock, sessionsInFakeDb)
    }

    @Test
    fun scheduleBatches_shouldCancelAllWorkers(){
        createEnoughSessionsForTwoBatches()

        val testObserver = executeCreateBatches()
        testObserver.waitForCompletionAndAssertNoErrors()
        verify(workManager, times(1)).cancelAllWorkByTag(anyNotNull())
    }

    @Test
    fun manySessions_shouldBeUploadedInBatches(){
        createEnoughSessionsForTwoBatches()

        val testObserver = executeCreateBatches()
        testObserver.waitForCompletionAndAssertNoErrors()

        val argument = ArgumentCaptor.forClass(WorkRequest::class.java)
        verify(workManager, times(2)).enqueue(argument.capture())
        verifySessionsIdsForUploaderTask(argument.allValues.first(), 0..BATCH_SIZE)
        verifySessionsIdsForUploaderTask(argument.allValues[1], BATCH_SIZE..sessionsInFakeDb.size)
    }

    private fun createEnoughSessionsForTwoBatches() {
        val sessionsOverBatchSize = 1
        sessionsInFakeDb.addAll(createClosedSessions(BATCH_SIZE + sessionsOverBatchSize))
    }

    private fun verifySessionsIdsForUploaderTask(workRequest: WorkRequest, range: IntRange) {
        val firstBatchOfSessionsIds = workRequest.workSpec.input.getStringArray(SESSIONS_IDS_KEY)
        assertThat(firstBatchOfSessionsIds).asList().containsExactlyElementsIn(sessionsInFakeDb.subList(range.first, range.last).map { it.id })
    }


    private fun executeCreateBatches(): TestObserver<Void>  {
        val syncTask = SessionEventsSyncMasterTask(
            projectId,
            sessionsEventsManagerMock
        ) { workManager }

        return syncTask.execute().test()
    }

    private fun createClosedSessions(nSessions: Int) =
        mutableListOf<SessionEvents>().apply {
            repeat(nSessions) { this.add(createFakeClosedSession(timeHelper, projectId)) }
        }
}
