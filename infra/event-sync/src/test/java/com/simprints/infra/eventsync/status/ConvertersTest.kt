package com.simprints.infra.eventsync.status

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation.DownSyncState
import com.simprints.infra.eventsync.status.up.domain.EventUpSyncOperation.UpSyncState
import org.junit.Test

class ConvertersTest {
    private val converters = Converters()

    @Test
    fun `correctly converts DownSyncState`() {
        mapOf(
            DownSyncState.RUNNING to "RUNNING",
            DownSyncState.COMPLETE to "COMPLETE",
            DownSyncState.FAILED to "FAILED",
        ).forEach { (state, name) ->
            assertThat(converters.fromDownSyncStateToString(state)).isEqualTo(name)
            assertThat(converters.fromStringToDownSyncState(name)).isEqualTo(state)
        }
    }

    @Test
    fun `correctly converts UpSyncState`() {
        mapOf(
            UpSyncState.RUNNING to "RUNNING",
            UpSyncState.COMPLETE to "COMPLETE",
            UpSyncState.FAILED to "FAILED",
        ).forEach { (state, name) ->
            assertThat(converters.fromUpSyncStateToString(state)).isEqualTo(name)
            assertThat(converters.fromStringToUpSyncState(name)).isEqualTo(state)
        }
    }
}
