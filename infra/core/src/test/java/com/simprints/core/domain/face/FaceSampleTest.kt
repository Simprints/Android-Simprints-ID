package com.simprints.core.domain.face

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FaceSampleTest {

    @Test
    fun testUniqueId() {
        assertThat(listOf<FaceSample>().uniqueId()).isNull()
        assertThat(
            listOf(
                FaceSample(
                    template = byteArrayOf(1, 2),
                    format = ""
                ),
            ).uniqueId()
        ).isNotNull()
    }

    @Test
    fun testConcatTemplates() {
        val samples = listOf(
            FaceSample(
                template = byteArrayOf(2),
                format = ""
            ),
            FaceSample(
                template = byteArrayOf(1),
                format = ""
            ),
            FaceSample(
                template = byteArrayOf(3),
                format = ""
            ),
        )
        assertThat(samples.concatTemplates()).isEqualTo(byteArrayOf(1, 2, 3))
    }

}
