package com.simprints.core.tools.time

import com.google.common.truth.Truth.assertThat
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TimerImplTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private lateinit var timerImpl: TimerImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        timerImpl = TimerImpl()
    }

    @Test
    fun testObserveTickOncePerMinute_emitsImmediately() = runTest {
        val result = timerImpl.observeTickOncePerMinute()
            .take(1)
            .toList()

        assertThat(result).hasSize(1)
        assertThat(result[0]).isEqualTo(Unit)
    }

    @Test
    fun testObserveTickOncePerMinute_emitsMultipleTimes() = runTest {
        val result = timerImpl.observeTickOncePerMinute()
            .take(3)
            .toList()

        assertThat(result).hasSize(3)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testObserveTickOncePerMinute_waitsForCorrectTime() = runTest {
        val flow = timerImpl.observeTickOncePerMinute()

        // 1st tick immediately
        assertThat(flow.first()).isEqualTo(Unit)

        // no next tick earlier than in a minute
        val deferred = async { flow.drop(1).first() }
        advanceTimeBy(59_000L)
        assertThat(deferred.isCompleted).isFalse()

        // next tick in a full minute
        advanceTimeBy(1_000L)
        deferred.await()
        assertThat(deferred.isCompleted).isTrue()
    }
}
