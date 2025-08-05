package com.simprints.core.tools.time

import com.google.common.truth.Truth.assertThat
import com.lyft.kronos.KronosClock
import com.lyft.kronos.KronosTime
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

class KronosTimeHelperImplTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var kronosClock: KronosClock

    private lateinit var timeHelperImpl: KronosTimeHelperImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        timeHelperImpl = KronosTimeHelperImpl(kronosClock)
    }

    @Test
    fun ensureTrustworthiness() {
        timeHelperImpl.ensureTrustworthiness()

        verify { kronosClock.sync() }
    }

    @Test
    fun testNowTrustworthy() {
        every { kronosClock.getCurrentTime() } returns KronosTime(1000L, 1L)
        every { kronosClock.getElapsedTimeMs() } returns 3L

        assertThat(timeHelperImpl.now()).isEqualTo(Timestamp(1000L, true, 3L))
    }

    @Test
    fun testNowIsNotTrustworthy() {
        every { kronosClock.getCurrentTime() } returns KronosTime(1000L, null)
        every { kronosClock.getElapsedTimeMs() } returns 3L

        assertThat(timeHelperImpl.now()).isEqualTo(Timestamp(1000L, false, 3L))
    }

    @Test
    fun testMsBetweenNowAndTime() {
        every { kronosClock.getCurrentTime() } returns KronosTime(3000L, 0L)
        every { kronosClock.getElapsedTimeMs() } returns 3L

        val result = timeHelperImpl.msBetweenNowAndTime(Timestamp(1000L))

        assertThat(result).isEqualTo(2000L)
    }

    @Ignore("This test is failing in pipeline tests for unknown reasons")
    @Test
    fun testTodayInMillis() {
        every { kronosClock.getCurrentTime() } returns KronosTime(TIMESTAMP, 0L)
        every { kronosClock.getElapsedTimeMs() } returns 3L

        val result = timeHelperImpl.todayInMillis()

        assertThat(result).isEqualTo(TIMESTAMP_TODAY)
    }

    @Ignore("This test is failing in pipeline tests for unknown reasons")
    @Test
    fun testTomorrowInMillis() {
        every { kronosClock.getCurrentTime() } returns KronosTime(TIMESTAMP, 0L)
        every { kronosClock.getElapsedTimeMs() } returns 3L

        val result = timeHelperImpl.tomorrowInMillis()

        assertThat(result).isEqualTo(TIMESTAMP_TOMORROW)
    }

    @Test
    fun testWatchOncePerMinute_emitsImmediately() = runTest {
        val result = timeHelperImpl.watchOncePerMinute()
            .take(1)
            .toList()

        assertThat(result).hasSize(1)
        assertThat(result[0]).isEqualTo(Unit)
    }

    @Test
    fun testWatchOncePerMinute_emitsMultipleTimes() = runTest {
        val result = timeHelperImpl.watchOncePerMinute()
            .take(3)
            .toList()

        assertThat(result).hasSize(3)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testWatchOncePerMinute_waitsForCorrectTime() = runTest {
        val flow = timeHelperImpl.watchOncePerMinute()

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

    companion object {
        // Random date at random time
        private const val TIMESTAMP = 1_542_537_183_000L

        // Same date at 0:00:00
        private const val TIMESTAMP_TODAY = 1_542_492_000_000L

        // Next day at 0:00:00
        private const val TIMESTAMP_TOMORROW = 1_542_578_400_000L
    }
}
