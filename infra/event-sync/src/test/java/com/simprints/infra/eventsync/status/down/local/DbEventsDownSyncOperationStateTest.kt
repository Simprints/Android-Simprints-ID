package com.simprints.infra.eventsync.status.down.local

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.infra.events.sampledata.SampleDefaults.TIME1
import com.simprints.infra.eventsync.SampleSyncScopes.projectDownSyncScope
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation.DownSyncState.COMPLETE
import com.simprints.infra.eventsync.status.down.local.DbEventsDownSyncOperationState.Companion.buildFromEventsDownSyncOperationState
import org.junit.Test

class DbEventsDownSyncOperationStateTest {
    @Test
    fun buildDbEventsDownSyncOperationState_fromEventDownSyncOperation() {
        val op = projectDownSyncScope.operations
            .first()
            .copy(lastSyncTime = TIME1, lastEventId = GUID1, state = COMPLETE)
        val downSyncState = buildFromEventsDownSyncOperationState(op)

        with(downSyncState) {
            assertThat(id).isEqualTo(op.getUniqueKey())
            assertThat(lastEventId).isEqualTo(GUID1)
            assertThat(lastState).isEqualTo(COMPLETE)
            assertThat(lastUpdatedTime).isEqualTo(TIME1)
        }
    }
}
