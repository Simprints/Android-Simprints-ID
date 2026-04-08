package com.simprints.infra.sync.config.usecase

import com.simprints.core.DispatcherIO
import com.simprints.infra.authlogic.AuthManager
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.local.migration.RealmToRoomMigrationFlagsStore
import com.simprints.infra.sync.ScheduleCommand
import com.simprints.infra.sync.SyncOrchestrator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val syncOrchestrator: SyncOrchestrator,
    private val authManager: AuthManager,
    private val flagsStore: RealmToRoomMigrationFlagsStore,
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
    @param:DispatcherIO private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke() = withContext(ioDispatcher) {
        // Cancel all background sync
        syncOrchestrator.execute(ScheduleCommand.Everything.unschedule())
        syncOrchestrator.deleteEventSyncInfo()
        // Sign out the user and clear all local data
        authManager.signOut()
        // Reset migration flags
        flagsStore.clearMigrationFlags()
        enrolmentRecordRepository.closeOpenDbConnection()
    }
}
