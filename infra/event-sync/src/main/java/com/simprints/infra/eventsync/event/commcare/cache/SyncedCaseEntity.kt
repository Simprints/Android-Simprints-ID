package com.simprints.infra.eventsync.event.commcare.cache

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "synced_commcare_cases")
data class SyncedCaseEntity(
    @PrimaryKey
    val caseId: String,
    val simprintsId: String,
    val lastSyncedTimestamp: Long,
)
