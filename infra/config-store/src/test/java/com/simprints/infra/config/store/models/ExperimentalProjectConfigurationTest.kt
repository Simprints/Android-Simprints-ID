package com.simprints.infra.config.store.models

import com.google.common.truth.Truth.*
import com.simprints.infra.config.store.models.ExperimentalProjectConfiguration.Companion.ALLOW_CONFIRMING_GUIDS_NOT_IN_CALLBACK
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
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Test

internal class ExperimentalProjectConfigurationTest {
    @Test
    fun `check pool validation flag correctly`() {
        mapOf(
            // Value not present
            emptyMap<String, JsonElement>() to false,
            // Value not boolean
            mapOf(ENABLE_ID_POOL_VALIDATION to JsonPrimitive(1)) to false,
            // Value present and FALSE
            mapOf(ENABLE_ID_POOL_VALIDATION to JsonPrimitive(false)) to false,
            // Value present and TRUE
            mapOf(ENABLE_ID_POOL_VALIDATION to JsonPrimitive(true)) to true,
        ).forEach { (config, result) ->
            assertThat(ExperimentalProjectConfiguration(config).idPoolValidationEnabled).isEqualTo(result)
        }
    }

    @Test
    fun `check single good face capture fallback flag correctly`() {
        mapOf(
            // Value not present
            emptyMap<String, JsonElement>() to false,
            // Value not boolean
            mapOf(SINGLE_GOOD_QUALITY_FALLBACK_REQUIRED to JsonPrimitive(1)) to false,
            // Value present and FALSE
            mapOf(SINGLE_GOOD_QUALITY_FALLBACK_REQUIRED to JsonPrimitive(false)) to false,
            // Value present and TRUE
            mapOf(SINGLE_GOOD_QUALITY_FALLBACK_REQUIRED to JsonPrimitive(true)) to true,
        ).forEach { (config, result) ->
            assertThat(ExperimentalProjectConfiguration(config).singleQualityFallbackRequired).isEqualTo(result)
        }
    }

    @Test
    fun `check face auto capture flag correctly`() {
        mapOf(
            // Value not present
            emptyMap<String, JsonElement>() to false,
            // Value not boolean
            mapOf(FACE_AUTO_CAPTURE_ENABLED to JsonPrimitive(1)) to false,
            // Value present and FALSE
            mapOf(FACE_AUTO_CAPTURE_ENABLED to JsonPrimitive(false)) to false,
            // Value present and TRUE
            mapOf(FACE_AUTO_CAPTURE_ENABLED to JsonPrimitive(true)) to true,
        ).forEach { (config, result) ->
            assertThat(ExperimentalProjectConfiguration(config).faceAutoCaptureEnabled).isEqualTo(result)
        }
    }

    @Test
    fun `check face auto capture imaging duration flag correctly`() {
        mapOf(
            // Value not present
            emptyMap<String, JsonElement>() to FACE_AUTO_CAPTURE_IMAGING_DURATION_MILLIS_DEFAULT,
            // Value not int
            mapOf(FACE_AUTO_CAPTURE_IMAGING_DURATION_MILLIS to JsonPrimitive(true)) to FACE_AUTO_CAPTURE_IMAGING_DURATION_MILLIS_DEFAULT,
            // Value present and lesser than min
            mapOf(FACE_AUTO_CAPTURE_IMAGING_DURATION_MILLIS to JsonPrimitive(0)) to FACE_AUTO_CAPTURE_IMAGING_DURATION_MILLIS_MIN,
            // Value present and greater than max
            mapOf(FACE_AUTO_CAPTURE_IMAGING_DURATION_MILLIS to JsonPrimitive(60_001)) to FACE_AUTO_CAPTURE_IMAGING_DURATION_MILLIS_MAX,
            // Value present and within the range
            mapOf(FACE_AUTO_CAPTURE_IMAGING_DURATION_MILLIS to JsonPrimitive(1_000)) to 1_000L,
        ).forEach { (config, result) ->
            assertThat(ExperimentalProjectConfiguration(config).faceAutoCaptureImagingDurationMillis).isEqualTo(result)
        }
    }

    @Test
    fun `check records DB migration flag correctly`() {
        mapOf(
            // Value not present
            emptyMap<String, JsonElement>() to false,
            // Value not boolean
            mapOf(RECORDS_DB_MIGRATION_FROM_REALM_TO_ROOM_ENABLED to JsonPrimitive(1)) to false,
            // Value present and FALSE
            mapOf(RECORDS_DB_MIGRATION_FROM_REALM_TO_ROOM_ENABLED to JsonPrimitive(false)) to false,
            // Value present and TRUE
            mapOf(RECORDS_DB_MIGRATION_FROM_REALM_TO_ROOM_ENABLED to JsonPrimitive(true)) to true,
        ).forEach { (config, result) ->
            assertThat(ExperimentalProjectConfiguration(config).recordsDbMigrationFromRealmEnabled).isEqualTo(result)
        }
    }

    @Test
    fun `check records DB migration max retries parsed correctly`() {
        mapOf(
            // Value not present
            emptyMap<String, JsonElement>() to RECORDS_DB_MIGRATION_FROM_REALM_TO_ROOM_DEFAULT_MAX_RETRIES,
            // Value not int
            mapOf(RECORDS_DB_MIGRATION_FROM_REALM_TO_ROOM_MAX_RETRIES to JsonPrimitive(true)) to
                RECORDS_DB_MIGRATION_FROM_REALM_TO_ROOM_DEFAULT_MAX_RETRIES,
            // Value is int
            mapOf(RECORDS_DB_MIGRATION_FROM_REALM_TO_ROOM_MAX_RETRIES to JsonPrimitive(3)) to 3,
        ).forEach { (config, result) ->
            assertThat(ExperimentalProjectConfiguration(config).recordsDbMigrationFromRealmMaxRetries).isEqualTo(result)
        }
    }

