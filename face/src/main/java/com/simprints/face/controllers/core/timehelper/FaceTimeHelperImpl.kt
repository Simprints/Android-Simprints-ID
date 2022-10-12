package com.simprints.face.controllers.core.timehelper

import com.simprints.core.tools.time.TimeHelper
import javax.inject.Inject

class FaceTimeHelperImpl @Inject constructor(private val timeHelper: TimeHelper) : FaceTimeHelper {
    override fun now(): Long = timeHelper.now()
}
