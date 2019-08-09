package com.simprints.id.activities.orchestrator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.moduleapi.app.DomainToModuleApiAppResponse
import com.simprints.id.orchestrator.OrchestratorManager

class OrchestratorViewModelFactory(private val orchestratorManager: OrchestratorManager,
                                   private val orchestratorEventsHelper: OrchestratorEventsHelper,
                                   private val preferencesManager: PreferencesManager,
                                   private val sessionEventsManager: SessionEventsManager,
                                   private val domainToModuleApiConverter: DomainToModuleApiAppResponse) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        OrchestratorViewModel(orchestratorManager, orchestratorEventsHelper, preferencesManager, sessionEventsManager, domainToModuleApiConverter) as T
}
