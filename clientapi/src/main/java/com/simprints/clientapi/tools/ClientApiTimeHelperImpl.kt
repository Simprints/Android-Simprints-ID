package com.simprints.clientapi.tools

import com.simprints.core.tools.time.TimeHelper

class ClientApiTimeHelperImpl(private val timeHelper: TimeHelper) : ClientApiTimeHelper {
    override fun now() = timeHelper.now()
}
