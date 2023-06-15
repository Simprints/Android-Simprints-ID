package com.simprints.infra.config.local.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.testtools.projectConfiguration
import com.simprints.infra.config.testtools.protoProjectConfiguration
import org.junit.Test

class ProjectConfigurationTest {

    @Test
    fun `should map correctly the model`() {
        assertThat(protoProjectConfiguration.toDomain()).isEqualTo(projectConfiguration)
        assertThat(projectConfiguration.toProto()).isEqualTo(protoProjectConfiguration)
    }
}
