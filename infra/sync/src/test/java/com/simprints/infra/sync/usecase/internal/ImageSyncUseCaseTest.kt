package com.simprints.infra.sync.usecase.internal

import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.sync.ImageSyncTimestampProvider
import com.simprints.infra.sync.SyncConstants
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.UUID

class ImageSyncUseCaseTest {
    @MockK
    private lateinit var workManager: WorkManager

    @MockK
    private lateinit var imageSyncTimestampProvider: ImageSyncTimestampProvider

    private lateinit var useCase: ImageSyncUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        useCase = ImageSyncUseCase(
            workManager = workManager,
            imageSyncTimestampProvider = imageSyncTimestampProvider,
        )
    }

    @Test
    fun `image sync status returns syncing when worker is running`() = runTest {
        val workInfoFlow = flowOf(createWorkInfos(WorkInfo.State.RUNNING))
        every { workManager.getWorkInfosFlow(any()) } returns workInfoFlow
        every { imageSyncTimestampProvider.getMillisSinceLastImageSync() } returns 30_000L
        every { imageSyncTimestampProvider.getLastImageSyncTimestamp() } returns 1_234_567_890L

        val status = useCase().first()

        assertThat(status.isSyncing).isTrue()
        assertThat(status.lastUpdateTimeMillis).isEqualTo(1_234_567_890L)
    }

    @Test
    fun `image sync status returns not syncing when worker is cancelled`() = runTest {
        val workInfoFlow = flowOf(createWorkInfos(WorkInfo.State.CANCELLED))
        every { workManager.getWorkInfosFlow(any()) } returns workInfoFlow
        every { imageSyncTimestampProvider.getMillisSinceLastImageSync() } returns 120_000L
        every { imageSyncTimestampProvider.getLastImageSyncTimestamp() } returns 1_234_567_890L

        val status = useCase().first()

        assertThat(status.isSyncing).isFalse()
        assertThat(status.lastUpdateTimeMillis).isEqualTo(1_234_567_890L)
    }

    @Test
    fun `image sync status returns null timestamp when no sync has occurred`() = runTest {
        val workInfoFlow = flowOf(createWorkInfos(WorkInfo.State.CANCELLED))
        every { workManager.getWorkInfosFlow(any()) } returns workInfoFlow
        every { imageSyncTimestampProvider.getMillisSinceLastImageSync() } returns null
        every { imageSyncTimestampProvider.getLastImageSyncTimestamp() } returns null

        val status = useCase().first()

        assertThat(status.isSyncing).isFalse()
        assertThat(status.lastUpdateTimeMillis).isNull()
    }

    @Test
    fun `image sync status includes progress when available`() = runTest {
        val workInfo1 = createWorkInfosWithProgress(WorkInfo.State.RUNNING, current = 5, max = 10)
        val workInfo2 = createWorkInfosWithProgress(WorkInfo.State.RUNNING)
        val workInfoFlow = flowOf(workInfo1, workInfo2)
        every { workManager.getWorkInfosFlow(any()) } returns workInfoFlow
        every { imageSyncTimestampProvider.getMillisSinceLastImageSync() } returns 0L

        val status1 = useCase().first()
        assertThat(status1.progress).isEqualTo(5 to 10)

        val status2 = useCase().drop(1).first()
        assertThat(status2.progress).isEqualTo(null)
    }

    @Test
    fun `image sync status returns syncing momentarily when worker succeeds quickly`() = runTest {
        val workInfoFlow = flowOf(createWorkInfos(WorkInfo.State.SUCCEEDED))
        every { workManager.getWorkInfosFlow(any()) } returns workInfoFlow
        every { imageSyncTimestampProvider.getMillisSinceLastImageSync() } returns 0L

        val status1 = useCase().first()
        assertThat(status1.isSyncing).isTrue()

        val status2 = useCase().drop(1).first()
        assertThat(status2.isSyncing).isFalse()
    }

    private fun createWorkInfos(state: WorkInfo.State): List<WorkInfo> = listOf(
        WorkInfo(UUID.randomUUID(), state, emptySet()),
    )

    private fun createWorkInfosWithProgress(
        state: WorkInfo.State,
        current: Int? = null,
        max: Int? = null,
    ): List<WorkInfo> {
        val progressData = Data
            .Builder()
            .apply {
                current?.let { putInt(SyncConstants.PROGRESS_CURRENT, it) }
                max?.let { putInt(SyncConstants.PROGRESS_MAX, it) }
            }.build()

        val workInfo = mockk<WorkInfo> {
            every { this@mockk.state } returns state
            every { progress } returns progressData
        }
        return listOf(workInfo)
    }
}
