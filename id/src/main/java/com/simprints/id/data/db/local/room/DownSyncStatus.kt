package com.simprints.id.data.db.local.room

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "DownSyncStatus")
data class DownSyncStatus(
    @PrimaryKey var id: String,
    var projectId: String,
    var userId: String? = null,
    var moduleId: String? = null,
    var lastPatientId: String? = null,
    var lastPatientUpdatedAt: Long? = null,
    var totalToDownload: Int = 0,
    var lastSyncTime: Long? = null
) {
    @Ignore
    constructor(projectId: String,
                userId: String? = null,
                moduleId: String? = null,
                lastPatientId: String? = null,
                lastPatientUpdatedAt: Long? = null,
                totalToDownload: Int = 0,
                lastSyncTime: Long? = null):
        this(id = "${projectId}_${userId ?: ""}_${moduleId ?: ""}",
            projectId = projectId,
            userId = userId,
            moduleId = moduleId,
            lastPatientId = lastPatientId,
            lastPatientUpdatedAt = lastPatientUpdatedAt,
            totalToDownload = totalToDownload,
            lastSyncTime = lastSyncTime)
}
