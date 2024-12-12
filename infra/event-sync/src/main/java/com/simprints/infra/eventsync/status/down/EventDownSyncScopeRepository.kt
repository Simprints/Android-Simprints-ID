package com.simprints.infra.eventsync.status.down

import com.simprints.core.domain.common.Partitioning
import com.simprints.core.domain.modality.Modes
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.eventsync.exceptions.MissingArgumentForDownSyncScopeException
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncScope
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncScope.SubjectModuleScope
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncScope.SubjectProjectScope
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncScope.SubjectUserScope
import com.simprints.infra.eventsync.status.down.local.DbEventDownSyncOperationStateDao
import com.simprints.infra.eventsync.status.down.local.DbEventsDownSyncOperationState.Companion.buildFromEventsDownSyncOperationState
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import javax.inject.Inject

internal class EventDownSyncScopeRepository @Inject constructor(
    private val authStore: AuthStore,
    private val recentUserActivityManager: RecentUserActivityManager,
    private val downSyncOperationOperationDao: DbEventDownSyncOperationStateDao,
    private val configManager: ConfigManager,
    private val tokenizationProcessor: TokenizationProcessor,
) {
    suspend fun getDownSyncScope(
        modes: List<Modes>,
        selectedModuleIDs: List<String>,
        syncPartitioning: Partitioning,
    ): EventDownSyncScope {
        val projectId = getProjectId()

        val syncScope = when (syncPartitioning) {
            Partitioning.GLOBAL -> SubjectProjectScope(projectId, modes)
            Partitioning.USER -> SubjectUserScope(projectId, getUserId(projectId), modes)
            Partitioning.MODULE -> SubjectModuleScope(projectId, selectedModuleIDs, modes)
        }

        syncScope.operations = syncScope.operations.map { op -> refreshState(op) }
        return syncScope
    }

    private fun getProjectId(): String {
        val projectId = authStore.signedInProjectId
        if (projectId.isBlank()) {
            throw MissingArgumentForDownSyncScopeException("ProjectId required")
        }
        return projectId
    }

    private suspend fun getUserId(projectId: String): String {
        // After we are certain that all users have user IDs cached (no-one uses 2023.3 for a while), the fallback can be removed
        val possibleUserId: TokenizableString = authStore.signedInUserId
            ?: recentUserActivityManager.getRecentUserActivity().lastUserUsed

        if (possibleUserId.value.isBlank()) {
            throw MissingArgumentForDownSyncScopeException("UserId required")
        }
        return when (possibleUserId) {
            is TokenizableString.Raw ->
                tokenizationProcessor
                    .encrypt(
                        decrypted = possibleUserId,
                        tokenKeyType = TokenKeyType.AttendantId,
                        project = configManager.getProject(projectId),
                    ).value
            is TokenizableString.Tokenized -> possibleUserId.value
        }
    }

    suspend fun insertOrUpdate(syncScopeOperation: EventDownSyncOperation) {
        downSyncOperationOperationDao.insertOrUpdate(
            buildFromEventsDownSyncOperationState(syncScopeOperation),
        )
    }

    suspend fun refreshState(syncScopeOperation: EventDownSyncOperation): EventDownSyncOperation {
        val uniqueOpId = syncScopeOperation.getUniqueKey()
        val state = downSyncOperationOperationDao.load().firstOrNull { it.id == uniqueOpId }

        return syncScopeOperation.copy(
            queryEvent = syncScopeOperation.queryEvent.copy(lastEventId = state?.lastEventId),
            lastEventId = state?.lastEventId,
            lastSyncTime = state?.lastUpdatedTime,
            state = state?.lastState,
        )
    }

    suspend fun deleteOperations(
        moduleIds: List<String>,
        modes: List<Modes>,
    ) {
        val scope = SubjectModuleScope(getProjectId(), moduleIds, modes)
        scope.operations.forEach {
            downSyncOperationOperationDao.delete(it.getUniqueKey())
        }
    }

    suspend fun deleteAll() {
        downSyncOperationOperationDao.deleteAll()
    }
}
