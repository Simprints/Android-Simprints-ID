package com.simprints.feature.dashboard.logout.usecase

import com.simprints.core.DispatcherIO
import com.simprints.infra.authlogic.AuthManager
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.local.migration.RealmToRoomMigrationFlagsStore
import com.simprints.infra.sync.ScheduleCommand
import com.simprints.infra.sync.SyncOrchestrator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

internal class LogoutUseCase @Inject constructor(
    private val syncOrchestrator: SyncOrchestrator,
    private val authManager: AuthManager,
    private val flagsStore: RealmToRoomMigrationFlagsStore,
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
    @param:DispatcherIO private val ioDispatcher: CoroutineDispatcher,
) {
    // To prevent a race between wiping data and navigation, this use case must block the executing thread
    operator fun invoke() = runBlocking(ioDispatcher) {
        // Cancel all background sync
        syncOrchestrator.executeSchedulingCommand(ScheduleCommand.Everything.unschedule())
        syncOrchestrator.deleteEventSyncInfo()
        // sign out the user
        authManager.signOut()
        // Reset migration flags
        flagsStore.clearMigrationFlags()
        enrolmentRecordRepository.closeOpenDbConnection()
    }
}
