package com.simprints.infra.sync.logout

import com.simprints.core.DispatcherIO
import com.simprints.core.broadcasts.InternalBroadcaster
import com.simprints.infra.authlogic.AuthManager
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.local.migration.RealmToRoomMigrationFlagsStore
import com.simprints.infra.sync.ScheduleCommand
import com.simprints.infra.sync.SyncOrchestrator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class LogoutUseCase @Inject constructor(
    private val syncOrchestrator: SyncOrchestrator,
    private val authManager: AuthManager,
    private val flagsStore: RealmToRoomMigrationFlagsStore,
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
    private val broadcaster: InternalBroadcaster,
    @param:DispatcherIO private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(isProjectEnded: Boolean = false) = withContext(ioDispatcher) {
        // Cancel all background sync
        syncOrchestrator.execute(ScheduleCommand.Everything.unschedule())
        syncOrchestrator.deleteEventSyncInfo()
        // Sign out the user and clear all local data
        authManager.signOut()
        // Reset migration flags
        flagsStore.clearMigrationFlags()
        enrolmentRecordRepository.closeOpenDbConnection()
        // Notify UI to navigate to login
        broadcaster.loggedOut(isProjectEnded)
    }
}
