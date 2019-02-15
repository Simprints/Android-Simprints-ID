package com.simprints.id.services.scheduledSync.peopleDownSync.worker

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.NetworkType
import androidx.work.WorkInfo
import com.simprints.testframework.unit.robolectric.ShadowAndroidXMultiDex
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.DownSyncManager
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.DownSyncMasterWorker
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.id.testtools.TestApplication
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.annotation.Config
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class DownSyncManagerTest {

    @Mock lateinit var syncScope: SyncScope

    @Inject lateinit var downSyncManager: DownSyncManager

    @Before
    @Throws(Exception::class)
    fun setUp() {
        UnitTestConfig(this).fullSetup()
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun createOneTimeWorkRequest_shouldCreateWithConstraints() {
        val workRequest = downSyncManager.buildOneTimeDownSyncMasterWorker(syncScope)
        val workSpec = workRequest.workSpec
        assertEquals(NetworkType.CONNECTED, workSpec.constraints.requiredNetworkType)
        assertEquals(DownSyncMasterWorker::class.qualifiedName, workSpec.workerClassName)
        assertEquals(WorkInfo.State.ENQUEUED, workSpec.state)
    }

    @Test
    fun createPeriodicWorkRequest_shouldCreatePeriodicWithConstraints() {
        val workRequest = downSyncManager.buildPeriodicDownSyncMasterWorker(syncScope)
        val workSpec = workRequest.workSpec
        assertEquals(NetworkType.CONNECTED, workSpec.constraints.requiredNetworkType)
        assertEquals(DownSyncMasterWorker::class.qualifiedName, workSpec.workerClassName)
        assertTrue(workSpec.isPeriodic)
        assertEquals(WorkInfo.State.ENQUEUED, workSpec.state)
    }
}
