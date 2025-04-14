package com.simprints.document.capture.models

import org.w3c.dom.Document
import kotlin.math.abs

internal data class DocumentTarget(
    val yawTarget: Target,
    val rollTarget: Target,
    val areaRange: ClosedRange<Float>,
) {
    operator fun contains(document: Document): Boolean = document.roll in rollTarget && document.yaw in yawTarget
}

internal fun interface Target {
    operator fun contains(actualValue: Float): Boolean
}

internal data class SymmetricTarget(
    val value: Float,
) : Target {
    override operator fun contains(actualValue: Float): Boolean = abs(actualValue) < value
}
