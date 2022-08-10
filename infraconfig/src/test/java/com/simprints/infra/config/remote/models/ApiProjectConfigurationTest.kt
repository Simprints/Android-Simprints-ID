package com.simprints.infra.config.remote.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.testtools.apiProjectConfiguration
import com.simprints.infra.config.testtools.projectConfiguration
import org.junit.Test

class ApiProjectConfigurationTest {

    @Test
    fun `should map correctly the model`() {
        assertThat(apiProjectConfiguration.toDomain()).isEqualTo(projectConfiguration)
    }
}
