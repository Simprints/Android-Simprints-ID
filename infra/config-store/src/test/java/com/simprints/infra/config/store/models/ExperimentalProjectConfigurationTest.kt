package com.simprints.infra.config.store.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.models.ExperimentalProjectConfiguration.Companion.ENABLE_ID_POOL_VALIDATION
import com.simprints.infra.config.store.models.ExperimentalProjectConfiguration.Companion.SINGLE_GOOD_QUALITY_FALLBACK_REQUIRED
import com.simprints.infra.config.store.models.ExperimentalProjectConfiguration.Companion.FACE_AUTO_CAPTURE_ENABLED
import org.junit.Test

internal class ExperimentalProjectConfigurationTest {
    @Test
    fun `check pool validation flag correctly`() {
        mapOf<Map<String, Any>, Boolean>(
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
        mapOf<Map<String, Any>, Boolean>(
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
        mapOf<Map<String, Any>, Boolean>(
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
}
