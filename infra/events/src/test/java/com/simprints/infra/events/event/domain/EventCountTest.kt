package com.simprints.infra.events.event.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class EventCountTest {
    @Test
    fun getExactCount() {
        assertThat(EventCount(5, false).exactCount).isEqualTo(5)
        assertThat(EventCount(5, true).exactCount).isNull()
    }
}
