package com.simprints.id.tools

interface TimeHelper {

    fun now(): Long
    fun msBetweenNowAndTime(time: Long): Long
}
