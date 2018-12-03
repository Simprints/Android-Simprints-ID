package com.simprints.id.services.scheduledSync.peopleDownSync.worker

import android.content.Context
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.firebase.FirebaseApp
import com.simprints.id.activities.ShadowAndroidXMultiDex
import com.simprints.id.di.DaggerForTests
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.DownSyncMasterWorker
import com.simprints.id.shared.whenever
import com.simprints.id.testUtils.roboletric.TestApplication
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import timber.log.Timber
import javax.inject.Inject


@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class DownSyncMasterWorkerTest: DaggerForTests() {

    @Inject lateinit var context: Context
    @Mock lateinit var workParams: WorkerParameters
    private lateinit var downSyncMasterWorker: DownSyncMasterWorker

    @Before
    override fun setUp() {
        FirebaseApp.initializeApp(RuntimeEnvironment.application)
        app = (RuntimeEnvironment.application as TestApplication)
        try {
            WorkManager.initialize(app, Configuration.Builder().build())
        } catch (e: IllegalStateException) {
            Timber.d("WorkManager already initialized")
        }
        super.setUp()
        whenever(workParams.inputData).thenReturn()
        testAppComponent.inject(this)
        MockitoAnnotations.initMocks(this)
        downSyncMasterWorker = DownSyncMasterWorker(context, workParams)
    }

    @Test
    fun test() {
        downSyncMasterWorker.doWork()
        assert(true)
    }
}
