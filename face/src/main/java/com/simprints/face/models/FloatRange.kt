package com.simprints.face.models

/**
 * A range of values of type `Float`.
 */
class FloatRange(
    override val start: Float,
    override val endInclusive: Float
) : ClosedRange<Float>
