package com.simprints.id.data.db.events_sync.down.local

import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.GUID1
import com.simprints.id.commontesttools.DefaultTestConstants.TIME1
import com.simprints.id.commontesttools.DefaultTestConstants.projectSyncScope
import com.simprints.id.data.db.events_sync.down.domain.EventDownSyncOperation.DownSyncState.COMPLETE
import com.simprints.id.data.db.events_sync.down.domain.getUniqueKey
import com.simprints.id.data.db.events_sync.down.local.DbEventsDownSyncOperationState.Companion.buildFromEventsDownSyncOperationState
import org.junit.Test

class DbEventsDownSyncOperationStateTest {

    @Test
    fun buildDbEventsDownSyncOperationState_fromEventDownSyncOperation() {
        val op = projectSyncScope.operations
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
