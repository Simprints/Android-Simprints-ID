package com.simprints.id.activities.orchestrator

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.DispatcherIO
import com.simprints.eventsystem.event.EventRepository
import com.simprints.id.domain.moduleapi.app.DomainToModuleApiAppResponse
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.orchestrator.OrchestratorManager
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.SynchronizationConfiguration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrchestratorViewModel @Inject constructor(
    private val orchestratorManager: OrchestratorManager,
    private val orchestratorEventsHelper: OrchestratorEventsHelper,
    private val configManager: ConfigManager,
    private val eventRepository: EventRepository,
    private val domainToModuleApiConverter: DomainToModuleApiAppResponse,
    @DispatcherIO private val dispatcher: CoroutineDispatcher
) : ViewModel() {

    val syncFrequency = MutableLiveData<SynchronizationConfiguration.Frequency>()
    val ongoingStep = orchestratorManager.ongoingStep

    val appResponse = Transformations.map(orchestratorManager.appResponse) {
        it?.let {
            orchestratorEventsHelper.addCallbackEventInSessions(it)
            domainToModuleApiConverter.fromDomainModuleApiAppResponse(it)
        }
    }

    init {
        viewModelScope.launch(dispatcher) {
            syncFrequency.postValue(configManager.getProjectConfiguration().synchronization.frequency)
        }
    }

    /**
     * Starts or restore a modality flow
     *
     * @param appRequest
     * @param shouldRestoreState
     */
    fun startOrRestoreModalityFlow(appRequest: AppRequest, shouldRestoreState: Boolean) =
        viewModelScope.launch {
            val projectConfiguration = configManager.getProjectConfiguration()
            orchestratorManager.initialise(
                projectConfiguration.general.modalities,
                appRequest,
                getCurrentSessionId()
            )
            if (shouldRestoreState) {
                restoreState()
            } else {
                startModalityFlow()
            }
        }


    private suspend fun startModalityFlow() =
        orchestratorManager.startModalityFlow()


    private suspend fun getCurrentSessionId(): String =
        eventRepository.getCurrentCaptureSessionEvent().id


    fun onModalStepRequestDone(
        appRequest: AppRequest,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        viewModelScope.launch {
            orchestratorManager.handleIntentResult(appRequest, requestCode, resultCode, data)
        }
    }

    private suspend fun restoreState() =
        orchestratorManager.restoreState()


    fun saveState() {
        orchestratorManager.saveState()
    }
}
