package com.simprints.infra.config.store.remote.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.testtools.apiConsentConfiguration
import com.simprints.infra.config.store.testtools.apiGeneralConfiguration
import com.simprints.infra.config.store.testtools.apiIdentificationConfiguration
import com.simprints.infra.config.store.testtools.apiMultiFactorIdConfiguration
import com.simprints.infra.config.store.testtools.apiProjectConfiguration
import com.simprints.infra.config.store.testtools.apiSynchronizationConfiguration
import com.simprints.infra.config.store.testtools.consentConfiguration
import com.simprints.infra.config.store.testtools.customKeyMap
import com.simprints.infra.config.store.testtools.generalConfiguration
import com.simprints.infra.config.store.testtools.identificationConfiguration
import com.simprints.infra.config.store.testtools.multiFactorIdConfiguration
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
            id = "id",
            projectId = "projectId",
            updatedAt = "updatedAt",
            general = apiGeneralConfiguration,
            face = null,
            fingerprint = null,
            consent = apiConsentConfiguration,
            identification = apiIdentificationConfiguration,
            synchronization = apiSynchronizationConfiguration,
            multiFactorId = apiMultiFactorIdConfiguration,
            custom = customKeyMap,
        )
        val projectConfiguration = ProjectConfiguration(
            id = "id",
            projectId = "projectId",
            updatedAt = "updatedAt",
            general = generalConfiguration,
            face = null,
            fingerprint = null,
            consent = consentConfiguration,
            identification = identificationConfiguration,
            synchronization = synchronizationConfiguration,
            multifactorId = multiFactorIdConfiguration,
            custom = customKeyMap,
        )

        assertThat(apiProjectConfiguration.toDomain()).isEqualTo(projectConfiguration)
    }
}
