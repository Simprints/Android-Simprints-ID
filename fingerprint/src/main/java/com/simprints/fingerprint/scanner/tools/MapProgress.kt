package com.simprints.fingerprint.scanner.tools

/**
 * map a progress value from the interval [0.0, 1.0] to [min, max] linearly
 */
fun Float.mapProgress(min: Float, max: Float): Float =
    min + (max - min) * this
