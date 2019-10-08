package com.simprints.id.orchestrator

import com.simprints.id.domain.moduleapi.app.requests.AppRequestType

class FlowManagerImpl : FlowManager {

    private var currentFlow: AppRequestType = AppRequestType.ENROL

    override fun getCurrentFlow(): AppRequestType = currentFlow

    override fun setCurrentFlow(currentFlow: AppRequestType) {
        this.currentFlow = currentFlow
    }

}
