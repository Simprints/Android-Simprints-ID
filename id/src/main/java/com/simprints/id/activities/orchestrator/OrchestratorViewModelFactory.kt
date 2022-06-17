package com.simprints.id.activities.orchestrator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.core.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.DomainToModuleApiAppResponse
import com.simprints.id.orchestrator.OrchestratorManager

class OrchestratorViewModelFactory(private val orchestratorManager: OrchestratorManager,
                                   private val orchestratorEventsHelper: OrchestratorEventsHelper,
                                   private val modalities: List<Modality>,
                                   private val eventRepository: com.simprints.eventsystem.event.EventRepository,
                                   private val domainToModuleApiConverter: DomainToModuleApiAppResponse,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        OrchestratorViewModel(orchestratorManager, orchestratorEventsHelper, modalities, eventRepository, domainToModuleApiConverter) as T
}
