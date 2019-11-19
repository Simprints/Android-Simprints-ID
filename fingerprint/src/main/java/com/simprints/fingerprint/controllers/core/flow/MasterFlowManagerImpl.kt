package com.simprints.fingerprint.controllers.core.flow

import com.simprints.id.domain.moduleapi.app.requests.AppRequestType
import com.simprints.id.orchestrator.FlowProvider

class MasterFlowManagerImpl(private val flowProvider: FlowProvider) : MasterFlowManager {

    override fun getCurrentAction(): Action =
        when (flowProvider.getCurrentFlow()) {
            AppRequestType.ENROL -> Action.ENROL
            AppRequestType.IDENTIFY -> Action.IDENTIFY
            AppRequestType.VERIFY -> Action.VERIFY
        }
}
