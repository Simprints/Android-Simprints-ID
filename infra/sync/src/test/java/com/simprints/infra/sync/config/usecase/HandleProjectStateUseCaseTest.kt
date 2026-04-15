package com.simprints.infra.sync.config.usecase

import com.simprints.infra.config.store.models.ProjectState
import com.simprints.infra.sync.OneTime
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.infra.sync.SyncableCounts
import com.simprints.infra.sync.usecase.ObserveSyncableCountsUseCase
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class HandleProjectStateUseCaseTest {
    @MockK
    private lateinit var observeSyncableCounts: ObserveSyncableCountsUseCase

    @MockK
    private lateinit var syncOrchestrator: SyncOrchestrator

    private lateinit var useCase: HandleProjectStateUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        useCase = HandleProjectStateUseCase(
            observeSyncableCounts = observeSyncableCounts,
            syncOrchestrator = syncOrchestrator,
        )
    }

    @Test
    fun `Fully logs out when project has ended`() = runTest {
        every { observeSyncableCounts.invoke() } returns flowOf(
            SyncableCounts(
                totalRecords = 0,
                recordEventsToDownload = 0,
                isRecordEventsToDownloadLowerBound = false,
                eventsToUpload = 0,
                enrolmentsToUpload = 0,
                samplesToUpload = 0,
            ),
        )

        useCase(ProjectState.PROJECT_ENDED)

        coVerify { syncOrchestrator.execute(eq(OneTime.LogoutCommand(true))) }
    }

    @Test
    fun `Logs out when project has ending and no items to upload`() = runTest {
        every { observeSyncableCounts.invoke() } returns flowOf(
            SyncableCounts(
                totalRecords = 0,
                recordEventsToDownload = 0,
                isRecordEventsToDownloadLowerBound = false,
                eventsToUpload = 0,
                enrolmentsToUpload = 0,
                samplesToUpload = 0,
            ),
        )

        useCase(ProjectState.PROJECT_ENDING)

        coVerify { syncOrchestrator.execute(eq(OneTime.LogoutCommand(true))) }
    }

    @Test
    fun `Does not logs out when project has ending and has items to upload`() = runTest {
        every { observeSyncableCounts.invoke() } returns flowOf(
            SyncableCounts(
                totalRecords = 0,
                recordEventsToDownload = 0,
                isRecordEventsToDownloadLowerBound = false,
                eventsToUpload = 5,
                enrolmentsToUpload = 0,
                samplesToUpload = 0,
            ),
        )

        useCase(ProjectState.PROJECT_ENDING)

        coVerify(exactly = 0) { syncOrchestrator.execute(eq(OneTime.LogoutCommand(true))) }
    }

    @Test
    fun `Does not logs out when project is running`() = runTest {
        every { observeSyncableCounts.invoke() } returns flowOf(
            SyncableCounts(
                totalRecords = 0,
                recordEventsToDownload = 0,
                isRecordEventsToDownloadLowerBound = false,
                eventsToUpload = 0,
                enrolmentsToUpload = 0,
                samplesToUpload = 0,
            ),
        )

        useCase(ProjectState.RUNNING)

        coVerify(exactly = 0) { syncOrchestrator.execute(eq(OneTime.LogoutCommand(true))) }
    }
}
