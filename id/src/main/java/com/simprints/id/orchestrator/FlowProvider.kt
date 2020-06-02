package com.simprints.id.orchestrator

import com.simprints.id.Application

interface FlowProvider {

    enum class FlowType {
        ENROL,
        IDENTIFY,
        VERIFY
    }

    fun getCurrentFlow(): FlowType

    companion object {
        fun build(app: Application) = app.orchestratorComponent.getFlowManager()
    }
}
