package com.simprints.id.data.db.people_sync.down.domain

import com.simprints.id.domain.modality.Modes

class PeopleDownSyncOperationFactoryImpl : PeopleDownSyncOperationFactory {

    override fun buildProjectSyncOperation(projectId: String,
                                           modes: List<Modes>,
                                           syncOperationResult: PeopleDownSyncOperationResult?) =
        PeopleDownSyncOperation(
            projectId = projectId,
            userId = null,
            moduleId = null,
            modes = modes,
            lastResult = syncOperationResult
        )

    override fun buildUserSyncOperation(projectId: String,
                                        userId: String,
                                        modes: List<Modes>,
                                        syncOperationResult: PeopleDownSyncOperationResult?) =
        PeopleDownSyncOperation(
            projectId = projectId,
            userId = userId,
            moduleId = null,
            modes = modes,
            lastResult = syncOperationResult
        )

    override fun buildModuleSyncOperation(projectId: String,
                                          moduleId: String,
                                          modes: List<Modes>,
                                          syncOperationResult: PeopleDownSyncOperationResult?) =
        PeopleDownSyncOperation(
            projectId = projectId,
            userId = null,
            moduleId = moduleId,
            modes = modes,
            lastResult = syncOperationResult
        )
}