    @Test
    fun `check signed url enabled flag correctly`() {
        mapOf(
            // Value not present
            emptyMap<String, JsonElement>() to false,
            // Value not boolean
            mapOf(SAMPLE_UPLOAD_WITH_URL_ENABLED to JsonPrimitive(1)) to false,
            // Value present and FALSE
            mapOf(SAMPLE_UPLOAD_WITH_URL_ENABLED to JsonPrimitive(false)) to false,
            // Value present and TRUE
            mapOf(SAMPLE_UPLOAD_WITH_URL_ENABLED to JsonPrimitive(true)) to true,
        ).forEach { (config, result) ->
            assertThat(ExperimentalProjectConfiguration(config).sampleUploadWithSignedUrlEnabled).isEqualTo(result)
        }
    }

    @Test
    fun `check display camera flash flag correctly`() {
        mapOf(
            // Value not present
            emptyMap<String, JsonElement>() to false,
            // Value not boolean
            mapOf(CAMERA_FLASH_CONTROLS_ENABLED to JsonPrimitive(1)) to false,
            // Value present and FALSE
            mapOf(CAMERA_FLASH_CONTROLS_ENABLED to JsonPrimitive(false)) to false,
            // Value present and TRUE
            mapOf(CAMERA_FLASH_CONTROLS_ENABLED to JsonPrimitive(true)) to true,
        ).forEach { (config, result) ->
            assertThat(ExperimentalProjectConfiguration(config).displayCameraFlashToggle).isEqualTo(result)
        }
    }

    @Test
    fun `check fallback to CommCare threshold days correctly`() {
        mapOf(
            // Value not present
            emptyMap<String, JsonElement>() to FALLBACK_TO_COMMCARE_THRESHOLD_DAYS_DEFAULT,
            // Value not int
            mapOf(FALLBACK_TO_COMMCARE_THRESHOLD_DAYS to JsonPrimitive(true)) to FALLBACK_TO_COMMCARE_THRESHOLD_DAYS_DEFAULT,
            mapOf(FALLBACK_TO_COMMCARE_THRESHOLD_DAYS to JsonPrimitive(0)) to 0L,
            mapOf(FALLBACK_TO_COMMCARE_THRESHOLD_DAYS to JsonPrimitive(1)) to 1L,
            mapOf(FALLBACK_TO_COMMCARE_THRESHOLD_DAYS to JsonPrimitive(5)) to 5L,
            // Value present and exactly at default
            mapOf(FALLBACK_TO_COMMCARE_THRESHOLD_DAYS to JsonPrimitive(3)) to 3L,
        ).forEach { (config, result) ->
            assertThat(ExperimentalProjectConfiguration(config).fallbackToCommCareThresholdDays).isEqualTo(result)
        }
    }

    @Test
    fun `check ocr use high res flag correctly`() {
        mapOf(
            emptyMap<String, JsonElement>() to true,
            mapOf(ExperimentalProjectConfiguration.OCR_USE_HIGH_RES to JsonPrimitive(1)) to true,
            mapOf(ExperimentalProjectConfiguration.OCR_USE_HIGH_RES to JsonPrimitive(false)) to false,
            mapOf(ExperimentalProjectConfiguration.OCR_USE_HIGH_RES to JsonPrimitive(true)) to true,
        ).forEach { (config, result) ->
            assertThat(ExperimentalProjectConfiguration(config).ocrUseHighRes).isEqualTo(result)
        }
    }

    @Test
    fun `check ocr captures value correctly`() {
        val expectedOcrCaptures = 10
        mapOf(
            emptyMap<String, JsonElement>() to ExperimentalProjectConfiguration.OCR_CAPTURES_DEFAULT,
            mapOf(ExperimentalProjectConfiguration.OCR_CAPTURES to JsonPrimitive(true))
                to ExperimentalProjectConfiguration.OCR_CAPTURES_DEFAULT,
            mapOf(ExperimentalProjectConfiguration.OCR_CAPTURES to JsonPrimitive(expectedOcrCaptures)) to expectedOcrCaptures,
        ).forEach { (config, result) ->
            assertThat(ExperimentalProjectConfiguration(config).ocrCaptures).isEqualTo(result)
        }
    }

    @Test
    fun `check allow confirming GUIDs not in callback flag correctly`() {
        mapOf(
            // Value not present
            emptyMap<String, JsonElement>() to false,
            // Value not boolean
            mapOf(ALLOW_CONFIRMING_GUIDS_NOT_IN_CALLBACK to JsonPrimitive(1)) to false,
            // Value present and FALSE
            mapOf(ALLOW_CONFIRMING_GUIDS_NOT_IN_CALLBACK to JsonPrimitive(false)) to false,
            // Value present and TRUE
            mapOf(ALLOW_CONFIRMING_GUIDS_NOT_IN_CALLBACK to JsonPrimitive(true)) to true,
        ).forEach { (config, result) ->
            assertThat(ExperimentalProjectConfiguration(config).allowConfirmingGuidsNotInCallback).isEqualTo(result)
        }
    }
}
