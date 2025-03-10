package com.simprints.ear.infra.basebiosdk.detection

/**
 * Face that represents a detected biometric target after
 *
 * @property sourceWidth bounding rectangle width
 * @property sourceHeight bounding rectangle height
 * @property quality image quality
 * @property template
 * @property format
 */
data class Ear(
    private val sourceWidth: Int,
    private val sourceHeight: Int,
    val quality: Float,
    val template: ByteArray,
    val format: String,
)
