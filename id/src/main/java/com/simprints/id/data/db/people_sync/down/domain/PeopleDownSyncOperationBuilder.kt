package com.simprints.id.data.db.people_sync.down.domain

import com.simprints.id.domain.modality.Modes

interface PeopleDownSyncOperationBuilder {

    fun buildProjectSyncOperation(
        projectId: String,
        modes: List<Modes>,
        syncOperationResult:
        PeopleDownSyncOperationResult?): PeopleDownSyncOperation

    fun buildUserSyncOperation(
        projectId: String,
        userId: String,
        modes: List<Modes>,
        syncOperationResult: PeopleDownSyncOperationResult?): PeopleDownSyncOperation

    fun buildModuleSyncOperation(
        projectId: String,
        moduleId: String,
        modes: List<Modes>,
        syncOperationResult: PeopleDownSyncOperationResult?): PeopleDownSyncOperation
}
