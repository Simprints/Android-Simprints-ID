package com.simprints.id.data.db.events_sync.up.local

import androidx.annotation.Keep
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.simprints.id.data.db.events_sync.up.domain.EventUpSyncOperation

@Entity(tableName = "DbEventsUpSyncOperation")
@Keep
data class DbEventsUpSyncOperation(
    @PrimaryKey var id: String,
    @Embedded val upSyncOp: EventUpSyncOperation
)

fun EventUpSyncOperation.fromDomainToDb(): DbEventsUpSyncOperation =
    DbEventsUpSyncOperation(
        this.hashCode().toString(),
        this
    )

private fun DbEventsUpSyncOperation.fromDbToDomain() =
    this.upSyncOp

private fun EventUpSyncOperation.getUniqueKey() =
    this.copy(lastState = null, lastSyncTime = null).hashCode()
