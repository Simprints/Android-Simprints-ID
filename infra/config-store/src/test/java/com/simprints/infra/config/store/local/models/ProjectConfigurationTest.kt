package com.simprints.infra.config.store.local.models

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.infra.config.store.models.FaydaCardConfig
import com.simprints.infra.config.store.models.MultiFactorIdConfiguration
import com.simprints.infra.config.store.testtools.projectConfiguration
import com.simprints.infra.config.store.testtools.protoProjectConfiguration
import org.junit.Test

class ProjectConfigurationTest {
    @Test
    fun `should map correctly the model`() {
        assertThat(protoProjectConfiguration.toDomain()).isEqualTo(projectConfiguration)
        assertThat(projectConfiguration.toProto()).isEqualTo(protoProjectConfiguration)
    }

    @Test
    fun `should correctly map fayda card config through proto round-trip`() {
        val domainConfig = MultiFactorIdConfiguration(
            allowedExternalCredentials = listOf(ExternalCredentialType.FaydaCard),
            ghanaIdCardConfig = null,
            nhisCardConfig = null,
            qrCodeConfig = null,
            faydaCardConfig = FaydaCardConfig(isCapturingAllFields = true),
        )
        val proto = domainConfig.toProto()
        assertThat(proto.hasFaydaCardConfig()).isTrue()
        assertThat(proto.faydaCardConfig.isCapturingAllFields).isTrue()
        assertThat(proto.toDomain()).isEqualTo(domainConfig)
    }

    @Test
    fun `should ignore broken custom config model`() {
        assertThat(
            protoProjectConfiguration
                .toBuilder()
                .setCustomJson("{")
                .build()
                .toDomain(),
        ).isEqualTo(
            projectConfiguration.copy(custom = null),
        )
    }
}
