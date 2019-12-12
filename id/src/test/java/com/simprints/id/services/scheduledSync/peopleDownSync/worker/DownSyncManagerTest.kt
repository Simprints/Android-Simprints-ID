package com.simprints.id.services.scheduledSync.peopleDownSync.worker

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.NetworkType
import androidx.work.WorkInfo
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.DownSyncManager
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.DownSyncManagerImpl
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.master.DownSyncMasterWorker
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.robolectric.annotation.Config
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class DownSyncManagerTest {

    @Inject lateinit var downSyncManager: DownSyncManager

    @Before
    @Throws(Exception::class)
    fun setUp() {
        UnitTestConfig(this).fullSetup()
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun createOneTimeWorkRequest_shouldCreateWithConstraints() {
        val workSpec = (downSyncManager as DownSyncManagerImpl).buildOneTimeRequest().workSpec
        assertEquals(NetworkType.CONNECTED, workSpec.constraints.requiredNetworkType)
        assertEquals(DownSyncMasterWorker::class.qualifiedName, workSpec.workerClassName)
        assertEquals(WorkInfo.State.ENQUEUED, workSpec.state)
    }

    @Test
    fun createPeriodicWorkRequest_shouldCreatePeriodicWithConstraints() {
        val workSpec = (downSyncManager as DownSyncManagerImpl).buildPeriodicRequest().workSpec
        assertEquals(NetworkType.CONNECTED, workSpec.constraints.requiredNetworkType)
        assertEquals(DownSyncMasterWorker::class.qualifiedName, workSpec.workerClassName)
        assertTrue(workSpec.isPeriodic)
        assertEquals(WorkInfo.State.ENQUEUED, workSpec.state)
    }
}
