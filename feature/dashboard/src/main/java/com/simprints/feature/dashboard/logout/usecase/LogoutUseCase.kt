package com.simprints.feature.dashboard.logout.usecase

import com.simprints.core.DispatcherIO
import com.simprints.infra.authlogic.AuthManager
import com.simprints.infra.enrolment.records.repository.local.migration.RealmToRoomMigrationFlagsStore
import com.simprints.infra.sync.SyncOrchestrator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class LogoutUseCase @Inject constructor(
    private val syncOrchestrator: SyncOrchestrator,
    private val authManager: AuthManager,
    private val flagsStore: RealmToRoomMigrationFlagsStore,
    @DispatcherIO private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke() = withContext(ioDispatcher) {
        // Cancel all background sync
        syncOrchestrator.cancelBackgroundWork()
        syncOrchestrator.deleteEventSyncInfo()
        // sign out the user
        authManager.signOut()
        // Reset migration flags
        flagsStore.clearMigrationFlags()
    }
}
