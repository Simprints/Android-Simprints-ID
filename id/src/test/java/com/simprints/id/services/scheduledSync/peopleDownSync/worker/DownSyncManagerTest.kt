package com.simprints.id.services.scheduledSync.peopleDownSync.worker

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.NetworkType
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.firebase.FirebaseApp
import com.simprints.id.activities.ShadowAndroidXMultiDex
import com.simprints.id.di.DaggerForTests
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.DownSyncManager
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.DownSyncMasterWorker
import com.simprints.id.testUtils.roboletric.TestApplication
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.annotation.Config
import timber.log.Timber
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class DownSyncManagerTest: DaggerForTests() {

    @Mock lateinit var syncScope: SyncScope

    @Inject lateinit var downSyncManager: DownSyncManager

    @Before
    @Throws(Exception::class)
    override fun setUp() {
        app = (ApplicationProvider.getApplicationContext() as TestApplication)
        FirebaseApp.initializeApp(app)
        try {
            WorkManager.initialize(app, Configuration.Builder().build())
        } catch (e: IllegalStateException) {
            Timber.d("WorkManager already initialized")
        }
        super.setUp()
        testAppComponent.inject(this)
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
