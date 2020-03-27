package com.simprints.face.models

import com.simprints.face.detection.Face
import com.simprints.uicomponents.models.FloatRange
import kotlin.math.abs

data class FaceTarget(
    val yawTarget: Target,
    val rollTarget: Target,
    val areaRange: FloatRange
) {
    operator fun contains(face: Face): Boolean =
        face.roll in rollTarget &&
                face.yaw in yawTarget
}

interface Target {
    operator fun contains(actualValue: Float): Boolean
}

data class SymmetricTarget(val value: Float) : Target {
    override operator fun contains(actualValue: Float): Boolean =
        abs(actualValue) < value
}

data class AsymmetricTarget(val startValue: Float, val endValueInclusive: Float) : Target {
    override fun contains(actualValue: Float): Boolean =
        actualValue > startValue && actualValue <= endValueInclusive
}
