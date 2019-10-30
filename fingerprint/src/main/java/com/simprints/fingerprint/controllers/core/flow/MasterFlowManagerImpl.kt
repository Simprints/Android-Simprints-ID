package com.simprints.fingerprint.controllers.core.flow

import com.simprints.id.domain.moduleapi.app.requests.AppRequestType
import com.simprints.id.orchestrator.FlowManager

class MasterFlowManagerImpl(private val flowManager: FlowManager) : MasterFlowManager {

    override fun getCurrentAction(): Action =
        when (flowManager.getCurrentFlow()) {
            AppRequestType.ENROL -> Action.ENROL
            AppRequestType.IDENTIFY -> Action.IDENTIFY
            AppRequestType.VERIFY -> Action.VERIFY
        }
}
