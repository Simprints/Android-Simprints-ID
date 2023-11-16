package com.simprints.core.tools.time

import com.google.common.truth.Truth.assertThat
import com.lyft.kronos.KronosClock
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import java.util.concurrent.TimeUnit

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
    fun testNow() {
        every { kronosClock.getCurrentTimeMs() } returns 1000L

        assertThat(timeHelperImpl.now()).isEqualTo(1000L)
    }

    @Test
    fun testNowMinus() {
        every { kronosClock.getCurrentTimeMs() } returns 2000L

        val result = timeHelperImpl.nowMinus(1L, TimeUnit.SECONDS)

        // 2000ms - 1s = 1000ms
        assertThat(result).isEqualTo(1000L)
    }

    @Test
    fun testMsBetweenNowAndTime() {
        every { kronosClock.getCurrentTimeMs() } returns 3000L

        val result = timeHelperImpl.msBetweenNowAndTime(1000L)

        assertThat(result).isEqualTo(2000L)
    }

    @Ignore("This test fails on pipeline")
    @Test
    fun testTodayInMillis() {
        every { kronosClock.getCurrentTimeMs() } returns TIMESTAMP

        val result = timeHelperImpl.todayInMillis()

        assertThat(result).isEqualTo(TIMESTAMP_TODAY)
    }

    @Ignore("This test fails on pipeline")
    @Test
    fun testTomorrowInMillis() {
        every { kronosClock.getCurrentTimeMs() } returns TIMESTAMP

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
