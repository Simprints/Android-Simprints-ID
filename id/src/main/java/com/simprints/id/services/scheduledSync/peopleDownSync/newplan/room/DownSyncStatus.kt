package com.simprints.id.services.scheduledSync.peopleDownSync.newplan.room

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

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
class DownSyncStatus(
    @PrimaryKey val id: String,
    val projectId: String,
    val userId: String? = null,
    val moduleId: String? = null,
    val lastPatientId: String? = null,
    val lastPatientUpdatedAt: Long? = null,
    val totalToDownload: Int = 0,
    val lastSyncTime: Long? = null
) {
    @Ignore
    constructor(projectId: String,
                userId: String? = null,
                moduleId: String? = null,
                lastPatientId: String? = null,
                lastPatientUpdatedAt: Long? = null,
                totalToDownload: Int = 0,
                lastSyncTime: Long?):
        this(id = "${projectId}_${userId ?: ""}_${moduleId ?: ""}",
            projectId = projectId,
            userId = userId,
            moduleId = moduleId,
            lastPatientId = lastPatientId,
            lastPatientUpdatedAt = lastPatientUpdatedAt,
            totalToDownload = totalToDownload,
            lastSyncTime = lastSyncTime)
}
