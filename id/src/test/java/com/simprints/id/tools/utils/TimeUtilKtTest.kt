package com.simprints.id.tools.utils

import com.google.common.truth.Truth
import org.junit.Test

class TimeUtilKtTest {

    @Test
    fun `given a time in seconds with minutes and seconds format it properly`() {
        val formattedTime = getFormattedEstimatedOutage(180L)

        Truth.assertThat(formattedTime).isEqualTo("03 minutes, 00 seconds")
    }

    @Test
    fun `given a time in seconds with hours minutes and seconds format it properly`() {
        val formattedTime = getFormattedEstimatedOutage(4000L)

        Truth.assertThat(formattedTime).isEqualTo("01 hours, 06 minutes, 40 seconds")
    }
}
