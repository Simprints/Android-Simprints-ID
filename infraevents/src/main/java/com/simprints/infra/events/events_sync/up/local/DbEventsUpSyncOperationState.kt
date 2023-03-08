package com.simprints.infra.events.events_sync.up.local

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.simprints.infra.events.events_sync.up.domain.EventUpSyncOperation
import com.simprints.infra.events.events_sync.up.domain.EventUpSyncOperation.UpSyncState
import com.simprints.infra.events.events_sync.up.domain.getUniqueKey

@Entity(tableName = "DbEventsUpSyncOperation")
@Keep
data class DbEventsUpSyncOperationState(
    @PrimaryKey var id: String,
    val lastState: UpSyncState?,
    val lastUpdatedTime: Long?
) {
    companion object {
        fun buildFromEventsUpSyncOperationState(op: EventUpSyncOperation) =
            DbEventsUpSyncOperationState(
                op.getUniqueKey(),
                op.lastState,
                op.lastSyncTime
            )
    }
}
