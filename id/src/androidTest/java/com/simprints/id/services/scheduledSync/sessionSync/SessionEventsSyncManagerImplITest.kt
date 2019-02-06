package com.simprints.id.services.scheduledSync.sessionSync

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.InstrumentationRegistry
import androidx.test.filters.SmallTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.simprints.id.Application
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.di.AppModuleForAndroidTests
import com.simprints.id.di.DaggerForAndroidTests
import com.simprints.id.shared.DefaultTestConstants
import com.simprints.id.shared.DependencyRule
import com.simprints.id.shared.PreferencesModuleForAnyTests
import com.simprints.id.testSnippets.launchActivityEnrol
import com.simprints.id.testSnippets.setupRandomGeneratorToGenerateKey
import com.simprints.id.testTemplates.FirstUseLocalAndRemote
import com.simprints.id.testTools.adapters.toCalloutCredentials
import com.simprints.id.testTools.models.TestProject
import com.simprints.id.tools.RandomGenerator
import com.simprints.id.tools.delegates.lazyVar
import com.simprints.mockscanner.MockBluetoothAdapter
import io.realm.RealmConfiguration
import junit.framework.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject


@RunWith(AndroidJUnit4::class)
@SmallTest
class SessionEventsSyncManagerImplITest : DaggerForAndroidTests(), FirstUseLocalAndRemote {

    override var peopleRealmConfiguration: RealmConfiguration? = null
    override var sessionsRealmConfiguration: RealmConfiguration? = null

    override lateinit var testProject: TestProject

    @Rule
    @JvmField
    val simprintsActionTestRule = ActivityTestRule(CheckLoginFromIntentActivity::class.java, false, false)

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Inject lateinit var remoteDbManager: RemoteDbManager
    @Inject lateinit var randomGeneratorMock: RandomGenerator

    override var preferencesModule: PreferencesModuleForAnyTests by lazyVar {
        PreferencesModuleForAnyTests(settingsPreferencesManagerRule = DependencyRule.SpyRule)
    }

    override var module by lazyVar {
        AppModuleForAndroidTests(
            app,
            randomGeneratorRule = DependencyRule.MockRule,
            bluetoothComponentAdapterRule = DependencyRule.ReplaceRule { mockBluetoothAdapter }
        )
    }

    private lateinit var mockBluetoothAdapter: MockBluetoothAdapter

    @Before
    override fun setUp() {
        app = InstrumentationRegistry.getTargetContext().applicationContext as Application
        super<DaggerForAndroidTests>.setUp()

        testAppComponent.inject(this)

        setupRandomGeneratorToGenerateKey(DefaultTestConstants.DEFAULT_REALM_KEY, randomGeneratorMock)
        app.initDependencies()

        super<FirstUseLocalAndRemote>.setUp()
        signOut()
    }

    @Test
    fun sessionEventsSyncManagerImpl_enqueuesWorkerMaster_shouldDeletePreviousWorker() {
        launchActivityEnrol(testProject.toCalloutCredentials(), simprintsActionTestRule)
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

    private fun signOut() {
        remoteDbManager.signOutOfRemoteDb()
    }
}
