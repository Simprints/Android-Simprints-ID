package com.simprints.id.data.db.events_sync.down.local

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.simprints.id.data.db.events_sync.down.domain.EventDownSyncOperation
import com.simprints.id.data.db.events_sync.down.domain.EventDownSyncOperation.DownSyncState
import com.simprints.id.data.db.events_sync.down.domain.getUniqueKey

@Entity(tableName = "DbEventsDownSyncOperation")
@Keep
data class DbEventsDownSyncOperationState(
    @PrimaryKey var id: Int,
    val lastState: DownSyncState?,
    val lastEventId: String?,
    val lastUpdatedTime: Long?
) {

    companion object {
        fun buildFromEventsDownSyncOperationState(op: EventDownSyncOperation) =
            DbEventsDownSyncOperationState(
                op.getUniqueKey(),
                op.state,
                op.lastEventId,
                op.lastSyncTime
            )
    }
}
