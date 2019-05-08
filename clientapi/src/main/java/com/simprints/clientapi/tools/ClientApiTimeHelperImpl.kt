package com.simprints.clientapi.tools

import com.simprints.id.tools.TimeHelper

class ClientApiTimeHelperImpl(private val timeHelper: TimeHelper) : ClientApiTimeHelper {
    override fun now() = timeHelper.now()
}
