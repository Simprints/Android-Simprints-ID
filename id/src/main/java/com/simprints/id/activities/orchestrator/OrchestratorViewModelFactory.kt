package com.simprints.id.activities.orchestrator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.eventsystem.event.EventRepository
import com.simprints.id.domain.moduleapi.app.DomainToModuleApiAppResponse
import com.simprints.id.orchestrator.OrchestratorManager
import com.simprints.infra.config.ConfigManager

class OrchestratorViewModelFactory(
    private val orchestratorManager: OrchestratorManager,
    private val orchestratorEventsHelper: OrchestratorEventsHelper,
    private val configManager: ConfigManager,
    private val eventRepository: EventRepository,
    private val domainToModuleApiConverter: DomainToModuleApiAppResponse,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        OrchestratorViewModel(
            orchestratorManager,
            orchestratorEventsHelper,
            configManager,
            eventRepository,
            domainToModuleApiConverter
        ) as T
}
