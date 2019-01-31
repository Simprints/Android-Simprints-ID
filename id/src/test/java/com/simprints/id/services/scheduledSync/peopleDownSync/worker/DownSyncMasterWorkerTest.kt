package com.simprints.id.services.scheduledSync.peopleDownSync.worker

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.*
import com.simprints.id.activities.ShadowAndroidXMultiDex
import com.simprints.id.di.DaggerForUnitTests
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilder
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.DownSyncMasterWorker
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.WorkManagerConstants
import com.simprints.id.shared.whenever
import com.simprints.id.testUtils.roboletric.TestApplication
import com.simprints.testframework.unit.RobolectricDaggerTestConfig
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.annotation.Config
import javax.inject.Inject


@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class DownSyncMasterWorkerTest : DaggerForUnitTests() {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @Inject lateinit var context: Context
    @Inject lateinit var syncScopesBuilder: SyncScopesBuilder

    @Mock lateinit var workParams: WorkerParameters
    private lateinit var downSyncMasterWorker: DownSyncMasterWorker

    private val projectId = "projectId"
    private val userId = "userId"
    private val moduleId = setOf("moduleId1", "moduleId2", "moduleId3")
    private val syncScope = SyncScope(projectId, userId, moduleId)
    private val subSyncScope = SubSyncScope(projectId, userId, "moduleId1")
    private val numberOfEnqueuedSyncCountWorkers = 3
    private val numberOfBlockedDownSyncWorkers = 3
    private val numberOfBlockedInputMergerWorkers = 1
    private val uniqueNameForChainWorkers = "${WorkManagerConstants.SYNC_WORKER_CHAIN}_${syncScope.uniqueKey}"
    private val workerKeyForSubDownSyncScope = "${WorkManagerConstants.SUBDOWNSYNC_WORKER_TAG}_${subSyncScope.uniqueKey}"
    private val workerKeyForSubCountScope = "${WorkManagerConstants.SUBCOUNT_WORKER_TAG}_${subSyncScope.uniqueKey}"

    @Before
    fun setUp() {
        RobolectricDaggerTestConfig(this).setupAllAndFinish()

        MockitoAnnotations.initMocks(this)
        whenever(workParams.inputData).thenReturn(workDataOf(DownSyncMasterWorker.SYNC_WORKER_SYNC_SCOPE_INPUT to syncScopesBuilder.fromSyncScopeToJson(syncScope)))
        downSyncMasterWorker = DownSyncMasterWorker(context, workParams)
    }

    @Test
    fun getSyncChainWorkersUniqueNameForSync_shouldCreateUniqueName() {
        val workName = DownSyncMasterWorker.getSyncChainWorkersUniqueNameForSync(syncScope)
        assertEquals(uniqueNameForChainWorkers, workName)
    }

    @Test
    fun getDownSyncWorkerKeyForScope_shouldCreateDownSyncWorkerKey() {
        val workerKey = DownSyncMasterWorker.getDownSyncWorkerKeyForScope(subSyncScope)
        assertEquals(workerKeyForSubDownSyncScope, workerKey)
    }

    @Test
    fun getCountWorkerKeyForScope_shouldCreateCountWorkerKey() {
        val countWorkerKey = DownSyncMasterWorker.getCountWorkerKeyForScope(subSyncScope)
        assertEquals(workerKeyForSubCountScope, countWorkerKey)
    }

    @Test
    @Ignore
    // When it executes with all other tests, it fails to SQL connections execturd in the wrong threads.
    // Probably Robolectric doesn't cope well with Room and SQL.
    fun doWorkTest_shouldCreateCountAndDownSyncWorkersAndSucceed() {

        val result = downSyncMasterWorker.doWork()
        val workInfo = WorkManager.getInstance()
            .getWorkInfosForUniqueWork(DownSyncMasterWorker.getSyncChainWorkersUniqueNameForSync(syncScope)).get()
        val seq = workInfo.asSequence().groupBy { it.state }

        assertEquals(numberOfBlockedInputMergerWorkers + numberOfBlockedDownSyncWorkers + numberOfEnqueuedSyncCountWorkers,
            workInfo.size)
        assertEquals(numberOfEnqueuedSyncCountWorkers, seq[WorkInfo.State.ENQUEUED]?.size)
        assertEquals(numberOfBlockedDownSyncWorkers + numberOfBlockedInputMergerWorkers, seq[WorkInfo.State.BLOCKED]?.size)
        assert(result is ListenableWorker.Result.Success)
    }

    @Test
    @Ignore
    // Passes in isolation similar to the test above
    fun doWorkTest_shouldReturnSuccessImmediatelyWithEmptySubSyncScopeList() {
        val noModuleSyncScope = SyncScope(projectId, null, setOf())
        whenever(workParams.inputData).thenReturn(workDataOf(DownSyncMasterWorker.SYNC_WORKER_SYNC_SCOPE_INPUT to syncScopesBuilder.fromSyncScopeToJson(noModuleSyncScope)))
        downSyncMasterWorker = DownSyncMasterWorker(context, workParams)

        val result = downSyncMasterWorker.doWork()
        val workInfo = WorkManager.getInstance()
            .getWorkInfosForUniqueWork(DownSyncMasterWorker.getSyncChainWorkersUniqueNameForSync(syncScope)).get()
        val seq = workInfo.asSequence().groupBy { it.state }
        assertEquals(0, workInfo.size)
        assertEquals(null, seq[WorkInfo.State.ENQUEUED]?.size)
        assertEquals(null, seq[WorkInfo.State.BLOCKED]?.size)
        assert(result is ListenableWorker.Result.Success)
    }
}
