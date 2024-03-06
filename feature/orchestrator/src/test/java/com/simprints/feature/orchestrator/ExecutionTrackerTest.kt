package com.simprints.feature.orchestrator

import androidx.lifecycle.Lifecycle
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class ExecutionTrackerTest {

    private lateinit var executionTracker: ExecutionTracker

    @Before
    fun setUp() {
        executionTracker = ExecutionTracker()
    }

    @Test
    fun `resets execution flag if single activity created and destroyed`() {
        executionTracker.onStateChanged(mockk(), Lifecycle.Event.ON_CREATE)
        executionTracker.isExecuting.set(true)

        executionTracker.onStateChanged(mockk(), Lifecycle.Event.ON_DESTROY)
        assertThat(executionTracker.isExecuting.get()).isFalse()
    }

    @Test
    fun `resets execution flag if multiple activity created and destroyed`() {
        executionTracker.onStateChanged(mockk(), Lifecycle.Event.ON_CREATE)
        executionTracker.onStateChanged(mockk(), Lifecycle.Event.ON_CREATE)
        executionTracker.isExecuting.set(true)

        executionTracker.onStateChanged(mockk(), Lifecycle.Event.ON_DESTROY)
        assertThat(executionTracker.isExecuting.get()).isTrue()

        executionTracker.onStateChanged(mockk(), Lifecycle.Event.ON_DESTROY)
        assertThat(executionTracker.isExecuting.get()).isFalse()
    }
}
