package com.simprints.feature.troubleshooting.workers

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.common.truth.Truth.assertThat
import com.jraska.livedata.test
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.UUID

class WorkerLogViewModelTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var workManager: WorkManager

    @MockK
    private lateinit var dateFormatter: SimpleDateFormat

    private lateinit var viewModel: WorkerLogViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        every { dateFormatter.format(any()) } returns "date"

        viewModel = WorkerLogViewModel(
            workManager = workManager,
            dateFormatter = dateFormatter,
        )
    }

    @Test
    fun `sets list of scopes on request`() = runTest {
        coEvery { workManager.getWorkInfosFlow(any()) } returns flowOf(
            listOf(
                mockk<WorkInfo>(relaxed = true) {
                    every { id } returns UUID.fromString("c92d4da1-dc9a-4e25-9fcd-a9aca78a4cf3")
                    every { tags } returns setOf("com.simprints.Worker")
                    every { state } returns WorkInfo.State.SUCCEEDED
                },
            ),
        )

        val workers = viewModel.workers.test()
        viewModel.collectWorkerData()

        assertThat(workers.value()).isNotEmpty()
        assertThat(workers.value().first().title).isEqualTo("Worker")
        assertThat(workers.value().first().body).contains("Output")
    }

    @Test
    fun `sets list of scopes placeholder if no scopes`() = runTest {
        coEvery { workManager.getWorkInfosFlow(any()) } returns flowOf(emptyList())

        val workers = viewModel.workers.test()
        viewModel.collectWorkerData()

        assertThat(workers.value()).isNotEmpty()
    }

    @Test
    fun `sets id to UUID if no tag`() = runTest {
        coEvery { workManager.getWorkInfosFlow(any()) } returns flowOf(
            listOf(
                mockk<WorkInfo>(relaxed = true) {
                    every { id } returns UUID.fromString("c92d4da1-dc9a-4e25-9fcd-a9aca78a4cf3")
                    every { tags } returns setOf("Worker")
                },
            ),
        )

        val workers = viewModel.workers.test()
        viewModel.collectWorkerData()

        assertThat(workers.value()).isNotEmpty()
        assertThat(workers.value().first().title).isEqualTo("c92d4da1-dc9a-4e25-9fcd-a9aca78a4cf3")
    }

    @Test
    fun `sets takes next scheduled time if worked is enqueued`() = runTest {
        coEvery { workManager.getWorkInfosFlow(any()) } returns flowOf(
            listOf(
                mockk<WorkInfo>(relaxed = true) {
                    every { state } returns WorkInfo.State.ENQUEUED
                },
            ),
        )

        val workers = viewModel.workers.test()
        viewModel.collectWorkerData()

        assertThat(workers.value()).isNotEmpty()
        assertThat(workers.value().first().body).contains("Next run:")
    }
}
