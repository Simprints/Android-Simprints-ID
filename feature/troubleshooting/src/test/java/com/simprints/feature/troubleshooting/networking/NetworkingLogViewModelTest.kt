package com.simprints.feature.troubleshooting.networking

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.jraska.livedata.test
import com.simprints.logging.persistent.LogEntry
import com.simprints.logging.persistent.LogEntryType
import com.simprints.logging.persistent.PersistentLogger
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.text.SimpleDateFormat

class NetworkingLogViewModelTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var persistentLogger: PersistentLogger

    @MockK
    private lateinit var dateFormatter: SimpleDateFormat

    private lateinit var viewModel: NetworkingLogViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        every { dateFormatter.format(any()) } returns "date"

        viewModel = NetworkingLogViewModel(
            persistentLogger = persistentLogger,
            dateFormatter = dateFormatter,
        )
    }

    @Test
    fun `sets list of logs on request`() = runTest {
        coEvery { persistentLogger.get(any()) } returns listOf(
            LogEntry(0L, LogEntryType.Network, "", ""),
        )

        val scopes = viewModel.logs.test()
        viewModel.collectData()

        assertThat(scopes.value()).isNotEmpty()
    }

    @Test
    fun `sets placeholder if no logs`() = runTest {
        coEvery { persistentLogger.get(any()) } returns emptyList()

        val scopes = viewModel.logs.test()
        viewModel.collectData()

        assertThat(scopes.value()).isNotEmpty()
    }
}
