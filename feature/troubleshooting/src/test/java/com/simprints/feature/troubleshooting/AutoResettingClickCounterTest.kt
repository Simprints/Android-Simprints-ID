package com.simprints.feature.troubleshooting

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AutoResettingClickCounterTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private lateinit var counter: AutoResettingClickCounter

    @Before
    fun setUp() {
        counter = AutoResettingClickCounter(requiredClicks = 3, resetDelayMs = 2000L)
    }

    @Test
    fun `returns true after required amount of clicks`() = runTest {
        assertThat(counter.handleClick(backgroundScope)).isFalse()
        assertThat(counter.handleClick(backgroundScope)).isFalse()
        // Third time the charm
        assertThat(counter.handleClick(backgroundScope)).isTrue()
    }

    @Test
    fun `should reset counter after delay if clicks count not reached`() = runTest {
        assertThat(counter.handleClick(backgroundScope)).isFalse()
        assertThat(counter.handleClick(backgroundScope)).isFalse()

        advanceTimeBy(5000L)

        assertThat(counter.handleClick(backgroundScope)).isFalse()
        assertThat(counter.handleClick(backgroundScope)).isFalse()
        assertThat(counter.handleClick(backgroundScope)).isTrue()
    }
}
