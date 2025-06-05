package com.simprints.infra.enrolment.records.repository.local.migration

import androidx.work.BackoffPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
class RealmToRoomMigrationSchedulerTest {
    @MockK
    lateinit var mockRealmToRoomMigrationFlagsStore: RealmToRoomMigrationFlagsStore

    @MockK
    lateinit var mockWorkManager: WorkManager

    @InjectMockKs
    private lateinit var scheduler: RealmToRoomMigrationScheduler

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        coEvery { mockWorkManager.cancelUniqueWork(any()) } returns mockk()
        coEvery { mockWorkManager.enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>()) } returns mockk()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `scheduleMigrationWorkerIfNeeded should cancel work when migration globally disabled`() = runTest {
        // Given
        coEvery { mockRealmToRoomMigrationFlagsStore.getCurrentStatus() } returns MigrationStatus.NOT_STARTED
        coEvery { mockRealmToRoomMigrationFlagsStore.isMigrationGloballyEnabled() } returns false
        coEvery { mockRealmToRoomMigrationFlagsStore.canRetry() } returns true // Does not matter for this case

        // When
        scheduler.scheduleMigrationWorkerIfNeeded()

        // Then
        coVerify(exactly = 1) {
            mockRealmToRoomMigrationFlagsStore.getCurrentStatus()
            mockRealmToRoomMigrationFlagsStore.isMigrationGloballyEnabled()

            mockWorkManager.cancelUniqueWork(RealmToRoomMigrationWorker.Companion.WORK_NAME)
        }
        coVerify(exactly = 0) { mockWorkManager.enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>()) }
    }

    @Test
    fun `scheduleMigrationWorkerIfNeeded should cancel work when migration cannot be retried`() = runTest {
        // Given
        coEvery { mockRealmToRoomMigrationFlagsStore.getCurrentStatus() } returns MigrationStatus.FAILED
        coEvery { mockRealmToRoomMigrationFlagsStore.isMigrationGloballyEnabled() } returns true
        coEvery { mockRealmToRoomMigrationFlagsStore.canRetry() } returns false

        // When
        scheduler.scheduleMigrationWorkerIfNeeded()

        // Then
        coVerify(exactly = 1) {
            mockRealmToRoomMigrationFlagsStore.getCurrentStatus()
            mockRealmToRoomMigrationFlagsStore.isMigrationGloballyEnabled()
            mockRealmToRoomMigrationFlagsStore.canRetry()

            mockWorkManager.cancelUniqueWork(RealmToRoomMigrationWorker.Companion.WORK_NAME)
        }
        coVerify(exactly = 0) { mockWorkManager.enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>()) }
    }

    @Test
    fun `scheduleMigrationWorkerIfNeeded should cancel work when migration status is COMPLETED`() = runTest {
        // Given
        coEvery { mockRealmToRoomMigrationFlagsStore.getCurrentStatus() } returns MigrationStatus.COMPLETED
        coEvery { mockRealmToRoomMigrationFlagsStore.isMigrationGloballyEnabled() } returns true
        coEvery { mockRealmToRoomMigrationFlagsStore.canRetry() } returns true

        // When
        scheduler.scheduleMigrationWorkerIfNeeded()

        // Then
        coVerify(exactly = 1) {
            mockRealmToRoomMigrationFlagsStore.getCurrentStatus()

            mockWorkManager.cancelUniqueWork(RealmToRoomMigrationWorker.Companion.WORK_NAME)
        }
        coVerify(exactly = 0) { mockWorkManager.enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>()) }
    }

    @Test
    fun `scheduleMigrationWorkerIfNeeded should enqueue work when status is FAILED and can retry`() = runTest {
        // Given
        coEvery { mockRealmToRoomMigrationFlagsStore.getCurrentStatus() } returns MigrationStatus.FAILED
        coEvery { mockRealmToRoomMigrationFlagsStore.isMigrationGloballyEnabled() } returns true
        coEvery { mockRealmToRoomMigrationFlagsStore.canRetry() } returns true

        val workRequestSlot = slot<OneTimeWorkRequest>()

        // When
        scheduler.scheduleMigrationWorkerIfNeeded()

        // Then
        coVerify(exactly = 1) {
            mockRealmToRoomMigrationFlagsStore.getCurrentStatus()
            mockRealmToRoomMigrationFlagsStore.isMigrationGloballyEnabled()
            mockRealmToRoomMigrationFlagsStore.canRetry()

            mockWorkManager.enqueueUniqueWork(
                RealmToRoomMigrationWorker.Companion.WORK_NAME,
                ExistingWorkPolicy.KEEP,
                capture(workRequestSlot),
            )
        }
        coVerify(exactly = 0) { mockWorkManager.cancelUniqueWork(any()) }

        val capturedRequest = workRequestSlot.captured
        assertThat(capturedRequest.workSpec.workerClassName).isEqualTo(RealmToRoomMigrationWorker::class.java.name)
        assertThat(capturedRequest.workSpec.constraints.requiredNetworkType).isEqualTo(NetworkType.NOT_REQUIRED)
        assertThat(capturedRequest.workSpec.constraints.requiresCharging()).isFalse()
        assertThat(capturedRequest.workSpec.constraints.requiresStorageNotLow()).isTrue()
        assertThat(capturedRequest.workSpec.backoffPolicy).isEqualTo(BackoffPolicy.EXPONENTIAL)
        assertThat(capturedRequest.workSpec.backoffDelayDuration).isEqualTo(TimeUnit.SECONDS.toMillis(60L))
        assertThat(capturedRequest.tags).contains(RealmToRoomMigrationWorker.Companion.WORK_NAME)
    }

    @Test
    fun `scheduleMigrationWorkerIfNeeded should enqueue work when status is NOT_STARTED`() = runTest {
        // Given
        coEvery { mockRealmToRoomMigrationFlagsStore.getCurrentStatus() } returns MigrationStatus.NOT_STARTED
        coEvery { mockRealmToRoomMigrationFlagsStore.isMigrationGloballyEnabled() } returns true
        coEvery { mockRealmToRoomMigrationFlagsStore.canRetry() } returns true
        val workRequestSlot = slot<OneTimeWorkRequest>()

        // When
        scheduler.scheduleMigrationWorkerIfNeeded()

        // Then
        coVerify(exactly = 1) {
            mockRealmToRoomMigrationFlagsStore.getCurrentStatus()
            mockRealmToRoomMigrationFlagsStore.isMigrationGloballyEnabled()
            mockRealmToRoomMigrationFlagsStore.canRetry()

            mockWorkManager.enqueueUniqueWork(
                RealmToRoomMigrationWorker.Companion.WORK_NAME,
                ExistingWorkPolicy.KEEP,
                capture(workRequestSlot),
            )
        }
        coVerify(exactly = 0) { mockWorkManager.cancelUniqueWork(any()) }
        val capturedRequest = workRequestSlot.captured
        assertThat(capturedRequest.workSpec.workerClassName).isEqualTo(RealmToRoomMigrationWorker::class.java.name)
    }

    @Test
    fun `scheduleMigrationWorkerIfNeeded should enqueue work when status is IN_PROGRESS`() = runTest {
        // Given
        coEvery { mockRealmToRoomMigrationFlagsStore.getCurrentStatus() } returns MigrationStatus.IN_PROGRESS
        coEvery { mockRealmToRoomMigrationFlagsStore.isMigrationGloballyEnabled() } returns true
        coEvery { mockRealmToRoomMigrationFlagsStore.canRetry() } returns true
        val workRequestSlot = slot<OneTimeWorkRequest>()

        // When
        scheduler.scheduleMigrationWorkerIfNeeded()

        // Then
        coVerify(exactly = 1) {
            mockRealmToRoomMigrationFlagsStore.getCurrentStatus()
            mockRealmToRoomMigrationFlagsStore.isMigrationGloballyEnabled()
            mockRealmToRoomMigrationFlagsStore.canRetry()

            mockWorkManager.enqueueUniqueWork(
                RealmToRoomMigrationWorker.Companion.WORK_NAME,
                ExistingWorkPolicy.KEEP,
                capture(workRequestSlot),
            )
        }
        coVerify(exactly = 0) { mockWorkManager.cancelUniqueWork(any()) }
        val capturedRequest = workRequestSlot.captured
        assertThat(capturedRequest.workSpec.workerClassName).isEqualTo(RealmToRoomMigrationWorker::class.java.name)
    }
}
