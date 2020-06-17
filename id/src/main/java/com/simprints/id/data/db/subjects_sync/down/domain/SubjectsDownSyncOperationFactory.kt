package com.simprints.id.data.db.subjects_sync.down.domain

import com.simprints.id.domain.modality.Modes

interface SubjectsDownSyncOperationFactory {

    fun buildProjectSyncOperation(
        projectId: String,
        modes: List<Modes>,
        syncOperationResult:
        SubjectsDownSyncOperationResult?): SubjectsDownSyncOperation

    fun buildUserSyncOperation(
        projectId: String,
        userId: String,
        modes: List<Modes>,
        syncOperationResult: SubjectsDownSyncOperationResult?): SubjectsDownSyncOperation

    fun buildModuleSyncOperation(
        projectId: String,
        moduleId: String,
        modes: List<Modes>,
        syncOperationResult: SubjectsDownSyncOperationResult?): SubjectsDownSyncOperation
}
