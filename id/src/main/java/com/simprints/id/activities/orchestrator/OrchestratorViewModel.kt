package com.simprints.id.activities.orchestrator

import android.content.Intent
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.moduleapi.app.DomainToModuleApiAppResponse
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.orchestrator.OrchestratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OrchestratorViewModel(private val orchestratorManager: OrchestratorManager,
                            private val orchestratorEventsHelper: OrchestratorEventsHelper,
                            private val preferencesManager: PreferencesManager,
                            private val sessionEventsManager: SessionEventsManager,
                            private val domainToModuleApiConverter: DomainToModuleApiAppResponse) : ViewModel() {

    val nextActivity = orchestratorManager.onGoingStep

    val appResponse = Transformations.map(orchestratorManager.appResponse) {
        orchestratorEventsHelper.addCallbackEventInSessions(it)
        domainToModuleApiConverter.fromDomainModuleApiAppResponse(it)
    }

    fun start(appRequest: AppRequest) {
        CoroutineScope(Dispatchers.Main).launch {
            orchestratorManager.start(
                preferencesManager.modalities,
                appRequest,
                sessionEventsManager.getCurrentSession().map { it.id }.blockingGet())
        }
    }

    fun onModalStepRequestDone(requestCode: Int, resultCode: Int, data: Intent?) =
        CoroutineScope(Dispatchers.Main).launch {
            orchestratorManager.onModalStepRequestDone(requestCode, resultCode, data)
        }
}
