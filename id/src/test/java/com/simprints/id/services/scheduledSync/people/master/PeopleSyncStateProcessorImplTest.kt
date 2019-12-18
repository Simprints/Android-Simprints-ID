package com.simprints.id.services.scheduledSync.people.master

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkInfo
import androidx.work.WorkInfo.State.FAILED
import androidx.work.WorkInfo.State.SUCCEEDED
import androidx.work.workDataOf
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker.Companion.OUTPUT_LAST_SYNC_ID
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncWorkerType.Companion.tagForType
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncWorkerType.DOWNLOADER
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

@RunWith(AndroidJUnit4::class)
class PeopleSyncStateProcessorImplTest {

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
        listOf(createSyncMasterWorker(SUCCEEDED, UNIQUE_SYNC_ID), createSyncMasterWorker(SUCCEEDED, "${UNIQUE_SYNC_ID}_older"))

    private val failedMasterWorkers: List<WorkInfo> =
        listOf(createWorkInfo(FAILED))


    private val ctx: Context = ApplicationProvider.getApplicationContext()

    private var masterWorkersLiveData = MutableLiveData<List<WorkInfo>>()
    private var syncWorkersLiveData = MutableLiveData<List<WorkInfo>>()

    lateinit var peopleSyncStateProcessor: PeopleSyncStateProcessor
    @RelaxedMockK lateinit var personRepository: PersonRepository
    @RelaxedMockK lateinit var syncWorkersLiveDataProvider: SyncWorkersLiveDataProvider

    @Before
    fun setUp() {
        UnitTestConfig(this).setupWorkManager()
        MockKAnnotations.init(this)
        peopleSyncStateProcessor = PeopleSyncStateProcessorImpl(ctx, personRepository, syncWorkersLiveDataProvider)
        mockWorkersInfoLiveData()
    }

    @Test
    fun processor_masterWorkerCompletes_shouldExtractTheUniqueSyncId() = runBlockingTest {
        masterWorkersLiveData.value = successfulMasterWorkers

        peopleSyncStateProcessor.getLastSyncState().testObserver()

        verify { syncWorkersLiveDataProvider.getSyncWorkersLiveData(UNIQUE_SYNC_ID) }
    }

    @Test
    fun processor_masterWorkerFails_shouldNotExtractTheUniqueSyncId() = runBlockingTest {
        masterWorkersLiveData.value = failedMasterWorkers

        peopleSyncStateProcessor.getLastSyncState().testObserver()

        verify(exactly = 0) { syncWorkersLiveDataProvider.getSyncWorkersLiveData(UNIQUE_SYNC_ID) }
    }

    @Test
    fun processor_allWorkersSucceed_shouldSyncStateBeSuccess() = runBlockingTest {
        masterWorkersLiveData.value = successfulMasterWorkers
        syncWorkersLiveData.value = createWorkInfosHistoryForSuccessfulSync()

        val syncStates = peopleSyncStateProcessor.getLastSyncState().testObserver().observedValues

        val lastSyncState = syncStates.last()
        lastSyncState!!.assertSuccessfulSyncState()
    }

    @Test
    fun processor_oneWorkerStillRunning_shouldSyncStateBeRunning() = runBlockingTest {
        masterWorkersLiveData.value = successfulMasterWorkers
        syncWorkersLiveData.value = createWorkInfosHistoryForRunningSync()

        val syncStates = peopleSyncStateProcessor.getLastSyncState().testObserver().observedValues

        val lastSyncState = syncStates.last()
        lastSyncState!!.assertRunningSyncState()
    }

    @Test
    fun processor_oneWorkerFailed_shouldSyncStateBeFail() = runBlockingTest {
        masterWorkersLiveData.value = successfulMasterWorkers
        syncWorkersLiveData.value = createWorkInfosHistoryForFailingSync()

        val syncStates = peopleSyncStateProcessor.getLastSyncState().testObserver().observedValues

        val lastSyncState = syncStates.last()
        lastSyncState!!.assertFailingSyncState()
    }

    @Test
    fun processor_oneWorkerEnqueued_shouldSyncStateBeConnecting() = runBlockingTest {
        masterWorkersLiveData.value = successfulMasterWorkers
        syncWorkersLiveData.value = createWorkInfosHistoryForConnectingSync()

        val syncStates = peopleSyncStateProcessor.getLastSyncState().testObserver().observedValues

        val lastSyncState = syncStates.last()
        lastSyncState!!.assertConnectingSyncState()
    }

    @Test
    fun processor_allWorkersSucceedWithMultiRetries_shouldSyncStateBeSuccess() = runBlockingTest {
        masterWorkersLiveData.value = successfulMasterWorkers
        syncWorkersLiveData.value = createWorkInfosHistoryForSuccessfulSyncInMultiAttempts()

        val syncStates = peopleSyncStateProcessor.getLastSyncState().testObserver().observedValues

        val lastSyncState = syncStates.last()
        lastSyncState!!.assertSuccessfulSyncState()
    }


    private fun createSyncMasterWorker(state: WorkInfo.State,
                                       uniqueMasterSyncId: String) =
        createWorkInfo(state,
            workDataOf(OUTPUT_LAST_SYNC_ID to uniqueMasterSyncId),
            createCommonDownSyncTags(uniqueMasterSyncId, uniqueMasterSyncId) + listOf(tagForType(DOWNLOADER))
        )

    private fun mockWorkersInfoLiveData() {
        every { syncWorkersLiveDataProvider.getMasterWorkersLiveData() } returns masterWorkersLiveData
        every { syncWorkersLiveDataProvider.getSyncWorkersLiveData(any()) } returns syncWorkersLiveData
    }
}


