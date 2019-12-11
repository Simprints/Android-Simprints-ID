package com.simprints.id.data.db.syncscope.domain

import com.simprints.id.domain.modality.Modes

sealed class DownSyncScope(open val projectId: String) {

    open fun getDownSyncOperations(): List<DownSyncOperation> = emptyList()
}

data class ProjectSyncScope(
    override val projectId: String,
    val modes: List<Modes>) : DownSyncScope(projectId)

data class UserSyncScope(
    override val projectId: String,
    val userId: String,
    val modes: List<Modes>) : DownSyncScope(projectId)

data class ModuleSyncScope(
    override val projectId: String,
    val modules: List<String>,
    val modes: List<Modes>) : DownSyncScope(projectId)
