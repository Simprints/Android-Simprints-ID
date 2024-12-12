package com.simprints.core.tools.utils

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.utils.TimeUtils.getFormattedEstimatedOutage
import org.junit.Test

class TimeUtilKtTest {
    @Test
    fun `given a time in seconds with minutes and seconds format it properly`() {
        val formattedTime = getFormattedEstimatedOutage(180L)

        assertThat(formattedTime).isEqualTo("03 minutes, 00 seconds")
    }

    @Test
    fun `given a time in seconds with seconds format it properly`() {
        val formattedTime = getFormattedEstimatedOutage(18L)

        assertThat(formattedTime).isEqualTo("18 seconds")
    }

    @Test
    fun `given 0 time in seconds format it properly`() {
        val formattedTime = getFormattedEstimatedOutage(0L)

        assertThat(formattedTime).isEqualTo("00 seconds")
    }

    @Test
    fun `given a time in seconds with hours minutes and seconds format it properly`() {
        val formattedTime = getFormattedEstimatedOutage(4000L)

        assertThat(formattedTime).isEqualTo("01 hours, 06 minutes, 40 seconds")
    }
}
