package com.simprints.infra.eventsync

// todo MS-1278 disband into usecases
interface EventSyncManager {
    fun getPeriodicWorkTags(): List<String>

    fun getOneTimeWorkTags(): List<String>

    fun getAllWorkerTag(): String

    suspend fun downSyncSubject(
        projectId: String,
        subjectId: String,
        metadata: String,
    )

    suspend fun deleteModules(unselectedModules: List<String>)

    suspend fun deleteSyncInfo()

    suspend fun resetDownSyncInfo()
}
