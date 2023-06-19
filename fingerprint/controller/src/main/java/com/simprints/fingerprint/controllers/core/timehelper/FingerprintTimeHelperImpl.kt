package com.simprints.fingerprint.controllers.core.timehelper

import com.simprints.core.tools.time.TimeHelper
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * This is a helper class that provides time tracing functionality
 */
class FingerprintTimeHelperImpl @Inject constructor(private val timeHelper: TimeHelper) :
    FingerprintTimeHelper {

    override fun now(): Long = timeHelper.now()
    override fun nowMinus(duration: Long, unit: TimeUnit): Long =
        timeHelper.nowMinus(duration, unit)

    override fun msBetweenNowAndTime(time: Long): Long = timeHelper.msBetweenNowAndTime(time)

    override fun newTimer(): Timer = Timer()
}