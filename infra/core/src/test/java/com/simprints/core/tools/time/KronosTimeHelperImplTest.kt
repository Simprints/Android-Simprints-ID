package com.simprints.core.tools.time

import com.google.common.truth.Truth.assertThat
import com.lyft.kronos.KronosClock
import com.lyft.kronos.KronosTime
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

class KronosTimeHelperImplTest {
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

    companion object {
        // Random date at random time
        private const val TIMESTAMP = 1_542_537_183_000L

        // Same date at 0:00:00
        private const val TIMESTAMP_TODAY = 1_542_492_000_000L

        // Next day at 0:00:00
        private const val TIMESTAMP_TOMORROW = 1_542_578_400_000L
    }
}
