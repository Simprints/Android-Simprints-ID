package com.simprints.id.data.db.syncinfo.domain

import com.simprints.id.domain.modality.Modes

sealed class DownSyncScope {

    open fun getDownSyncOperations(): Array<DownSyncOperation> = emptyArray()
}

data class ProjectSyncScope(
    val projectId: String,
    val modes: List<Modes>) : DownSyncScope() {
}

data class UserSyncScope(
    val projectId: String,
    val userId: String,
    val modes: List<Modes>) : DownSyncScope() {
}

data class ModuleSyncScope(
    val projectId: String,
    val modules: List<String>,
    val modes: List<Modes>) : DownSyncScope() {
}
