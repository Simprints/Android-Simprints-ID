package com.simprints.feature.validatepool.screen

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.*
import com.jraska.livedata.test
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.tools.time.TimeHelper
import com.simprints.feature.validatepool.usecase.HasRecordsUseCase
import com.simprints.feature.validatepool.usecase.IsModuleIdNotSyncedUseCase
import com.simprints.feature.validatepool.usecase.ShouldSuggestSyncUseCase
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordQuery
import com.simprints.infra.eventsync.status.models.EventSyncState
import com.simprints.infra.eventsync.status.models.EventSyncWorkerState
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType
import com.simprints.infra.sync.ImageSyncStatus
import com.simprints.infra.sync.OneTime
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.infra.sync.SyncStatus
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ValidateSubjectPoolViewModelTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var hasRecordsUseCase: HasRecordsUseCase

    @MockK
    private lateinit var isModuleIdNotSyncedUseCase: IsModuleIdNotSyncedUseCase

    @MockK
    private lateinit var shouldSuggestSyncUseCase: ShouldSuggestSyncUseCase

    @MockK
    private lateinit var syncOrchestrator: SyncOrchestrator

    @MockK
    private lateinit var timeHelper: TimeHelper

    private lateinit var syncStatusFlow: MutableStateFlow<SyncStatus>

    private lateinit var viewModel: ValidateSubjectPoolViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        syncStatusFlow = MutableStateFlow(createSyncStatus(isCompleted = false))
        every { syncOrchestrator.observeSyncState() } returns syncStatusFlow
        every { timeHelper.readableBetweenNowAndTime(any()) } returns "5 minutes ago"

        viewModel = ValidateSubjectPoolViewModel(
            hasRecordsUseCase,
            isModuleIdNotSyncedUseCase,
            shouldSuggestSyncUseCase,
            syncOrchestrator,
            timeHelper,
        )
    }

    @Test
    fun `when subject pool not empty returns Success `() = runTest {
        coEvery { hasRecordsUseCase(any()) } returns true

        viewModel.checkIdentificationPool(EnrolmentRecordQuery())

        assertThat(viewModel.state.value?.peekContent()).isEqualTo(ValidateSubjectPoolState.Success)
    }

    @Test
    fun `if ID by project when not synced recently returns RequiresSync`() = runTest {
        val enrolmentRecordQuery = EnrolmentRecordQuery(projectId = "projectId")
        coEvery { hasRecordsUseCase(any()) } returns false
        coEvery { shouldSuggestSyncUseCase() } returns true

        viewModel.checkIdentificationPool(enrolmentRecordQuery)

        assertThat(viewModel.state.value?.peekContent()).isEqualTo(ValidateSubjectPoolState.RequiresSync)
        coVerify(exactly = 1) { hasRecordsUseCase(any()) }
        coVerify(exactly = 0) { isModuleIdNotSyncedUseCase(any()) }
    }

    @Test
    fun `if ID by project when synced recently returns PoolEmpty`() = runTest {
        val enrolmentRecordQuery = EnrolmentRecordQuery(projectId = "module1")
        coEvery { hasRecordsUseCase(any()) } returns false
        coEvery { shouldSuggestSyncUseCase() } returns false

        viewModel.checkIdentificationPool(enrolmentRecordQuery)

        assertThat(viewModel.state.value?.peekContent()).isEqualTo(ValidateSubjectPoolState.PoolEmpty)
    }

    @Test
    fun `if ID by user when subjects enrolled under other attendant ID returns UserMismatch`() = runTest {
        val enrolmentRecordQuery = EnrolmentRecordQuery(attendantId = "attendantId".asTokenizableEncrypted())
        coEvery { hasRecordsUseCase(any()) } returns true
        coEvery { hasRecordsUseCase(enrolmentRecordQuery) } returns false

        viewModel.checkIdentificationPool(enrolmentRecordQuery)

        assertThat(viewModel.state.value?.peekContent()).isEqualTo(ValidateSubjectPoolState.UserMismatch)
        coVerify(exactly = 0) { isModuleIdNotSyncedUseCase(any()) }
        coVerify(exactly = 0) { shouldSuggestSyncUseCase() }
    }

    @Test
    fun `if ID by user when no subjects and should sync returns RequiredSync`() = runTest {
        val enrolmentRecordQuery = EnrolmentRecordQuery(attendantId = "attendantId".asTokenizableEncrypted())
        coEvery { hasRecordsUseCase(any()) } returns false
        coEvery { shouldSuggestSyncUseCase() } returns true

        viewModel.checkIdentificationPool(enrolmentRecordQuery)

        assertThat(viewModel.state.value?.peekContent()).isEqualTo(ValidateSubjectPoolState.RequiresSync)
        coVerify(exactly = 0) { isModuleIdNotSyncedUseCase(any()) }
    }

    @Test
    fun `if ID by user when no subjects and synced returns PoolEmpty`() = runTest {
        val enrolmentRecordQuery = EnrolmentRecordQuery(attendantId = "attendantId".asTokenizableEncrypted())
        coEvery { hasRecordsUseCase(any()) } returns false
        coEvery { shouldSuggestSyncUseCase() } returns false

        viewModel.checkIdentificationPool(enrolmentRecordQuery)

        assertThat(viewModel.state.value?.peekContent()).isEqualTo(ValidateSubjectPoolState.PoolEmpty)
        coVerify(exactly = 0) { isModuleIdNotSyncedUseCase(any()) }
    }

    @Test
    fun `if ID by module when module is not synced returns ModuleMismatch`() = runTest {
        val enrolmentRecordQuery = EnrolmentRecordQuery(moduleId = "module1".asTokenizableEncrypted())
        coEvery { hasRecordsUseCase(any()) } returns false
        coEvery { isModuleIdNotSyncedUseCase(any()) } returns true

        viewModel.checkIdentificationPool(enrolmentRecordQuery)

        assertThat(viewModel.state.value?.peekContent()).isEqualTo(ValidateSubjectPoolState.ModuleMismatch)
        coVerify(exactly = 1) { hasRecordsUseCase(any()) }
        coVerify(exactly = 0) { shouldSuggestSyncUseCase() }
    }

    @Test
    fun `if ID by module when module is synced and not synced recently returns RequiresSync`() = runTest {
        val enrolmentRecordQuery = EnrolmentRecordQuery(moduleId = "module1".asTokenizableEncrypted())
        coEvery { hasRecordsUseCase(any()) } returns false
        coEvery { isModuleIdNotSyncedUseCase(any()) } returns false
        coEvery { shouldSuggestSyncUseCase() } returns true

        viewModel.checkIdentificationPool(enrolmentRecordQuery)

        assertThat(viewModel.state.value?.peekContent()).isEqualTo(ValidateSubjectPoolState.RequiresSync)
    }

    @Test
    fun `if ID by module when module is synced and synced recently returns PoolEmpty`() = runTest {
        val enrolmentRecordQuery = EnrolmentRecordQuery(moduleId = "module1".asTokenizableEncrypted())
        coEvery { hasRecordsUseCase(any()) } returns false
        coEvery { isModuleIdNotSyncedUseCase(any()) } returns false
        coEvery { shouldSuggestSyncUseCase() } returns false

        viewModel.checkIdentificationPool(enrolmentRecordQuery)

        assertThat(viewModel.state.value?.peekContent()).isEqualTo(ValidateSubjectPoolState.PoolEmpty)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `runs sync and check`() = runTest {
        val enrolmentRecordQuery = EnrolmentRecordQuery(projectId = "projectId")
        val job = Job()

        coEvery { hasRecordsUseCase(any()) } returnsMany listOf(true)
        every { syncOrchestrator.execute(any<OneTime>()) } returns job

        val result = viewModel.state.test()

        viewModel.startSync(enrolmentRecordQuery)

        syncStatusFlow.value = createSyncStatus(isCompleted = true)
        job.complete()
        advanceUntilIdle()

        assertThat(result.valueHistory().map { it.peekContent() }).containsExactly(
            ValidateSubjectPoolState.SyncInProgress,
            ValidateSubjectPoolState.Validating,
            ValidateSubjectPoolState.Success,
        )
        coVerify(exactly = 1) { syncOrchestrator.execute(any<OneTime>()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `checkIdentificationPool not re-validating when sync in progress`() = runTest {
        val enrolmentRecordQuery = EnrolmentRecordQuery(projectId = "projectId")
        val job = Job()
        coEvery { hasRecordsUseCase(enrolmentRecordQuery) } returns true
        every { syncOrchestrator.execute(any<OneTime>()) } returns job
        viewModel.startSync(enrolmentRecordQuery)

        viewModel.checkIdentificationPool(enrolmentRecordQuery)

        job.complete()
        advanceUntilIdle()
        coVerify(exactly = 1) { syncOrchestrator.execute(any<OneTime>()) }
        coVerify(exactly = 0) { hasRecordsUseCase(any()) }
    }

    private fun createSyncStatus(isCompleted: Boolean): SyncStatus {
        val reporterStates = if (isCompleted) {
            listOf(
                EventSyncState.SyncWorkerInfo(
                    type = EventSyncWorkerType.END_SYNC_REPORTER,
                    state = EventSyncWorkerState.Succeeded,
                ),
            )
        } else {
            emptyList()
        }

        return SyncStatus(
            eventSyncState = EventSyncState(
                syncId = "",
                progress = null,
                total = null,
                upSyncWorkersInfo = emptyList(),
                downSyncWorkersInfo = emptyList(),
                reporterStates = reporterStates,
                lastSyncTime = null,
            ),
            imageSyncStatus = ImageSyncStatus(
                isSyncing = false,
                progress = null,
                lastUpdateTimeMillis = null,
            ),
        )
    }
}
