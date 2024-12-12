package com.simprints.infra.config.store.remote.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.testtools.apiConsentConfiguration
import com.simprints.infra.config.store.testtools.apiGeneralConfiguration
import com.simprints.infra.config.store.testtools.apiIdentificationConfiguration
import com.simprints.infra.config.store.testtools.apiProjectConfiguration
import com.simprints.infra.config.store.testtools.apiSynchronizationConfiguration
import com.simprints.infra.config.store.testtools.consentConfiguration
import com.simprints.infra.config.store.testtools.customKeyMap
import com.simprints.infra.config.store.testtools.generalConfiguration
import com.simprints.infra.config.store.testtools.identificationConfiguration
import com.simprints.infra.config.store.testtools.projectConfiguration
import com.simprints.infra.config.store.testtools.synchronizationConfiguration
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
            apiSynchronizationConfiguration,
            customKeyMap,
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
            synchronizationConfiguration,
            customKeyMap,
        )

        assertThat(apiProjectConfiguration.toDomain()).isEqualTo(projectConfiguration)
    }
}
