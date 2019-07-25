package com.simprints.fingerprint.controllers.core.timehelper

import java.util.concurrent.TimeUnit

interface FingerprintTimeHelper {

    fun now(): Long
    fun nowMinus(duration: Long, unit: TimeUnit = TimeUnit.MILLISECONDS): Long
    fun msBetweenNowAndTime(time: Long): Long
}
