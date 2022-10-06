package com.simprints.infra.config.remote.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.domain.models.ProjectConfiguration
import com.simprints.infra.config.testtools.*
import org.junit.Test

class ApiProjectConfigurationTest {

    @Test
    fun `should map correctly the model`() {
        assertThat(apiProjectConfiguration.toDomain()).isEqualTo(projectConfiguration)
    }

    @Test
    fun `should map correctly the model when both fingerprint and face are missing`() {
        val apiProjectConfiguration = ApiProjectConfiguration(
            "projectId",
            apiGeneralConfiguration,
            null,
            null,
            apiConsentConfiguration,
            apiIdentificationConfiguration,
            apiSynchronizationConfiguration
        )
        val projectConfiguration = ProjectConfiguration(
            "projectId",
            generalConfiguration,
            null,
            null,
            consentConfiguration,
            identificationConfiguration,
            synchronizationConfiguration
        )

        assertThat(apiProjectConfiguration.toDomain()).isEqualTo(projectConfiguration)
    }
}
