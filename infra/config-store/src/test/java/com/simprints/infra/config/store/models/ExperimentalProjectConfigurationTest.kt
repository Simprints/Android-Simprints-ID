package com.simprints.infra.config.store.models

import com.google.common.truth.Truth.*
import com.simprints.infra.config.store.models.ExperimentalProjectConfiguration.Companion.CAMERA_FLASH_CONTROLS_ENABLED
import com.simprints.infra.config.store.models.ExperimentalProjectConfiguration.Companion.ENABLE_ID_POOL_VALIDATION
import com.simprints.infra.config.store.models.ExperimentalProjectConfiguration.Companion.FACE_AUTO_CAPTURE_ENABLED
import com.simprints.infra.config.store.models.ExperimentalProjectConfiguration.Companion.FACE_AUTO_CAPTURE_IMAGING_DURATION_MILLIS
import com.simprints.infra.config.store.models.ExperimentalProjectConfiguration.Companion.FACE_AUTO_CAPTURE_IMAGING_DURATION_MILLIS_DEFAULT
import com.simprints.infra.config.store.models.ExperimentalProjectConfiguration.Companion.FACE_AUTO_CAPTURE_IMAGING_DURATION_MILLIS_MAX
import com.simprints.infra.config.store.models.ExperimentalProjectConfiguration.Companion.FACE_AUTO_CAPTURE_IMAGING_DURATION_MILLIS_MIN
import com.simprints.infra.config.store.models.ExperimentalProjectConfiguration.Companion.FALLBACK_TO_COMMCARE_THRESHOLD_DAYS
import com.simprints.infra.config.store.models.ExperimentalProjectConfiguration.Companion.FALLBACK_TO_COMMCARE_THRESHOLD_DAYS_DEFAULT
import com.simprints.infra.config.store.models.ExperimentalProjectConfiguration.Companion.RECORDS_DB_MIGRATION_FROM_REALM_TO_ROOM_DEFAULT_MAX_RETRIES
import com.simprints.infra.config.store.models.ExperimentalProjectConfiguration.Companion.RECORDS_DB_MIGRATION_FROM_REALM_TO_ROOM_ENABLED
import com.simprints.infra.config.store.models.ExperimentalProjectConfiguration.Companion.RECORDS_DB_MIGRATION_FROM_REALM_TO_ROOM_MAX_RETRIES
import com.simprints.infra.config.store.models.ExperimentalProjectConfiguration.Companion.SAMPLE_UPLOAD_WITH_URL_ENABLED
import com.simprints.infra.config.store.models.ExperimentalProjectConfiguration.Companion.SINGLE_GOOD_QUALITY_FALLBACK_REQUIRED
import org.junit.Test

internal class ExperimentalProjectConfigurationTest {
    @Test
    fun `check pool validation flag correctly`() {
        mapOf(
            // Value not present
            emptyMap<String, Any>() to false,
            // Value not boolean
            mapOf(ENABLE_ID_POOL_VALIDATION to 1) to false,
            // Value present and FALSE
            mapOf(ENABLE_ID_POOL_VALIDATION to false) to false,
            // Value present and TRUE
            mapOf(ENABLE_ID_POOL_VALIDATION to true) to true,
        ).forEach { (config, result) ->
            assertThat(ExperimentalProjectConfiguration(config).idPoolValidationEnabled).isEqualTo(result)
        }
    }

    @Test
    fun `check single good face capture fallback flag correctly`() {
        mapOf(
            // Value not present
            emptyMap<String, Any>() to false,
            // Value not boolean
            mapOf(SINGLE_GOOD_QUALITY_FALLBACK_REQUIRED to 1) to false,
            // Value present and FALSE
            mapOf(SINGLE_GOOD_QUALITY_FALLBACK_REQUIRED to false) to false,
            // Value present and TRUE
            mapOf(SINGLE_GOOD_QUALITY_FALLBACK_REQUIRED to true) to true,
        ).forEach { (config, result) ->
            assertThat(ExperimentalProjectConfiguration(config).singleQualityFallbackRequired).isEqualTo(result)
        }
    }

    @Test
    fun `check face auto capture flag correctly`() {
        mapOf(
            // Value not present
            emptyMap<String, Any>() to false,
            // Value not boolean
            mapOf(FACE_AUTO_CAPTURE_ENABLED to 1) to false,
            // Value present and FALSE
            mapOf(FACE_AUTO_CAPTURE_ENABLED to false) to false,
            // Value present and TRUE
            mapOf(FACE_AUTO_CAPTURE_ENABLED to true) to true,
        ).forEach { (config, result) ->
            assertThat(ExperimentalProjectConfiguration(config).faceAutoCaptureEnabled).isEqualTo(result)
        }
    }

    @Test
    fun `check face auto capture imaging duration flag correctly`() {
        mapOf(
            // Value not present
            emptyMap<String, Any>() to FACE_AUTO_CAPTURE_IMAGING_DURATION_MILLIS_DEFAULT,
            // Value not int
            mapOf(FACE_AUTO_CAPTURE_IMAGING_DURATION_MILLIS to true) to FACE_AUTO_CAPTURE_IMAGING_DURATION_MILLIS_DEFAULT,
            // Value present and lesser than min
            mapOf(FACE_AUTO_CAPTURE_IMAGING_DURATION_MILLIS to 0) to FACE_AUTO_CAPTURE_IMAGING_DURATION_MILLIS_MIN,
            // Value present and greater than max
            mapOf(FACE_AUTO_CAPTURE_IMAGING_DURATION_MILLIS to 60_001) to FACE_AUTO_CAPTURE_IMAGING_DURATION_MILLIS_MAX,
            // Value present and within the range
            mapOf(FACE_AUTO_CAPTURE_IMAGING_DURATION_MILLIS to 1_000) to 1_000L,
        ).forEach { (config, result) ->
            assertThat(ExperimentalProjectConfiguration(config).faceAutoCaptureImagingDurationMillis).isEqualTo(result)
        }
    }

