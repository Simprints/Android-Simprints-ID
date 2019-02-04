package com.simprints.id.services.scheduledSync.sessionSync

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.firebase.FirebaseApp
import com.simprints.id.activities.ShadowAndroidXMultiDex
import com.simprints.id.di.DaggerForTests
import com.simprints.id.testUtils.base.RxJavaTest
import com.simprints.id.testUtils.liveData.TestLifecycleOwner
import com.simprints.id.testUtils.roboletric.TestApplication
import com.simprints.id.testUtils.workManager.initWorkManagerIfRequired
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
        val sessionEventsSyncManagerImpl = SessionEventsSyncManagerImpl()
        var masterWorkerInfoLiveData = getWorkInfoForWorkerMaster()

        sessionEventsSyncManagerImpl.scheduleSessionsSync()
        masterWorkerInfoLiveData.observe(TestLifecycleOwner().onResume(), Observer {
            Lo.d("TEST", "TEST")
        })


//        val driver = WorkManagerTestInitHelper.getTestDriver()
//        driver.setPeriodDelayMet(it.id)
//
//        masterWorkerInfoLiveData?.let {
//            val driver = WorkManagerTestInitHelper.getTestDriver()
//            driver.setPeriodDelayMet(it.id)
//            masterWorkerInfoLiveData = getWorkInfoForWorkerMaster()
//            assertThat(masterWorkerInfoLiveData?.state).isEqualTo(WorkInfo.State.RUNNING)
//        } ?: fail("No Worker Info for MasterWorker")
    }

    private fun getWorkInfoForWorkerMaster(): LiveData<List<WorkInfo>> =
        WorkManager.getInstance().getWorkInfosForUniqueWorkLiveData(SessionEventsSyncManagerImpl.MASTER_WORKER_TAG)
}
