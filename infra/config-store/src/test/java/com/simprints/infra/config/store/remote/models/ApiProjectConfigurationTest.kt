package com.simprints.infra.config.store.remote.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.testtools.*
import org.junit.Test

class ApiProjectConfigurationTest {

    @Test
    fun `should map correctly the model`() {
        assertThat(apiProjectConfiguration.toDomain()).isEqualTo(projectConfiguration)
    }

    @Test
    fun `should map correctly the model when both fingerprint and face are missing`() {
        val apiProjectConfiguration = ApiProjectConfiguration(
            "id",
            "projectId",
            "updatedAt",
            apiGeneralConfiguration,
            null,
            null,
            apiConsentConfiguration,
            apiIdentificationConfiguration,
            apiSynchronizationConfiguration
        )
        val projectConfiguration = ProjectConfiguration(
            "id",
            "projectId",
            "updatedAt",
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
