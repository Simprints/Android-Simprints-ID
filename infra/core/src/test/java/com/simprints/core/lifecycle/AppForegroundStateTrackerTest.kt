package com.simprints.core.lifecycle

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.common.truth.Truth.assertThat
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AppForegroundStateTrackerTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val processLifecycleOwner = mockk<ProcessLifecycleOwner>()
    private val lifecycle = mockk<Lifecycle>()

    private lateinit var foregroundStateTracker: AppForegroundStateTracker

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkObject(ProcessLifecycleOwner.Companion)

        every { ProcessLifecycleOwner.Companion.get() } returns processLifecycleOwner
        every { processLifecycleOwner.lifecycle } returns lifecycle
        every { lifecycle.addObserver(any()) } returns Unit
        every { lifecycle.removeObserver(any()) } returns Unit

        foregroundStateTracker = AppForegroundStateTracker(testCoroutineRule.testCoroutineDispatcher)
    }

    @Test
    fun `observeAppInForeground returns true when app goes into foreground`() = runTest {
        val observerSlot = slot<DefaultLifecycleObserver>()
        every { lifecycle.addObserver(capture(observerSlot)) } returns Unit
        val channel = Channel<Boolean>(Channel.UNLIMITED)

        val job = launch {
            foregroundStateTracker.observeAppInForeground().collect {
                channel.trySend(it)
            }
        }
        while (!observerSlot.isCaptured) {
            testScheduler.advanceUntilIdle()
        }
        observerSlot.captured.onResume(mockk())
        val result = channel.receive()
        job.cancel()

        assertThat(result).isTrue()
    }

    @Test
    fun `observeAppInForeground returns false when app goes into background`() = runTest {
        val observerSlot = slot<DefaultLifecycleObserver>()
        every { lifecycle.addObserver(capture(observerSlot)) } returns Unit
        val channel = Channel<Boolean>(Channel.UNLIMITED)

        val job = launch {
            foregroundStateTracker.observeAppInForeground().collect {
                channel.trySend(it)
            }
        }
        while (!observerSlot.isCaptured) {
            testScheduler.advanceUntilIdle()
        }
        observerSlot.captured.onPause(mockk())
        val result = channel.receive()
        job.cancel()

        assertThat(result).isFalse()
    }
}
