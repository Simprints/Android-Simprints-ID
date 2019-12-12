package com.simprints.id.data.db.down_sync_info.domain

import com.simprints.id.domain.modality.Modes

sealed class DownSyncScope(open val projectId: String)

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
