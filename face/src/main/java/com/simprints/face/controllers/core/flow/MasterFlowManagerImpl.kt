package com.simprints.face.controllers.core.flow

import com.simprints.id.orchestrator.FlowProvider

class MasterFlowManagerImpl(private val flowProvider: FlowProvider) : MasterFlowManager {

    override fun getCurrentAction(): Action =
        when (flowProvider.getCurrentFlow()) {
            FlowProvider.FlowType.ENROL -> Action.ENROL
            FlowProvider.FlowType.IDENTIFY -> Action.IDENTIFY
            FlowProvider.FlowType.VERIFY -> Action.VERIFY
        }
}
