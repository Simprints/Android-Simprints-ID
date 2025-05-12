package com.simprints.infra.enrolment.records.repository.local.migration

import android.content.Context
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.enrolment.records.repository.domain.models.Subject
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectAction
import com.simprints.infra.enrolment.records.repository.local.RealmEnrolmentRecordLocalDataSource
import com.simprints.infra.enrolment.records.repository.local.RoomEnrolmentRecordLocalDataSource
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class RealmToRoomMigrationWorkerTest {
    @MockK(relaxed = true)
    private lateinit var appContext: Context

    @MockK(relaxed = true)
    private lateinit var workerParams: WorkerParameters

    @MockK(relaxed = true)
    private lateinit var realmDataSource: RealmEnrolmentRecordLocalDataSource

    @MockK(relaxed = true)
    private lateinit var roomDataSource: RoomEnrolmentRecordLocalDataSource

    @MockK(relaxed = true)
    private lateinit var realmToRoomMigrationFlagsStore: RealmToRoomMigrationFlagsStore

    @MockK(relaxed = true)
    private lateinit var configRepo: ConfigRepository

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcher: CoroutineDispatcher = UnconfinedTestDispatcher()

    @InjectMockKs
    private lateinit var worker: RealmToRoomMigrationWorker

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `doWork should retry when down sync is in progress`() = runTest {
        // Given
        coEvery { realmToRoomMigrationFlagsStore.isDownSyncInProgress() } returns true

        // When
        val result = worker.doWork()

        // Then
        coVerify { realmToRoomMigrationFlagsStore.updateStatus(MigrationStatus.NOT_STARTED) }
        assertThat(result).isEqualTo(Result.retry())
    }

    @Test
    fun `doWork should succeed and skip migration when realm is empty`() = runTest {
        // Given
        coEvery { realmToRoomMigrationFlagsStore.isDownSyncInProgress() } returns false
        coEvery { realmDataSource.count(any()) } returns 0

        // When
        val result = worker.doWork()

        // Then
        coVerify { realmToRoomMigrationFlagsStore.updateStatus(MigrationStatus.IN_PROGRESS) }
        coVerify(exactly = 0) { roomDataSource.deleteAll() }
        coVerify(exactly = 0) { realmDataSource.loadAllSubjectsInBatches(any()) }
        coVerify { realmToRoomMigrationFlagsStore.updateStatus(MigrationStatus.COMPLETED) }
        coVerify { realmToRoomMigrationFlagsStore.resetRetryCount() }
        assertThat(result).isEqualTo(Result.success())
    }

    @Test
    fun `doWork should succeed and migrate data when realm has data`() = runTest {
        val mockSubjectsBatch1 = listOf<Subject>(mockk(), mockk())
        val mockSubjectsBatch2 = listOf<Subject>(mockk())

        // Given
        coEvery { realmToRoomMigrationFlagsStore.isDownSyncInProgress() } returns false
        coEvery { realmDataSource.count(any()) } returns 3 // Total subjects
        coEvery {
            realmDataSource.loadAllSubjectsInBatches(any())
        } returns flowOf(mockSubjectsBatch1, mockSubjectsBatch2)
        coEvery { roomDataSource.performActions(any(), any()) } just Runs

        // When
        val result = worker.doWork()

        // Then
        coVerify { realmToRoomMigrationFlagsStore.updateStatus(MigrationStatus.IN_PROGRESS) }
        coVerify { roomDataSource.deleteAll() }
        coVerify { realmDataSource.loadAllSubjectsInBatches(RealmToRoomMigrationWorker.BATCH_SIZE) }
        coVerify(exactly = 1) {
            roomDataSource.performActions(
                match { actions ->
                    actions.all { it is SubjectAction.Creation } && actions.size == mockSubjectsBatch1.size
                },
                any(),
            )
        }
        coVerify(exactly = 1) {
            roomDataSource.performActions(
                match { actions ->
                    actions.all { it is SubjectAction.Creation } && actions.size == mockSubjectsBatch2.size
                },
                any(),
            )
        }
        coVerify { realmToRoomMigrationFlagsStore.updateStatus(MigrationStatus.COMPLETED) }
        coVerify { realmToRoomMigrationFlagsStore.resetRetryCount() }
        assertThat(result).isEqualTo(Result.success())
    }

    @Test
    fun `doWork should fail and increment retry count when migration fails in count`() = runTest {
        // Given
        coEvery { realmToRoomMigrationFlagsStore.isDownSyncInProgress() } returns false
        coEvery { realmDataSource.count(any()) } throws RuntimeException("Migration error")

        // When
        val result = worker.doWork()

        // Then
        coVerify { realmToRoomMigrationFlagsStore.updateStatus(MigrationStatus.IN_PROGRESS) }
        coVerify { realmToRoomMigrationFlagsStore.incrementRetryCount() }
        coVerify { realmToRoomMigrationFlagsStore.updateStatus(MigrationStatus.FAILED) }
        coVerify { roomDataSource.deleteAll() }
        assertThat(result).isEqualTo(Result.failure())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `doWork should fail and increment retry count when migration fails in processRecords`() = runTest {
        val mockSubjectsBatch1 = listOf<Subject>(mockk(), mockk())
        val mockSubjectsBatch2 = listOf<Subject>(mockk())

        // Given
        coEvery { realmToRoomMigrationFlagsStore.isDownSyncInProgress() } returns false
        coEvery { realmDataSource.count(any()) } returns 3 // Total subjects
        coEvery { realmDataSource.loadAllSubjectsInBatches(any()) } returns flowOf(mockSubjectsBatch1, mockSubjectsBatch2)
        coEvery { roomDataSource.performActions(any(), any()) } throws RuntimeException("insertion error")

        // When
        val result = worker.doWork()

        // Then
        coVerify { realmToRoomMigrationFlagsStore.updateStatus(MigrationStatus.IN_PROGRESS) }
        coVerify { realmToRoomMigrationFlagsStore.incrementRetryCount() }
        coVerify { realmToRoomMigrationFlagsStore.updateStatus(MigrationStatus.FAILED) }
        coVerify { roomDataSource.deleteAll() }
        assertThat(result).isEqualTo(Result.failure())
    }
}
