package com.simprints.id.activities.orchestrator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.orchestrator.OrchestratorManager
import com.simprints.id.tools.TimeHelper

class OrchestratorViewModelFactory(private val orchestratorManager: OrchestratorManager,
                                   private val preferencesManager: PreferencesManager,
                                   private val sessionEventsManager: SessionEventsManager,
                                   private val timeHelper: TimeHelper) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        OrchestratorViewModel(orchestratorManager, preferencesManager, sessionEventsManager, timeHelper) as T
}
