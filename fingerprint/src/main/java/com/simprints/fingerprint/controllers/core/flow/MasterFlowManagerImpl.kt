package com.simprints.fingerprint.controllers.core.flow

import com.simprints.core.domain.common.FlowProvider
import com.simprints.core.domain.common.FlowProvider.FlowType.*
import javax.inject.Inject

class MasterFlowManagerImpl @Inject constructor(private val flowProvider: FlowProvider) : MasterFlowManager {

    override fun getCurrentAction(): Action =
        when (flowProvider.getCurrentFlow()) {
            ENROL -> Action.ENROL
            IDENTIFY -> Action.IDENTIFY
            VERIFY -> Action.VERIFY
        }
}