    @Test
    fun `check records DB migration flag correctly`() {
        mapOf(
            // Value not present
            emptyMap<String, Any>() to false,
            // Value not boolean
            mapOf(RECORDS_DB_MIGRATION_FROM_REALM_TO_ROOM_ENABLED to 1) to false,
            // Value present and FALSE
            mapOf(RECORDS_DB_MIGRATION_FROM_REALM_TO_ROOM_ENABLED to false) to false,
            // Value present and TRUE
            mapOf(RECORDS_DB_MIGRATION_FROM_REALM_TO_ROOM_ENABLED to true) to true,
        ).forEach { (config, result) ->
            assertThat(ExperimentalProjectConfiguration(config).recordsDbMigrationFromRealmEnabled).isEqualTo(result)
        }
    }

    @Test
    fun `check records DB migration max retries parsed correctly`() {
        mapOf(
            // Value not present
            emptyMap<String, Any>() to RECORDS_DB_MIGRATION_FROM_REALM_TO_ROOM_DEFAULT_MAX_RETRIES,
            // Value not int
            mapOf(RECORDS_DB_MIGRATION_FROM_REALM_TO_ROOM_MAX_RETRIES to true) to
                RECORDS_DB_MIGRATION_FROM_REALM_TO_ROOM_DEFAULT_MAX_RETRIES,
            // Value is int
            mapOf(RECORDS_DB_MIGRATION_FROM_REALM_TO_ROOM_MAX_RETRIES to 3) to 3,
        ).forEach { (config, result) ->
            assertThat(ExperimentalProjectConfiguration(config).recordsDbMigrationFromRealmMaxRetries).isEqualTo(result)
        }
    }

    @Test
    fun `check signed url enabled flag correctly`() {
        mapOf(
            // Value not present
            emptyMap<String, Any>() to false,
            // Value not boolean
            mapOf(SAMPLE_UPLOAD_WITH_URL_ENABLED to 1) to false,
            // Value present and FALSE
            mapOf(SAMPLE_UPLOAD_WITH_URL_ENABLED to false) to false,
            // Value present and TRUE
            mapOf(SAMPLE_UPLOAD_WITH_URL_ENABLED to true) to true,
        ).forEach { (config, result) ->
            assertThat(ExperimentalProjectConfiguration(config).sampleUploadWithSignedUrlEnabled).isEqualTo(result)
        }
    }

    @Test
    fun `check display camera flash flag correctly`() {
        mapOf(
            // Value not present
            emptyMap<String, Any>() to false,
            // Value not boolean
            mapOf(CAMERA_FLASH_CONTROLS_ENABLED to 1) to false,
            // Value present and FALSE
            mapOf(CAMERA_FLASH_CONTROLS_ENABLED to false) to false,
            // Value present and TRUE
            mapOf(CAMERA_FLASH_CONTROLS_ENABLED to true) to true,
        ).forEach { (config, result) ->
            assertThat(ExperimentalProjectConfiguration(config).displayCameraFlashToggle).isEqualTo(result)
        }
    }

    @Test
    fun `check fallback to CommCare threshold days correctly`() {
        mapOf(
            // Value not present
            emptyMap<String, Any>() to FALLBACK_TO_COMMCARE_THRESHOLD_DAYS_DEFAULT,
            // Value not int
            mapOf(FALLBACK_TO_COMMCARE_THRESHOLD_DAYS to true) to FALLBACK_TO_COMMCARE_THRESHOLD_DAYS_DEFAULT,
            mapOf(FALLBACK_TO_COMMCARE_THRESHOLD_DAYS to 0) to 0L,
            mapOf(FALLBACK_TO_COMMCARE_THRESHOLD_DAYS to 1) to 1L,
            mapOf(FALLBACK_TO_COMMCARE_THRESHOLD_DAYS to 5) to 5L,
            // Value present and exactly at default
            mapOf(FALLBACK_TO_COMMCARE_THRESHOLD_DAYS to 3) to 3L,
        ).forEach { (config, result) ->
            assertThat(ExperimentalProjectConfiguration(config).fallbackToCommCareThresholdDays).isEqualTo(result)
        }
    }

    @Test
    fun `check ocr use high res flag correctly`() {
        mapOf(
            emptyMap<String, Any>() to true,
            mapOf(ExperimentalProjectConfiguration.OCR_USE_HIGH_RES to 1) to true,
            mapOf(ExperimentalProjectConfiguration.OCR_USE_HIGH_RES to false) to false,
            mapOf(ExperimentalProjectConfiguration.OCR_USE_HIGH_RES to true) to true,
        ).forEach { (config, result) ->
            assertThat(ExperimentalProjectConfiguration(config).ocrUseHighRes).isEqualTo(result)
        }
    }

    @Test
    fun `check ocr captures value correctly`() {
        val expectedOcrCaptures = 10
        mapOf(
            emptyMap<String, Any>() to ExperimentalProjectConfiguration.OCR_CAPTURES_DEFAULT,
            mapOf(ExperimentalProjectConfiguration.OCR_CAPTURES to true) to ExperimentalProjectConfiguration.OCR_CAPTURES_DEFAULT,
            mapOf(ExperimentalProjectConfiguration.OCR_CAPTURES to expectedOcrCaptures) to expectedOcrCaptures,
        ).forEach { (config, result) ->
            assertThat(ExperimentalProjectConfiguration(config).ocrCaptures).isEqualTo(result)
        }
    }
}
