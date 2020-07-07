package com.simprints.id.activities.orchestrator

import android.content.Intent
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.event.SessionRepository
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.app.DomainToModuleApiAppResponse
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.orchestrator.OrchestratorManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class OrchestratorViewModel(
    private val orchestratorManager: OrchestratorManager,
    private val orchestratorEventsHelper: OrchestratorEventsHelper,
    private val modalities: List<Modality>,
    private val sessionRepository: SessionRepository,
    private val domainToModuleApiConverter: DomainToModuleApiAppResponse,
    private val crashReportManager: CrashReportManager
) : ViewModel() {

    val ongoingStep = orchestratorManager.ongoingStep

    val appResponse = Transformations.map(orchestratorManager.appResponse) {
        it?.let {
            orchestratorEventsHelper.addCallbackEventInSessions(it)
            domainToModuleApiConverter.fromDomainModuleApiAppResponse(it)
        }
    }

    suspend fun startModalityFlow(appRequest: AppRequest) {
        orchestratorManager.initialise(
            modalities,
            appRequest,
            getCurrentSessionId())

    }

    private suspend fun getCurrentSessionId(): String =
        sessionRepository.getCurrentSession().id


    fun onModalStepRequestDone(appRequest: AppRequest, requestCode: Int, resultCode: Int, data: Intent?) {
        viewModelScope.launch {
            orchestratorManager.handleIntentResult(appRequest, requestCode, resultCode, data)
        }
    }

    fun restoreState() {
        runBlocking {
            orchestratorManager.restoreState()
        }
    }

    fun clearState() {
        orchestratorManager.clearState()
    }

    fun saveState() {
        orchestratorManager.saveState()
    }
}
