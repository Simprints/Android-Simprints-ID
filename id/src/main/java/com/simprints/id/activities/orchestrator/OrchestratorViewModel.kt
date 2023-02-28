package com.simprints.id.activities.orchestrator

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.ExternalScope
import com.simprints.eventsystem.event.EventRepository
import com.simprints.id.domain.moduleapi.app.DomainToModuleApiAppResponse
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.orchestrator.OrchestratorManager
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.ProjectConfiguration
import com.simprints.infra.config.domain.models.SynchronizationConfiguration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrchestratorViewModel @Inject constructor(
    private val orchestratorManager: OrchestratorManager,
    private val orchestratorEventsHelper: OrchestratorEventsHelper,
    private val configManager: ConfigManager,
    private val eventRepository: EventRepository,
    private val domainToModuleApiConverter: DomainToModuleApiAppResponse,
    @ExternalScope private val externalScope: CoroutineScope,
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
        viewModelScope.launch {
            syncFrequency.postValue(configManager.getProjectConfiguration().synchronization.frequency)
        }
    }

    /**
     * All modality flow operations should wait for this initialization job to complete
     */
    private lateinit var modalityFlowInitJob: Job

    /**
     * Starts or restore a modality flow
     *
     * @param appRequest
     * @param shouldRestoreState
     */
    fun startOrRestoreModalityFlow(appRequest: AppRequest, shouldRestoreState: Boolean) =
        viewModelScope.launch {
            val projectConfiguration = configManager.getProjectConfiguration()
            initModalityFlow(projectConfiguration, appRequest)
            if (shouldRestoreState) {
                restoreState()
            } else {
                startModalityFlow()
            }
        }.also { modalityFlowInitJob = it }

    private suspend fun initModalityFlow(
        projectConfiguration: ProjectConfiguration,
        appRequest: AppRequest
    ) = orchestratorManager.initialise(
        projectConfiguration.general.modalities,
        appRequest,
        getCurrentSessionId()
    )

    private suspend fun startModalityFlow() =
        orchestratorManager.startModalityFlow()

    private suspend fun restoreState() =
        orchestratorManager.restoreState()

    private suspend fun getCurrentSessionId(): String =
        eventRepository.getCurrentCaptureSessionEvent().id

    fun onModalStepRequestDone(
        appRequest: AppRequest,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        viewModelScope.launch {
            modalityFlowInitJob.join()
            orchestratorManager.handleIntentResult(appRequest, requestCode, resultCode, data)
        }
    }

    /* Use externalScope to ensure saving completes even when called from onDestroy()
     */
    fun saveState() = externalScope.launch {
        modalityFlowInitJob.join()
        orchestratorManager.saveState()
    }
}
