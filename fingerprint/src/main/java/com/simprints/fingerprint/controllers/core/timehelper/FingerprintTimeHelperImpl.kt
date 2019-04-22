package com.simprints.fingerprint.controllers.core.timehelper

import com.simprints.id.tools.TimeHelper
import java.util.concurrent.TimeUnit

class FingerprintTimeHelperImpl(private val timeHelper: TimeHelper) : FingerprintTimeHelper {

    override fun now(): Long = timeHelper.now()
    override fun nowMinus(duration: Long, unit: TimeUnit): Long = timeHelper.nowMinus(duration, unit)
    override fun msBetweenNowAndTime(time: Long): Long = timeHelper.msBetweenNowAndTime(time)
}
