package com.simprints.fingerprint.controllers.core.flow

import com.simprints.core.domain.common.FlowProvider
import com.simprints.core.domain.common.FlowProvider.FlowType.*

class MasterFlowManagerImpl(private val flowProvider: FlowProvider) : MasterFlowManager {

    override fun getCurrentAction(): Action =
        when (flowProvider.getCurrentFlow()) {
            ENROL -> Action.ENROL
            IDENTIFY -> Action.IDENTIFY
            VERIFY -> Action.VERIFY
        }
}
