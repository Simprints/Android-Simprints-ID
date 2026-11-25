package com.simprints.feature.troubleshooting.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.simprints.core.DispatcherIO
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.events.EventRepository
import com.simprints.infra.eventsync.ResetDownSyncInfoUseCase
import com.simprints.infra.sync.OneTime
import com.simprints.infra.sync.ScheduleCommand
import com.simprints.infra.sync.SyncOrchestrator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class DebugViewModel @Inject constructor(
    private val syncOrchestrator: SyncOrchestrator,
    private val resetDownSyncInfo: ResetDownSyncInfoUseCase,
    private val authStore: AuthStore,
    private val eventRepository: EventRepository,
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
    private val workManager: WorkManager,
    @param:DispatcherIO private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    fun clearFirebaseToken() {
        authStore.clearFirebaseToken()
    }

    fun cleanAll() {
        viewModelScope.launch(ioDispatcher) {
            syncOrchestrator.execute(OneTime.Events.stop())
            syncOrchestrator.execute(ScheduleCommand.Events.unschedule())

            eventRepository.deleteAll()
            resetDownSyncInfo()
            enrolmentRecordRepository.deleteAll()
            workManager.pruneWork()
        }
    }
}
