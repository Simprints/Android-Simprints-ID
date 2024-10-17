package com.simprints.infra.config.store.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.models.ExperimentalProjectConfiguration.Companion.ENABLE_ID_POOL_VALIDATION
import kotlin.test.Test

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

}
