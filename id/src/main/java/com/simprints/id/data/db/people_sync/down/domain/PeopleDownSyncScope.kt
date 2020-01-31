package com.simprints.id.data.db.people_sync.down.domain

import com.simprints.id.domain.modality.Modes

sealed class PeopleDownSyncScope(open val projectId: String)

data class ProjectSyncScope(
    override val projectId: String,
    val modes: List<Modes>) : PeopleDownSyncScope(projectId)

data class UserSyncScope(
    override val projectId: String,
    val userId: String,
    val modes: List<Modes>) : PeopleDownSyncScope(projectId)

data class ModuleSyncScope(
    override val projectId: String,
    val modules: List<String>,
    val modes: List<Modes>) : PeopleDownSyncScope(projectId)
