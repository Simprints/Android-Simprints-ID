package com.simprints.infra.config.store.local.models

import com.google.common.truth.Truth.assertThat
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
