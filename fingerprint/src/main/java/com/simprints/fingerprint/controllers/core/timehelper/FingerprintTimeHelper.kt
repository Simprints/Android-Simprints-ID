package com.simprints.fingerprint.controllers.core.timehelper

import java.util.*
import java.util.concurrent.TimeUnit

/**
 * This interface represents a  time helper for fingerprint module
 */
interface FingerprintTimeHelper {

    fun now(): Long
    fun nowMinus(duration: Long, unit: TimeUnit = TimeUnit.MILLISECONDS): Long
    fun msBetweenNowAndTime(time: Long): Long

    fun newTimer(): Timer
}
