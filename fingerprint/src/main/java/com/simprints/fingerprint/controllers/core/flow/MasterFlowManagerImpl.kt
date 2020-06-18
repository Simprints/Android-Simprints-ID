package com.simprints.fingerprint.controllers.core.flow

import com.simprints.id.orchestrator.FlowProvider
import com.simprints.id.orchestrator.FlowProvider.FlowType.*

class MasterFlowManagerImpl(private val flowProvider: FlowProvider) : MasterFlowManager {

    override fun getCurrentAction(): Action =
        when (flowProvider.getCurrentFlow()) {
            ENROL -> Action.ENROL
            IDENTIFY -> Action.IDENTIFY
            VERIFY -> Action.VERIFY
        }
}
