package com.simprints.infra.eventsync.status.up.local

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.simprints.infra.eventsync.status.up.domain.EventUpSyncOperation
import com.simprints.infra.eventsync.status.up.domain.EventUpSyncOperation.UpSyncState

@Entity(tableName = "DbEventsUpSyncOperation")
@Keep
internal data class DbEventsUpSyncOperationState(
    @PrimaryKey var id: String,
    val lastState: UpSyncState?,
    val lastUpdatedTime: Long?,
) {
    companion object {
        fun buildFromEventsUpSyncOperationState(op: EventUpSyncOperation) = DbEventsUpSyncOperationState(
            op.getUniqueKey(),
            op.lastState,
            op.lastSyncTime,
        )
    }
}
