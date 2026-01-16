package com.simprints.infra.config.store.models

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

/**
 * Thin wrapper around custom project configuration to keep all the experimental
 * feature definitions in a single place and make calls explicit and type-safe.
 */
data class ExperimentalProjectConfiguration(
    private val customConfig: Map<String, JsonElement>?,
) {
    val idPoolValidationEnabled: Boolean
        get() = customConfig
            ?.get(ENABLE_ID_POOL_VALIDATION)
            ?.jsonPrimitive
            ?.booleanOrNull
            .let { it == true }

    val singleQualityFallbackRequired: Boolean
        get() = customConfig
            ?.get(SINGLE_GOOD_QUALITY_FALLBACK_REQUIRED)
            ?.jsonPrimitive
            ?.booleanOrNull
            .let { it == true }

    val faceAutoCaptureEnabled: Boolean
        get() = customConfig
            ?.get(FACE_AUTO_CAPTURE_ENABLED)
            ?.jsonPrimitive
            ?.booleanOrNull
            .let { it == true }

    val faceAutoCaptureImagingDurationMillis: Long
        get() = customConfig
            ?.get(FACE_AUTO_CAPTURE_IMAGING_DURATION_MILLIS)
            ?.jsonPrimitive
            ?.longOrNull
            ?.coerceIn(FACE_AUTO_CAPTURE_IMAGING_DURATION_MILLIS_MIN, FACE_AUTO_CAPTURE_IMAGING_DURATION_MILLIS_MAX)
            ?: FACE_AUTO_CAPTURE_IMAGING_DURATION_MILLIS_DEFAULT

    val recordsDbMigrationFromRealmEnabled: Boolean
        get() = customConfig
            ?.get(RECORDS_DB_MIGRATION_FROM_REALM_TO_ROOM_ENABLED)
            ?.jsonPrimitive
            ?.booleanOrNull
            .let { it == true }

    val recordsDbMigrationFromRealmMaxRetries: Int
        get() = customConfig
            ?.get(RECORDS_DB_MIGRATION_FROM_REALM_TO_ROOM_MAX_RETRIES)
            ?.jsonPrimitive
            ?.intOrNull
            ?: RECORDS_DB_MIGRATION_FROM_REALM_TO_ROOM_DEFAULT_MAX_RETRIES

    val sampleUploadWithSignedUrlEnabled: Boolean
        get() = customConfig
            ?.get(SAMPLE_UPLOAD_WITH_URL_ENABLED)
            ?.jsonPrimitive
            ?.booleanOrNull
            .let { it == true }

    val displayCameraFlashToggle: Boolean
        get() = customConfig
            ?.get(CAMERA_FLASH_CONTROLS_ENABLED)
            ?.jsonPrimitive
            ?.booleanOrNull
            .let { it == true }

    val fallbackToCommCareThresholdDays: Long
        get() = customConfig
            ?.get(FALLBACK_TO_COMMCARE_THRESHOLD_DAYS)
            ?.jsonPrimitive
            ?.longOrNull
            ?: FALLBACK_TO_COMMCARE_THRESHOLD_DAYS_DEFAULT

    val ocrUseHighRes: Boolean
        get() = customConfig
            ?.get(OCR_USE_HIGH_RES)
            ?.jsonPrimitive
            ?.booleanOrNull
            ?: OCR_USE_HIGH_RES_DEFAULT

    val ocrCaptures: Int
        get() = customConfig
            ?.get(OCR_CAPTURES)
            ?.jsonPrimitive
            ?.intOrNull
            ?: OCR_CAPTURES_DEFAULT

    val allowConfirmingGuidsNotInCallback: Boolean
        get() = customConfig
            ?.get(ALLOW_CONFIRMING_GUIDS_NOT_IN_CALLBACK)
            ?.jsonPrimitive
            ?.booleanOrNull
            .let { it == true }

    companion object {
        internal const val ENABLE_ID_POOL_VALIDATION = "validateIdentificationPool"
        internal const val SINGLE_GOOD_QUALITY_FALLBACK_REQUIRED = "singleQualityFallbackRequired"
        internal const val FACE_AUTO_CAPTURE_ENABLED = "faceAutoCaptureEnabled"
        internal const val FACE_AUTO_CAPTURE_IMAGING_DURATION_MILLIS = "faceAutoCaptureImagingDurationMillis"

        internal const val RECORDS_DB_MIGRATION_FROM_REALM_TO_ROOM_ENABLED = "recordsDbMigrationFromRealmEnabled"
        const val RECORDS_DB_MIGRATION_FROM_REALM_TO_ROOM_MAX_RETRIES = "recordsDbMigrationFromRealmMaxRetries"
        internal const val RECORDS_DB_MIGRATION_FROM_REALM_TO_ROOM_DEFAULT_MAX_RETRIES = 10
        internal const val FACE_AUTO_CAPTURE_IMAGING_DURATION_MILLIS_MIN = 1L
        const val FACE_AUTO_CAPTURE_IMAGING_DURATION_MILLIS_DEFAULT = 3_000L
        internal const val FACE_AUTO_CAPTURE_IMAGING_DURATION_MILLIS_MAX = 60_000L

        internal const val SAMPLE_UPLOAD_WITH_URL_ENABLED = "sampleUploadWithSignedUrl"

        internal const val CAMERA_FLASH_CONTROLS_ENABLED = "displayCameraFlashToggle"

        internal const val FALLBACK_TO_COMMCARE_THRESHOLD_DAYS = "fallbackToCommCareThresholdDays"
        internal const val FALLBACK_TO_COMMCARE_THRESHOLD_DAYS_DEFAULT = 5L

        internal const val OCR_USE_HIGH_RES = "ocrHighRes"
        internal const val OCR_USE_HIGH_RES_DEFAULT = true
        internal const val OCR_CAPTURES = "ocrCaptures"
        internal const val OCR_CAPTURES_DEFAULT = 3

        const val ALLOW_CONFIRMING_GUIDS_NOT_IN_CALLBACK = "allowConfirmingGuidsNotInCallback"
    }
}
