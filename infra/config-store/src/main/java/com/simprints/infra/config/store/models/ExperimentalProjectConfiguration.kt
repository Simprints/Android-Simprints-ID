package com.simprints.infra.config.store.models

/**
 * Thin wrapper around custom project configuration to keep all the experimental
 * feature definitions in a single place and make calls explicit and type-safe.
 */
data class ExperimentalProjectConfiguration(
    private val customConfig: Map<String, Any>?,
) {
    val idPoolValidationEnabled: Boolean
        get() = customConfig
            ?.get(ENABLE_ID_POOL_VALIDATION)
            ?.let { it as? Boolean }
            .let { it == true }

    val singleQualityFallbackRequired: Boolean
        get() = customConfig
            ?.get(SINGLE_GOOD_QUALITY_FALLBACK_REQUIRED)
            ?.let { it as? Boolean }
            .let { it == true }

    val faceAutoCaptureEnabled: Boolean
        get() = customConfig
            ?.get(FACE_AUTO_CAPTURE_ENABLED)
            ?.let { it as? Boolean }
            .let { it == true }

    val faceAutoCaptureImagingDurationMillis: Long
        get() = customConfig
            ?.get(FACE_AUTO_CAPTURE_IMAGING_DURATION_MILLIS)
            ?.let { it as? Int }
            ?.toLong()
            ?.coerceIn(FACE_AUTO_CAPTURE_IMAGING_DURATION_MILLIS_MIN, FACE_AUTO_CAPTURE_IMAGING_DURATION_MILLIS_MAX)
            ?: FACE_AUTO_CAPTURE_IMAGING_DURATION_MILLIS_DEFAULT

    companion object {
        internal const val ENABLE_ID_POOL_VALIDATION = "validateIdentificationPool"
        internal const val SINGLE_GOOD_QUALITY_FALLBACK_REQUIRED = "singleQualityFallbackRequired"
        internal const val FACE_AUTO_CAPTURE_ENABLED = "faceAutoCaptureEnabled"
        internal const val FACE_AUTO_CAPTURE_IMAGING_DURATION_MILLIS = "faceAutoCaptureImagingDurationMillis"

        const val FACE_AUTO_CAPTURE_IMAGING_DURATION_MILLIS_MIN = 1L
        const val FACE_AUTO_CAPTURE_IMAGING_DURATION_MILLIS_DEFAULT = 3_000L
        const val FACE_AUTO_CAPTURE_IMAGING_DURATION_MILLIS_MAX = 60_000L
    }
}
