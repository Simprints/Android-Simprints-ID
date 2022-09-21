package com.simprints.clientapi.tools

import com.simprints.core.tools.time.TimeHelper
import javax.inject.Inject

class ClientApiTimeHelperImpl @Inject constructor(private val timeHelper: TimeHelper) :
    ClientApiTimeHelper {
    override fun now() = timeHelper.now()
}
