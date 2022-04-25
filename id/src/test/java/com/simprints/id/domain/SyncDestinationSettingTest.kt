package com.simprints.id.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SyncDestinationSettingTest {

    @Test
    fun `given empty list return false for checking both sync destination`() {
        assertThat(emptyList<SyncDestinationSetting>().containsCommcare()).isFalse()
        assertThat(emptyList<SyncDestinationSetting>().containsSimprints()).isFalse()
    }

    @Test
    fun `given list with both destinations return true for checking both sync destination`() {
        val destinations = listOf(SyncDestinationSetting.COMMCARE, SyncDestinationSetting.SIMPRINTS)
        assertThat(destinations.containsCommcare()).isTrue()
        assertThat(destinations.containsSimprints()).isTrue()
    }

    @Test
    fun `given list with simprints destinations correct values`() {
        val destinations = listOf(SyncDestinationSetting.SIMPRINTS)
        assertThat(destinations.containsCommcare()).isFalse()
        assertThat(destinations.containsSimprints()).isTrue()
    }

    @Test
    fun `given list with commcare destinations return correct values`() {
        val destinations = listOf(SyncDestinationSetting.COMMCARE)
        assertThat(destinations.containsCommcare()).isTrue()
        assertThat(destinations.containsSimprints()).isFalse()
    }
}
