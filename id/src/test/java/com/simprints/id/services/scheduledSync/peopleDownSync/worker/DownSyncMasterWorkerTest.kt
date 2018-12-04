package com.simprints.id.services.scheduledSync.peopleDownSync.worker

import android.content.Context
import androidx.work.*
import com.google.firebase.FirebaseApp
import com.simprints.id.activities.ShadowAndroidXMultiDex
import com.simprints.id.di.DaggerForTests
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilder
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.ConstantsWorkManager
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.DownSyncMasterWorker
import com.simprints.id.shared.whenever
import com.simprints.id.testUtils.roboletric.TestApplication
import junit.framework.Assert.assertEquals
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
    private val uniqueNameForChainWorkers = "${ConstantsWorkManager.SYNC_WORKER_CHAIN}_${syncScope.uniqueKey}"
    private val workerKeyForSubDownSyncScope = "${ConstantsWorkManager.SUBDOWNSYNC_WORKER_TAG}_${subSyncScope.uniqueKey}"
    private val workerKeyForSubCountScope = "${ConstantsWorkManager.SUBCOUNT_WORKER_TAG}_${subSyncScope.uniqueKey}"

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
        testAppComponent.inject(this)
        MockitoAnnotations.initMocks(this)
        whenever(workParams.inputData).thenReturn(workDataOf(DownSyncMasterWorker.SYNC_WORKER_SYNC_SCOPE_INPUT to syncScopesBuilder.fromSyncScopeToJson(syncScope)))
        downSyncMasterWorker = DownSyncMasterWorker(context, workParams)
    }

    @Test
    fun doWorkTest_shouldCreateCountAndDownSyncWorkersAndSucceed() {
        val result = downSyncMasterWorker.doWork()
        val workInfo = WorkManager.getInstance()
            .getWorkInfosForUniqueWork(DownSyncMasterWorker.getSyncChainWorkersUniqueNameForSync(syncScope)).get()
        val seq = workInfo.asSequence().groupBy { it.state }

        assertEquals(numberOfBlockedInputMergerWorkers + numberOfBlockedDownSyncWorkers + numberOfEnqueuedSyncCountWorkers,
            workInfo.size)
        assertEquals(numberOfEnqueuedSyncCountWorkers, seq[WorkInfo.State.ENQUEUED]?.size)
        assertEquals(numberOfBlockedDownSyncWorkers + numberOfBlockedInputMergerWorkers, seq[WorkInfo.State.BLOCKED]?.size)
        assertEquals(ListenableWorker.Result.SUCCESS, result)
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
}
