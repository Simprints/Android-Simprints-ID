package com.simprints.id.data.db.events_sync.down.local

import androidx.annotation.Keep
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.simprints.id.data.db.events_sync.down.domain.EventDownSyncOperation

@Entity(tableName = "DbEventsDownSyncOperation")
@Keep
data class DbEventsDownSyncOperation(
    @PrimaryKey var id: Int,
    @Embedded val downSyncOp: EventDownSyncOperation
)

fun EventDownSyncOperation.fromDomainToDb(): DbEventsDownSyncOperation =
    DbEventsDownSyncOperation(
        this.getUniqueKey(),
        this
    )

fun DbEventsDownSyncOperation.fromDbToDomain() =
    this.downSyncOp

fun EventDownSyncOperation.getUniqueKey() =
    this.queryEvent.copy(lastEventId = null).hashCode()
//Unique key: all request params expect for lastEventId
