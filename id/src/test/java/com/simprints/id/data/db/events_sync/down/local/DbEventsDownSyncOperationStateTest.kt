package com.simprints.eventsystem.events_sync.down.local

import com.google.common.truth.Truth.assertThat
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID1
import com.simprints.eventsystem.sampledata.SampleDefaults.TIME1
import com.simprints.eventsystem.sampledata.SampleDefaults.projectDownSyncScope
import com.simprints.eventsystem.events_sync.down.domain.EventDownSyncOperation.DownSyncState.COMPLETE
import com.simprints.eventsystem.events_sync.down.domain.getUniqueKey
import com.simprints.eventsystem.events_sync.down.local.DbEventsDownSyncOperationState.Companion.buildFromEventsDownSyncOperationState
import org.junit.Test

class DbEventsDownSyncOperationStateTest {

    @Test
    fun buildDbEventsDownSyncOperationState_fromEventDownSyncOperation() {
        val op = projectDownSyncScope.operations
            .first().copy(lastSyncTime = TIME1, lastEventId = GUID1, state = COMPLETE)
        val downSyncState = buildFromEventsDownSyncOperationState(op)

        with(downSyncState) {
            assertThat(id).isEqualTo(op.getUniqueKey())
            assertThat(lastEventId).isEqualTo(GUID1)
            assertThat(lastState).isEqualTo(COMPLETE)
            assertThat(lastUpdatedTime).isEqualTo(TIME1)
        }
    }
}
