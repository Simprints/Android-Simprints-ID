package com.simprints.uicomponents.models

/**
 * A range of values of type `Float`.
 */
public class FloatRange(
    override val start: Float,
    override val endInclusive: Float
) : ClosedRange<Float>
