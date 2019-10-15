package com.simprints.id.orchestrator

import com.simprints.id.Application
import com.simprints.id.domain.moduleapi.app.requests.AppRequestType

interface FlowManager {

    fun getCurrentFlow(): AppRequestType

    companion object {
        fun build(app: Application) = app.component.getFlowManager()
    }

}
