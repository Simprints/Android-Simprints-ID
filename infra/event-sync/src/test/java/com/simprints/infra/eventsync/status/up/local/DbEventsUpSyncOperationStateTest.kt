package com.simprints.infra.eventsync.status.up.local

import com.google.common.truth.Truth
import com.simprints.infra.events.sampledata.SampleDefaults.TIME1
import com.simprints.infra.eventsync.SampleSyncScopes.projectUpSyncScope
import com.simprints.infra.eventsync.status.up.domain.EventUpSyncOperation.UpSyncState.COMPLETE
import com.simprints.infra.eventsync.status.up.local.DbEventsUpSyncOperationState.Companion.buildFromEventsUpSyncOperationState
import org.junit.Test

class DbEventsUpSyncOperationStateTest {
    @Test
    fun buildDbState_fromEventUpSyncOperation() {
        val op = projectUpSyncScope.operation.copy(lastSyncTime = TIME1, lastState = COMPLETE)
        val downSyncState = buildFromEventsUpSyncOperationState(op)

        with(downSyncState, {
            Truth.assertThat(id).isEqualTo(op.getUniqueKey())
            Truth.assertThat(lastState).isEqualTo(COMPLETE)
            Truth.assertThat(lastUpdatedTime).isEqualTo(TIME1)
        })
    }
}
