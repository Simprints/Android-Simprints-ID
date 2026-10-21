package com.simprints.infra.camera.lighting

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports

/**
 * Luminance percentiles of an image region, each in 0..255.
 */
@ExcludedFromGeneratedTestCoverageReports("Data struct")
data class LightingStats(
    val p5: Int,
    val median: Int,
    val p95: Int,
) {
    /** Global contrast: P95 minus P5 luminance. */
    val contrast: Int get() = p95 - p5
}
