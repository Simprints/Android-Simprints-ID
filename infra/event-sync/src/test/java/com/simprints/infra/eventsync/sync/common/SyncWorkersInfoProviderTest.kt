package com.simprints.infra.eventsync.sync.common

import android.content.Context
import androidx.test.ext.junit.runners.*
import androidx.work.WorkManager
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SyncWorkersInfoProviderTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()
    private lateinit var workManager: WorkManager

    private lateinit var syncWorkersInfoProvider: SyncWorkersInfoProvider

    @Before
    fun setup() {
        workManager = mockk()
        every { workManager.getWorkInfosByTagFlow(any()) } returns flowOf(emptyList())

        mockkObject(WorkManager.Companion)
        every { WorkManager.getInstance(any()) } returns workManager

        syncWorkersInfoProvider = SyncWorkersInfoProvider(mockk<Context>())
    }

    @Test
    fun `getStartSyncReporters requests workers with correct tag`() = runTest {
        syncWorkersInfoProvider.getStartSyncReporters().first()

        verify { workManager.getWorkInfosByTagFlow(eq("TAG_PEOPLE_SYNC_WORKER_TYPE_START_SYNC_REPORTER")) }
    }

    @Test
    fun `getSyncWorkerInfos requests workers with correct tag`() = runTest {
        syncWorkersInfoProvider.getSyncWorkerInfos("uniqueTag").first()

        verify { workManager.getWorkInfosByTagFlow(eq("TAG_MASTER_SYNC_ID_uniqueTag")) }
    }
}
