package com.simprints.infra.eventsync.status.down.local

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation.DownSyncState

@Entity(tableName = "DbEventsDownSyncOperation")
@Keep
internal data class DbEventsDownSyncOperationState(
    @PrimaryKey var id: String,
    val lastState: DownSyncState?,
    val lastEventId: String?,
    val lastUpdatedTime: Long?,
) {
    companion object {
        fun buildFromEventsDownSyncOperationState(op: EventDownSyncOperation) = DbEventsDownSyncOperationState(
            op.getUniqueKey(),
            op.state,
            op.lastEventId,
            op.lastSyncTime,
        )
    }
}
