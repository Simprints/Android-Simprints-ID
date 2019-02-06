package com.simprints.id.services.scheduledSync.sessionSync

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.rule.ActivityTestRule
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.firebase.firestore.util.Assert.fail
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit


@RunWith(AndroidJUnit4::class)
@SmallTest
class SessionEventsSyncManagerImplITest {

    @Rule
    @JvmField
    val simprintsActionTestRule = ActivityTestRule(CheckLoginFromIntentActivity::class.java, false, false)

    @get:Rule var instantTaskExecutorRule = InstantTaskExecutorRule()
    private var sessionEventsSyncManager = SessionEventsSyncManagerImpl()

    @Test
    fun sessionEventsSyncManagerImpl_enqueuesANewWorkerMaster_shouldDeletePreviousWorkers() {
        WorkManager.getInstance().pruneWork()

        val olderMasterTaskVersion = SessionEventsSyncManagerImpl.MASTER_WORKER_VERSION - 1
        val newerMasterTaskVersion = SessionEventsSyncManagerImpl.MASTER_WORKER_VERSION

        sessionEventsSyncManager.createAndEnqueueRequest(version = olderMasterTaskVersion)
        verifyStateForVersionedWorker(olderMasterTaskVersion, WorkInfo.State.ENQUEUED, "Older worker not enqueued")

        sessionEventsSyncManager.createAndEnqueueRequest(version = newerMasterTaskVersion)
        verifyStateForVersionedWorker(olderMasterTaskVersion, WorkInfo.State.CANCELLED, "Older worker not deleted")
        verifyStateForVersionedWorker(newerMasterTaskVersion, WorkInfo.State.ENQUEUED, "New worker not enqueued")
    }

    @Test
    fun sessionEventsSyncManagerImpl_enqueuesANewWorkerMaster_shouldDeleteOldNotVersionedWorkers() {
        WorkManager.getInstance().pruneWork()

        PeriodicWorkRequestBuilder<SessionEventsMasterWorker>(6L, TimeUnit.HOURS)
            .addTag(SessionEventsSyncManagerImpl.MASTER_WORKER_TAG)
            .build().also {
                WorkManager.getInstance().enqueueUniquePeriodicWork(
                    SessionEventsSyncManagerImpl.MASTER_WORKER_TAG,
                    ExistingPeriodicWorkPolicy.KEEP, it)
            }

        val newerMasterTaskVersion = SessionEventsSyncManagerImpl.MASTER_WORKER_VERSION

        sessionEventsSyncManager.createAndEnqueueRequest(version = newerMasterTaskVersion)

        verifyStateForVersionedWorker(olderMasterTaskVersion, WorkInfo.State.CANCELLED, "Older worker not deleted")
        verifyStateForVersionedWorker(newerMasterTaskVersion, WorkInfo.State.ENQUEUED, "New worker not enqueued")
    }

    private fun verifyStateForVersionedWorker(version: Long, workerState: WorkInfo.State, errorMessage: String){
        val uniqueNameForMasterWorker = sessionEventsSyncManager.getMasterWorkerUniqueName(version)
        val masterWorkerInfo = getWorkInfoForWorkerMaster(uniqueNameForMasterWorker).first()
        if (masterWorkerInfo.state != workerState) {
            fail(errorMessage)
        }
    }

    private fun getWorkInfoForWorkerMaster(uniqueName: String): List<WorkInfo> =
        WorkManager.getInstance().getWorkInfosForUniqueWork(uniqueName).get()
}
