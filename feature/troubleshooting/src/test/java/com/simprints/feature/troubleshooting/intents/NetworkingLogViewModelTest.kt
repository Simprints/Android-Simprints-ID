package com.simprints.feature.troubleshooting.intents

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.jraska.livedata.test
import com.simprints.logging.persistent.LogEntry
import com.simprints.logging.persistent.LogEntryType
import com.simprints.logging.persistent.PersistentLogger
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NetworkingLogViewModelTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var persistentLogger: PersistentLogger

    private lateinit var viewModel: IntentLogViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        viewModel = IntentLogViewModel(
            persistentLogger = persistentLogger,
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
