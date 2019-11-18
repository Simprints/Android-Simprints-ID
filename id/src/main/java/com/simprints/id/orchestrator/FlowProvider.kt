package com.simprints.id.orchestrator

import com.simprints.id.Application
import com.simprints.id.domain.moduleapi.app.requests.AppRequestType

interface FlowProvider {

    fun getCurrentFlow(): AppRequestType

    companion object {
        fun build(app: Application) = app.orchestratorComponent.getFlowManager()
    }
}
