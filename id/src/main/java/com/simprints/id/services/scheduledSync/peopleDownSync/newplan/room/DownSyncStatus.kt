package com.simprints.id.services.scheduledSync.peopleDownSync.newplan.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * Aashay - DownSyncStatus: room entity
 *  1) Id
 *  2) ProjectId: String
 *  9) UserId: String?
 *  3) ModuleId: String?
 *  4) LastPatientId: String?
 *  5) LastPatientUpdatedAt: Long?
 *  6) TotalToDownload
 *  7) LastSyncTime
 */

@Entity(tableName = "DownSyncStatus")
data class DownSyncStatus(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val userId: String? = null,
    val moduleId: String? = null,
    val lastPatientId: String? = null,
    val lastPatientUpdatedAt: Long? = null,
    val totalToDownload: Int = 0,
    val lastSyncTime: Long? = null
)
