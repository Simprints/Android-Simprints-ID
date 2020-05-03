package com.simprints.id.services.scheduledSync.subjects.master

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkInfo
import androidx.work.WorkInfo.State.FAILED
import androidx.work.WorkInfo.State.SUCCEEDED
import androidx.work.workDataOf
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.services.scheduledSync.subjects.common.TAG_MASTER_SYNC_ID
import com.simprints.id.services.scheduledSync.subjects.common.TAG_PEOPLE_DOWN_SYNC_ALL_WORKERS
import com.simprints.id.services.scheduledSync.subjects.common.TAG_PEOPLE_SYNC_ALL_WORKERS
import com.simprints.id.services.scheduledSync.subjects.common.TAG_SCHEDULED_AT
import com.simprints.id.services.scheduledSync.subjects.master.internal.SubjectsSyncCache
import com.simprints.id.services.scheduledSync.subjects.master.internal.SyncWorkersLiveDataProvider
import com.simprints.id.services.scheduledSync.subjects.master.models.SubjectsSyncWorkerType.Companion.tagForType
import com.simprints.id.services.scheduledSync.subjects.master.models.SubjectsSyncWorkerType.START_SYNC_REPORTER
import com.simprints.id.services.scheduledSync.subjects.master.workers.SubjectsStartSyncReporterWorker.Companion.SYNC_ID_STARTED
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.common.livedata.testObserver
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class SubjectsSyncStateProcessorImplTest {

    companion object {
        const val UNIQUE_SYNC_ID = "UNIQUE_SYNC_ID"
        const val UNIQUE_DOWN_SYNC_ID = "UNIQUE_DOWN_SYNC_ID"
        const val UNIQUE_UP_SYNC_ID = "UNIQUE_UP_SYNC_ID"

        const val DOWNLOADED = 100
        const val TO_DOWNLOAD = 100
        const val UPLOADED = 10
        const val TO_UPLOAD = 10
    }

    private val successfulMasterWorkers: List<WorkInfo> =
        listOf(createStartSyncRerporterWorker(SUCCEEDED, "${UNIQUE_SYNC_ID}_older"), createStartSyncRerporterWorker(SUCCEEDED, UNIQUE_SYNC_ID))

    private val failedMasterWorkers: List<WorkInfo> =
        listOf(createWorkInfo(FAILED))


    private val ctx: Context = ApplicationProvider.getApplicationContext()

    private var startSyncReporterWorker = MutableLiveData<List<WorkInfo>>()
    private var syncWorkersLiveData = MutableLiveData<List<WorkInfo>>()

    lateinit var subjectsSyncStateProcessor: SubjectsSyncStateProcessor
    @RelaxedMockK lateinit var personRepository: SubjectRepository
    @RelaxedMockK lateinit var syncWorkersLiveDataProvider: SyncWorkersLiveDataProvider
    @RelaxedMockK lateinit var subjectsSyncCache: SubjectsSyncCache

    @Before
    fun setUp() {
        UnitTestConfig(this).setupWorkManager()
        MockKAnnotations.init(this)
        subjectsSyncStateProcessor = SubjectsSyncStateProcessorImpl(ctx, personRepository, subjectsSyncCache, syncWorkersLiveDataProvider)
        mockDependencies()
    }

    @Test
    fun processor_masterWorkerCompletes_shouldExtractTheUniqueSyncId() = runBlockingTest {
        startSyncReporterWorker.value = successfulMasterWorkers

        subjectsSyncStateProcessor.getLastSyncState().testObserver()

        verify { syncWorkersLiveDataProvider.getSyncWorkersLiveData(UNIQUE_SYNC_ID) }
    }

    @Test
    fun processor_masterWorkerFails_shouldNotExtractTheUniqueSyncId() = runBlockingTest {
        startSyncReporterWorker.value = failedMasterWorkers

        subjectsSyncStateProcessor.getLastSyncState().testObserver()

        verify(exactly = 0) { syncWorkersLiveDataProvider.getSyncWorkersLiveData(UNIQUE_SYNC_ID) }
    }

    @Test
    fun processor_allWorkersSucceed_shouldSyncStateBeSuccess() = runBlockingTest {
        startSyncReporterWorker.value = successfulMasterWorkers
        syncWorkersLiveData.value = createWorkInfosHistoryForSuccessfulSync()

        val syncStates = subjectsSyncStateProcessor.getLastSyncState().testObserver().observedValues

        val lastSyncState = syncStates.last()
        lastSyncState!!.assertSuccessfulSyncState()
    }

    @Test
    fun processor_oneWorkerStillRunning_shouldSyncStateBeRunning() = runBlockingTest {
        startSyncReporterWorker.value = successfulMasterWorkers
        syncWorkersLiveData.value = createWorkInfosHistoryForRunningSync()

        val syncStates = subjectsSyncStateProcessor.getLastSyncState().testObserver().observedValues

        val lastSyncState = syncStates.last()
        lastSyncState!!.assertRunningSyncState()
    }

    @Test
    fun processor_oneWorkerFailed_shouldSyncStateBeFail() = runBlockingTest {
        startSyncReporterWorker.value = successfulMasterWorkers
        syncWorkersLiveData.value = createWorkInfosHistoryForFailingSync()

        val syncStates = subjectsSyncStateProcessor.getLastSyncState().testObserver().observedValues

        val lastSyncState = syncStates.last()
        lastSyncState!!.assertFailingSyncState()
    }

    @Test
    fun processor_oneWorkerEnqueued_shouldSyncStateBeConnecting() = runBlockingTest {
        startSyncReporterWorker.value = successfulMasterWorkers
        syncWorkersLiveData.value = createWorkInfosHistoryForConnectingSync()

        val syncStates = subjectsSyncStateProcessor.getLastSyncState().testObserver().observedValues

        val lastSyncState = syncStates.last()
        lastSyncState!!.assertConnectingSyncState()
    }


    private fun createStartSyncRerporterWorker(state: WorkInfo.State,
                                               uniqueMasterSyncId: String) =
        createWorkInfo(state,
            workDataOf(SYNC_ID_STARTED to uniqueMasterSyncId),
            listOf(
                "$TAG_SCHEDULED_AT${Date().time}",
                TAG_PEOPLE_DOWN_SYNC_ALL_WORKERS,
                TAG_PEOPLE_SYNC_ALL_WORKERS,
                "$TAG_MASTER_SYNC_ID${uniqueMasterSyncId}") + listOf(tagForType(START_SYNC_REPORTER))
        )

    private fun mockDependencies() {
        every { syncWorkersLiveDataProvider.getStartSyncReportersLiveData() } returns startSyncReporterWorker
        every { syncWorkersLiveDataProvider.getSyncWorkersLiveData(any()) } returns syncWorkersLiveData
        every { subjectsSyncCache.readProgress(any()) } returns 0
    }
}


