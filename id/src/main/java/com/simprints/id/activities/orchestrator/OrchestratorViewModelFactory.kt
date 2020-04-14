package com.simprints.id.activities.orchestrator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.DomainToModuleApiAppResponse
import com.simprints.id.orchestrator.OrchestratorManager

class OrchestratorViewModelFactory(private val orchestratorManager: OrchestratorManager,
                                   private val orchestratorEventsHelper: OrchestratorEventsHelper,
                                   private val modalities: List<Modality>,
                                   private val sessionRepository: SessionRepository,
                                   private val domainToModuleApiConverter: DomainToModuleApiAppResponse,
                                   private val crashReportManager: CrashReportManager) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        OrchestratorViewModel(orchestratorManager, orchestratorEventsHelper, modalities, sessionRepository, domainToModuleApiConverter, crashReportManager) as T
}
