package com.simprints.face.controllers.core.flow

import com.simprints.core.domain.common.FlowProvider
import javax.inject.Inject

class MasterFlowManagerImpl @Inject constructor(private val flowProvider: FlowProvider) :
    MasterFlowManager {

    override fun getCurrentAction(): Action =
        when (flowProvider.getCurrentFlow()) {
            FlowProvider.FlowType.ENROL -> Action.ENROL
            FlowProvider.FlowType.IDENTIFY -> Action.IDENTIFY
            FlowProvider.FlowType.VERIFY -> Action.VERIFY
        }
}
