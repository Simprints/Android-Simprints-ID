package com.simprints.feature.troubleshooting

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.jraska.livedata.test
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TroubleshootingViewModelTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private lateinit var viewModel: TroubleshootingViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        viewModel = TroubleshootingViewModel()
    }

    @Test
    fun `propagate intent details opening when called`() = runTest {
        val events = viewModel.shouldOpenIntentDetails.test()
        viewModel.openIntentDetails("test")

        assertThat(events.value()).isNotNull()
    }
}
