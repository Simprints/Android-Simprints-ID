package com.simprints.id.services.scheduledSync.sessionSync

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.firebase.FirebaseApp
import com.simprints.id.activities.ShadowAndroidXMultiDex
import com.simprints.id.di.DaggerForTests
import com.simprints.id.testUtils.base.RxJavaTest
import com.simprints.id.testUtils.roboletric.TestApplication
import com.simprints.id.testUtils.workManager.initWorkManagerIfRequired
import junit.framework.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class SessionEventsSyncManagerImplTest: RxJavaTest, DaggerForTests() {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @Before
    override fun setUp() {
        app = (ApplicationProvider.getApplicationContext() as TestApplication)
        FirebaseApp.initializeApp(app)
        super.setUp()
        testAppComponent.inject(this)
        initWorkManagerIfRequired(app)
    }

    @Test
    fun sessionEventsSyncManagerImpl_enqueuesWorkerMaster_shouldWorkManagerBeEnqueued(){
//        val sessionEventsSyncManagerImpl = SessionEventsSyncManagerImpl(getWorkManager = { WorkManager.getInstance() })
//        sessionEventsSyncManagerImpl.scheduleSessionsSync()
//        getWorkInfoForWorkerMaster(sessionEventsSyncManagerImpl.getMasterWorkerUniqueName(MASTER_WORKER_VERSION))
//            .observe(TestLifecycleOwner().onResume(), Observer {
//                val masterWorkerInfo = it.first()
//                val isMasterWorkerEnqueued = masterWorkerInfo.state == WorkInfo.State.ENQUEUED && masterWorkerInfo.tags.contains(MASTER_WORKER_TAG)
//                if (!isMasterWorkerEnqueued){
//                    fail("No Master Worker enqueued.")
//                }
//            })
    }

    @Test
    fun sessionEventsSyncManagerImpl_enqueuesWorkerMaster_shouldDeletePreviousWorker(){
        WorkManager.getInstance().pruneWork()

        val olderMasterTaskVersion = SessionEventsSyncManagerImpl.MASTER_WORKER_VERSION - 1
        val masterTaskVersion = SessionEventsSyncManagerImpl.MASTER_WORKER_VERSION

        val sessionEventsSyncManagerImpl = SessionEventsSyncManagerImpl()

        sessionEventsSyncManagerImpl.createAndEnqueueRequest(version = olderMasterTaskVersion)
        var uniqueNameForOldWorker = sessionEventsSyncManagerImpl.getMasterWorkerUniqueName(olderMasterTaskVersion)
        var masterWorkerInfo = getWorkInfoForWorkerMaster(uniqueNameForOldWorker).first()
        val didOlderMasterTaskRun = masterWorkerInfo.state == WorkInfo.State.ENQUEUED || masterWorkerInfo.state == WorkInfo.State.SUCCEEDED
        if (!didOlderMasterTaskRun) {
            Assert.fail("Older worker didn't run")
        }

        sessionEventsSyncManagerImpl.createAndEnqueueRequest(version = masterTaskVersion)
        uniqueNameForOldWorker = sessionEventsSyncManagerImpl.getMasterWorkerUniqueName(olderMasterTaskVersion)
        masterWorkerInfo = getWorkInfoForWorkerMaster(uniqueNameForOldWorker).first()
        val didOldMasterTaskGetCancelled = masterWorkerInfo.state == WorkInfo.State.CANCELLED
        if (!didOldMasterTaskGetCancelled) {
            Assert.fail("Older worker not cancelled")
        }

        val uniqueNameForMasterWorker = sessionEventsSyncManagerImpl.getMasterWorkerUniqueName(masterTaskVersion)
        masterWorkerInfo = getWorkInfoForWorkerMaster(uniqueNameForMasterWorker).first()
        val didMasterTaskGetCancelled = masterWorkerInfo.state == WorkInfo.State.ENQUEUED || masterWorkerInfo.state == WorkInfo.State.SUCCEEDED
        if (!didMasterTaskGetCancelled) {
            Assert.fail("Master worker not cancelled")
        }
    }

    private fun getWorkInfoForWorkerMaster(uniqueName: String): List<WorkInfo> =
        WorkManager.getInstance().getWorkInfosForUniqueWork(uniqueName).get()
}
