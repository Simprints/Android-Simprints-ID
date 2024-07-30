package com.simprints.feature.validatepool.screen

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.jraska.livedata.test
import com.simprints.feature.validatepool.usecase.HasRecordsUseCase
import com.simprints.feature.validatepool.usecase.IsModuleIdNotSyncedUseCase
import com.simprints.feature.validatepool.usecase.RunBlockingEventSyncUseCase
import com.simprints.feature.validatepool.usecase.ShouldSuggestSyncUseCase
import com.simprints.infra.enrolment.records.store.domain.models.SubjectQuery
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
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
    private lateinit var runBlockingSync: RunBlockingEventSyncUseCase

    private lateinit var viewModel: ValidateSubjectPoolViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        viewModel = ValidateSubjectPoolViewModel(
            hasRecordsUseCase,
            isModuleIdNotSyncedUseCase,
            shouldSuggestSyncUseCase,
            runBlockingSync,
        )
    }

    @Test
    fun `when subject pool not empty returns Success `() = runTest {
        coEvery { hasRecordsUseCase(any()) } returns true

        viewModel.checkIdentificationPool(SubjectQuery())

        assertThat(viewModel.state.value?.peekContent()).isEqualTo(ValidateSubjectPoolState.Success)
    }

    @Test
    fun `if ID by project when not synced recently returns RequiresSync`() =
        runTest {
            val subjectQuery = SubjectQuery(projectId = "projectId")
            coEvery { hasRecordsUseCase(any()) } returns false
            coEvery { shouldSuggestSyncUseCase() } returns true

            viewModel.checkIdentificationPool(subjectQuery)

            assertThat(viewModel.state.value?.peekContent()).isEqualTo(ValidateSubjectPoolState.RequiresSync)
            coVerify(exactly = 1) { hasRecordsUseCase(any()) }
            coVerify(exactly = 0) { isModuleIdNotSyncedUseCase(any()) }
        }

    @Test
    fun `if ID by project when synced recently returns PoolEmpty`() =
        runTest {
            val subjectQuery = SubjectQuery(projectId = "module1")
            coEvery { hasRecordsUseCase(any()) } returns false
            coEvery { shouldSuggestSyncUseCase() } returns false

            viewModel.checkIdentificationPool(subjectQuery)

            assertThat(viewModel.state.value?.peekContent()).isEqualTo(ValidateSubjectPoolState.PoolEmpty)
        }

    @Test
    fun `if ID by user when subjects enrolled under other attendant ID returns UserMismatch`() =
        runTest {
            val subjectQuery = SubjectQuery(attendantId = "attendantId")
            coEvery { hasRecordsUseCase(any()) } returns true
            coEvery { hasRecordsUseCase(subjectQuery) } returns false

            viewModel.checkIdentificationPool(subjectQuery)

            assertThat(viewModel.state.value?.peekContent()).isEqualTo(ValidateSubjectPoolState.UserMismatch)
            coVerify(exactly = 0) { isModuleIdNotSyncedUseCase(any()) }
            coVerify(exactly = 0) { shouldSuggestSyncUseCase() }
        }

    @Test
    fun `if ID by user when no subjects and should sync returns RequiredSync`() = runTest {
        val subjectQuery = SubjectQuery(attendantId = "attendantId")
        coEvery { hasRecordsUseCase(any()) } returns false
        coEvery { shouldSuggestSyncUseCase() } returns true

        viewModel.checkIdentificationPool(subjectQuery)

        assertThat(viewModel.state.value?.peekContent()).isEqualTo(ValidateSubjectPoolState.RequiresSync)
        coVerify(exactly = 0) { isModuleIdNotSyncedUseCase(any()) }
    }

    @Test
    fun `if ID by user when no subjects and synced returns PoolEmpty`() = runTest {
        val subjectQuery = SubjectQuery(attendantId = "attendantId")
        coEvery { hasRecordsUseCase(any()) } returns false
        coEvery { shouldSuggestSyncUseCase() } returns false

        viewModel.checkIdentificationPool(subjectQuery)

        assertThat(viewModel.state.value?.peekContent()).isEqualTo(ValidateSubjectPoolState.PoolEmpty)
        coVerify(exactly = 0) { isModuleIdNotSyncedUseCase(any()) }
    }

    @Test
    fun `if ID by module when module is not synced returns ModuleMismatch`() =
        runTest {
            val subjectQuery = SubjectQuery(moduleId = "module1")
            coEvery { hasRecordsUseCase(any()) } returns false
            coEvery { isModuleIdNotSyncedUseCase(any()) } returns true

            viewModel.checkIdentificationPool(subjectQuery)

            assertThat(viewModel.state.value?.peekContent()).isEqualTo(ValidateSubjectPoolState.ModuleMismatch)
            coVerify(exactly = 1) { hasRecordsUseCase(any()) }
            coVerify(exactly = 0) { shouldSuggestSyncUseCase() }
        }

    @Test
    fun `if ID by module when module is synced and not synced recently returns RequiresSync`() =
        runTest {
            val subjectQuery = SubjectQuery(moduleId = "module1")
            coEvery { hasRecordsUseCase(any()) } returns false
            coEvery { isModuleIdNotSyncedUseCase(any()) } returns false
            coEvery { shouldSuggestSyncUseCase() } returns true

            viewModel.checkIdentificationPool(subjectQuery)

            assertThat(viewModel.state.value?.peekContent()).isEqualTo(ValidateSubjectPoolState.RequiresSync)
        }

    @Test
    fun `if ID by module when module is synced and synced recently returns PoolEmpty`() =
        runTest {
            val subjectQuery = SubjectQuery(moduleId = "module1")
            coEvery { hasRecordsUseCase(any()) } returns false
            coEvery { isModuleIdNotSyncedUseCase(any()) } returns false
            coEvery { shouldSuggestSyncUseCase() } returns false

            viewModel.checkIdentificationPool(subjectQuery)

            assertThat(viewModel.state.value?.peekContent()).isEqualTo(ValidateSubjectPoolState.PoolEmpty)
        }

    @Test
    fun `runs sync and check`() = runTest {
        val subjectQuery = SubjectQuery(projectId = "projectId")

        coEvery { hasRecordsUseCase(any()) } returnsMany listOf(true)
        coJustRun { runBlockingSync() }

        val result = viewModel.state.test()

        viewModel.syncAndRetry(subjectQuery)

        assertThat(result.valueHistory().map { it.peekContent() }).containsExactly(
            ValidateSubjectPoolState.SyncInProgress,
            ValidateSubjectPoolState.Validating,
            ValidateSubjectPoolState.Success,
        )
        coVerify(exactly = 1) { runBlockingSync() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `checkIdentificationPool not re-validating another time when sync in progress`() = runTest {
        val subjectQuery = SubjectQuery(projectId = "projectId")
        coEvery { hasRecordsUseCase(subjectQuery) } returns true
        coEvery { runBlockingSync() } coAnswers {
            delay(1000)
        }
        viewModel.syncAndRetry(subjectQuery)

        viewModel.checkIdentificationPool(subjectQuery)

        advanceUntilIdle()
        coVerify(exactly = 1) { hasRecordsUseCase(any()) }
    }
}
