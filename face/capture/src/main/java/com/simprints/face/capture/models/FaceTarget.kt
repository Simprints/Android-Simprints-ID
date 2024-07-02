package com.simprints.face.capture.models

import com.simprints.face.infra.basebiosdk.detection.Face
import kotlin.math.abs

internal data class FaceTarget(
    val yawTarget: Target,
    val rollTarget: Target,
    val areaRange: ClosedRange<Float>,
) {
    operator fun contains(face: Face): Boolean =
        face.roll in rollTarget && face.yaw in yawTarget
}

internal fun interface Target {
    operator fun contains(actualValue: Float): Boolean
}

internal data class SymmetricTarget(val value: Float) : Target {
    override operator fun contains(actualValue: Float): Boolean =
        abs(actualValue) < value
}
