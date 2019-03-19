package com.simprints.id.services.scheduledSync.sessionSync

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.rule.ActivityTestRule
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkInfo.State.*
import androidx.work.WorkManager
import com.google.common.truth.Truth.assertThat
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
@SmallTest
class SessionEventsSyncManagerImplITest {

    @get:Rule val simprintsActionTestRule = ActivityTestRule(CheckLoginFromIntentActivity::class.java, false, false)

    @get:Rule var instantTaskExecutorRule = InstantTaskExecutorRule()
    private var sessionEventsSyncManager = SessionEventsSyncManagerImpl()

    @Before
    fun setUp() {
        WorkManager.getInstance().pruneWork()
    }

    @Test
    fun sessionEventsSyncManagerImpl_enqueuesANewWorkerMaster_shouldDeletePreviousWorkers() {

        val olderMasterTaskVersion = SessionEventsSyncManagerImpl.MASTER_WORKER_VERSION - 1
        val newerMasterTaskVersion = SessionEventsSyncManagerImpl.MASTER_WORKER_VERSION

        sessionEventsSyncManager.createAndEnqueueRequest(version = olderMasterTaskVersion)
        verifyStateForVersionedWorker(olderMasterTaskVersion, listOf(ENQUEUED, RUNNING))

        sessionEventsSyncManager.createAndEnqueueRequest(version = newerMasterTaskVersion)
        verifyStateForVersionedWorker(olderMasterTaskVersion, listOf(CANCELLED))
        verifyStateForVersionedWorker(newerMasterTaskVersion, listOf(ENQUEUED, RUNNING))
    }

    @Test
    fun sessionEventsSyncManagerImpl_enqueuesANewWorkerMaster_shouldDeleteOldNotVersionedWorkers() {

        val oldWorkerUUID = PeriodicWorkRequestBuilder<SessionEventsMasterWorker>(6L, TimeUnit.HOURS)
            .addTag(SessionEventsSyncManagerImpl.MASTER_WORKER_TAG)
            .build().let {
                WorkManager.getInstance().enqueueUniquePeriodicWork(
                    SessionEventsSyncManagerImpl.MASTER_WORKER_TAG,
                    ExistingPeriodicWorkPolicy.KEEP, it)
                it.id
            }

        val newerMasterTaskVersion = SessionEventsSyncManagerImpl.MASTER_WORKER_VERSION
        sessionEventsSyncManager.createAndEnqueueRequest(version = newerMasterTaskVersion)

        verifyStateForVersionedWorker(newerMasterTaskVersion, listOf(ENQUEUED, RUNNING))
        val oldWorkerState = WorkManager.getInstance().getWorkInfoById(oldWorkerUUID).get()
        assertThat(oldWorkerState.state).isEqualTo(CANCELLED)
    }

    private fun verifyStateForVersionedWorker(version: Long, possibleWorkerStates: List<WorkInfo.State>) {
        val uniqueNameForMasterWorker = sessionEventsSyncManager.getMasterWorkerUniqueName(version)
        val masterWorkerInfo = getWorkInfoForWorkerMaster(uniqueNameForMasterWorker).first()
        assertThat(masterWorkerInfo.state).isIn(possibleWorkerStates)
    }

    private fun getWorkInfoForWorkerMaster(uniqueName: String): List<WorkInfo> =
        WorkManager.getInstance().getWorkInfosForUniqueWork(uniqueName).get()
}
