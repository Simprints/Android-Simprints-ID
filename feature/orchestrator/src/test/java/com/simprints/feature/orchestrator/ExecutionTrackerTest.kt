package com.simprints.feature.orchestrator

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class ExecutionTrackerTest {
    private lateinit var executionTracker: ExecutionTracker
    private lateinit var timeHelper: TimeHelper
    private val executionTimeLimitSec: Int = 1

    @Before
    fun setUp() {
        timeHelper = mockk()
        every { timeHelper.now() } returns Timestamp(0L)
        every { timeHelper.msBetweenNowAndTime(any()) } returns 0L

        executionTracker = ExecutionTracker(
            timeHelper = timeHelper,
            executionTimeLimitSec = executionTimeLimitSec,
        )
    }

    @Test
    fun `when owner is created, then it becomes the main executor`() {
        val ownerId = 123
        val owner = mockk<LifecycleOwner>()
        every { owner.hashCode() } returns ownerId

        executionTracker.onStateChanged(owner, Lifecycle.Event.ON_CREATE)

        assertThat(executionTracker.isMain(owner)).isTrue()
    }

    @Test
    fun `when owner is destroyed, then it is no longer remains the main executor`() {
        val ownerId = 123
        val owner = mockk<LifecycleOwner>()
        every { owner.hashCode() } returns ownerId

        executionTracker.onStateChanged(owner, Lifecycle.Event.ON_CREATE)
        executionTracker.onStateChanged(owner, Lifecycle.Event.ON_DESTROY)

        assertThat(executionTracker.isMain(owner)).isFalse()
    }

    @Test
    fun `given second owner, when first owner is destroyed, then the second owner becomes the main executor`() {
        val ownerId1 = 123
        val ownerId2 = 456
        val firstOwner = mockk<LifecycleOwner>()
        val secondOwner = mockk<LifecycleOwner>()
        every { firstOwner.hashCode() } returns ownerId1
        every { secondOwner.hashCode() } returns ownerId2

        executionTracker.onStateChanged(firstOwner, Lifecycle.Event.ON_CREATE)
        executionTracker.onStateChanged(firstOwner, Lifecycle.Event.ON_DESTROY)
        executionTracker.onStateChanged(secondOwner, Lifecycle.Event.ON_CREATE)

        assertThat(executionTracker.isMain(secondOwner)).isTrue()
    }

    @Test
    fun `given second owner, when first owner is not yet destroyed, then the first owner remains the main executor`() {
        val ownerId1 = 123
        val ownerId2 = 456
        val firstOwner = mockk<LifecycleOwner>()
        val secondOwner = mockk<LifecycleOwner>()
        every { firstOwner.hashCode() } returns ownerId1
        every { secondOwner.hashCode() } returns ownerId2

        executionTracker.onStateChanged(firstOwner, Lifecycle.Event.ON_CREATE)
        executionTracker.onStateChanged(secondOwner, Lifecycle.Event.ON_CREATE)

        assertThat(executionTracker.isMain(firstOwner)).isTrue()
    }

    @Test
    fun `given second owner and not enough time passed to unlock the main executor, when first owner is not yet destroyed, then the first owner remains the main executor`() {
        val ownerId1 = 123
        val ownerId2 = 456
        val firstOwner = mockk<LifecycleOwner>()
        val secondOwner = mockk<LifecycleOwner>()

        every { firstOwner.hashCode() } returns ownerId1
        every { secondOwner.hashCode() } returns ownerId2
        every { timeHelper.msBetweenNowAndTime(any()) } returns 1000L * (executionTimeLimitSec)

        executionTracker.onStateChanged(firstOwner, Lifecycle.Event.ON_CREATE)
        executionTracker.onStateChanged(secondOwner, Lifecycle.Event.ON_CREATE)

        assertThat(executionTracker.isMain(firstOwner)).isTrue()
    }

    @Test
    fun `given second owner and enough time passed to unlock the main executor, when first owner is not yet destroyed, then the second owner becomes the main executor`() {
        val ownerId1 = 123
        val ownerId2 = 456
        val firstOwner = mockk<LifecycleOwner>()
        val secondOwner = mockk<LifecycleOwner>()

        every { firstOwner.hashCode() } returns ownerId1
        every { secondOwner.hashCode() } returns ownerId2
        every { timeHelper.msBetweenNowAndTime(any()) } returns 1000L * (executionTimeLimitSec + 1)

        executionTracker.onStateChanged(firstOwner, Lifecycle.Event.ON_CREATE)
        executionTracker.onStateChanged(secondOwner, Lifecycle.Event.ON_CREATE)

        assertThat(executionTracker.isMain(secondOwner)).isTrue()
    }
}
