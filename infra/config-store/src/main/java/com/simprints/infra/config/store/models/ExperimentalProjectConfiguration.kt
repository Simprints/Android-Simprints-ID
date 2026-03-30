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

    val mfidLightingConditionsAssessmentEnabled: Boolean
        get() = customConfig
            ?.get(MFID_LIGHTING_CONDITIONS_ASSESSMENT_ENABLED)
            ?.jsonPrimitive
            ?.booleanOrNull
            .let { it == true }

    val mfidLightingConditionsAssessmentPadding: Int
        get() = customConfig
            ?.get(MFID_LIGHTING_CONDITIONS_ASSESSMENT_PADDING)
            ?.jsonPrimitive
            ?.intOrNull
            ?.coerceIn(
                MFID_LIGHTING_CONDITIONS_ASSESSMENT_PADDING_MIN,
                MFID_LIGHTING_CONDITIONS_ASSESSMENT_PADDING_MAX,
            )
            ?: MFID_LIGHTING_CONDITIONS_ASSESSMENT_PADDING_DEFAULT

    val mfidLightingConditionsAssessmentLowContrast: Int
        get() = customConfig
            ?.get(MFID_LIGHTING_CONDITIONS_ASSESSMENT_LOW_CONTRAST)
            ?.jsonPrimitive
            ?.intOrNull
            ?.coerceIn(
                MFID_LIGHTING_CONDITIONS_ASSESSMENT_LOW_CONTRAST_MIN,
                MFID_LIGHTING_CONDITIONS_ASSESSMENT_LOW_CONTRAST_MAX,
            )
            ?: MFID_LIGHTING_CONDITIONS_ASSESSMENT_LOW_CONTRAST_DEFAULT

    val mfidLightingConditionsAssessmentLowBrightness: Int
        get() = customConfig
            ?.get(MFID_LIGHTING_CONDITIONS_ASSESSMENT_LOW_BRIGHTNESS)
            ?.jsonPrimitive
            ?.intOrNull
            ?.coerceIn(
                MFID_LIGHTING_CONDITIONS_ASSESSMENT_LOW_BRIGHTNESS_MIN,
                MFID_LIGHTING_CONDITIONS_ASSESSMENT_LOW_BRIGHTNESS_MAX,
            )
            ?: MFID_LIGHTING_CONDITIONS_ASSESSMENT_LOW_BRIGHTNESS_DEFAULT

    val mfidLightingConditionsAssessmentHighBrightness: Int
        get() = customConfig
            ?.get(MFID_LIGHTING_CONDITIONS_ASSESSMENT_HIGH_BRIGHTNESS)
            ?.jsonPrimitive
            ?.intOrNull
            ?.coerceIn(
                MFID_LIGHTING_CONDITIONS_ASSESSMENT_HIGH_BRIGHTNESS_MIN,
                MFID_LIGHTING_CONDITIONS_ASSESSMENT_HIGH_BRIGHTNESS_MAX,
            )
            ?: MFID_LIGHTING_CONDITIONS_ASSESSMENT_HIGH_BRIGHTNESS_DEFAULT

    val mfidLightingConditionsAssessmentGlareBrightness: Int
        get() = customConfig
            ?.get(MFID_LIGHTING_CONDITIONS_ASSESSMENT_GLARE_BRIGHTNESS)
            ?.jsonPrimitive
            ?.intOrNull
            ?.coerceIn(
                MFID_LIGHTING_CONDITIONS_ASSESSMENT_GLARE_BRIGHTNESS_MIN,
                MFID_LIGHTING_CONDITIONS_ASSESSMENT_GLARE_BRIGHTNESS_MAX,
            )
            ?: MFID_LIGHTING_CONDITIONS_ASSESSMENT_GLARE_BRIGHTNESS_DEFAULT

    val mfidLightingConditionsAssessmentGlareSensitivity: Int
        get() = customConfig
            ?.get(MFID_LIGHTING_CONDITIONS_ASSESSMENT_GLARE_SENSITIVITY)
            ?.jsonPrimitive
            ?.intOrNull
            ?.coerceIn(
                MFID_LIGHTING_CONDITIONS_ASSESSMENT_GLARE_SENSITIVITY_MIN,
                MFID_LIGHTING_CONDITIONS_ASSESSMENT_GLARE_SENSITIVITY_MAX,
            )
            ?: MFID_LIGHTING_CONDITIONS_ASSESSMENT_GLARE_SENSITIVITY_DEFAULT

    val minimumFreeSpaceMb: Int
        get() = customConfig
            ?.get(MINIMUM_FREE_SPACE_MB)
            ?.jsonPrimitive
            ?.intOrNull
            ?.coerceAtLeast(0)
            ?: MINIMUM_FREE_SPACE_MB_DEFAULT

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

        const val MFID_LIGHTING_CONDITIONS_ASSESSMENT_ENABLED = "mfidLightingConditionsAssessmentEnabled"

        const val MFID_LIGHTING_CONDITIONS_ASSESSMENT_PADDING = "mfidLightingConditionsAssessmentPadding"
        internal const val MFID_LIGHTING_CONDITIONS_ASSESSMENT_PADDING_DEFAULT = 5
        internal const val MFID_LIGHTING_CONDITIONS_ASSESSMENT_PADDING_MIN = 0
        internal const val MFID_LIGHTING_CONDITIONS_ASSESSMENT_PADDING_MAX = 30

        const val MFID_LIGHTING_CONDITIONS_ASSESSMENT_LOW_CONTRAST = "mfidLightingConditionsAssessmentLowContrast"
        internal const val MFID_LIGHTING_CONDITIONS_ASSESSMENT_LOW_CONTRAST_DEFAULT = 30
        internal const val MFID_LIGHTING_CONDITIONS_ASSESSMENT_LOW_CONTRAST_MIN = 0
        internal const val MFID_LIGHTING_CONDITIONS_ASSESSMENT_LOW_CONTRAST_MAX = 50

        const val MFID_LIGHTING_CONDITIONS_ASSESSMENT_LOW_BRIGHTNESS = "mfidLightingConditionsAssessmentLowBrightness"
        internal const val MFID_LIGHTING_CONDITIONS_ASSESSMENT_LOW_BRIGHTNESS_DEFAULT = 25
        internal const val MFID_LIGHTING_CONDITIONS_ASSESSMENT_LOW_BRIGHTNESS_MIN = 0
        internal const val MFID_LIGHTING_CONDITIONS_ASSESSMENT_LOW_BRIGHTNESS_MAX = 50

        const val MFID_LIGHTING_CONDITIONS_ASSESSMENT_HIGH_BRIGHTNESS = "mfidLightingConditionsAssessmentHighBrightness"
        internal const val MFID_LIGHTING_CONDITIONS_ASSESSMENT_HIGH_BRIGHTNESS_DEFAULT = 95
        internal const val MFID_LIGHTING_CONDITIONS_ASSESSMENT_HIGH_BRIGHTNESS_MIN = 50
        internal const val MFID_LIGHTING_CONDITIONS_ASSESSMENT_HIGH_BRIGHTNESS_MAX = 100

        const val MFID_LIGHTING_CONDITIONS_ASSESSMENT_GLARE_BRIGHTNESS = "mfidLightingConditionsAssessmentGlareBrightness"
        internal const val MFID_LIGHTING_CONDITIONS_ASSESSMENT_GLARE_BRIGHTNESS_DEFAULT = 99
        internal const val MFID_LIGHTING_CONDITIONS_ASSESSMENT_GLARE_BRIGHTNESS_MIN = 50
        internal const val MFID_LIGHTING_CONDITIONS_ASSESSMENT_GLARE_BRIGHTNESS_MAX = 100

        const val MFID_LIGHTING_CONDITIONS_ASSESSMENT_GLARE_SENSITIVITY = "mfidLightingConditionsAssessmentGlareSensitivity"
        internal const val MFID_LIGHTING_CONDITIONS_ASSESSMENT_GLARE_SENSITIVITY_DEFAULT = 6
        internal const val MFID_LIGHTING_CONDITIONS_ASSESSMENT_GLARE_SENSITIVITY_MIN = 1
        internal const val MFID_LIGHTING_CONDITIONS_ASSESSMENT_GLARE_SENSITIVITY_MAX = 20

        const val MINIMUM_FREE_SPACE_MB = "minimumFreeSpaceMb"
        const val MINIMUM_FREE_SPACE_MB_DEFAULT = 1024 // 1GB
    }
}
