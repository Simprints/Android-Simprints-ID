package com.simprints.infra.config.remote.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.domain.Project
import com.simprints.infra.config.testtools.apiProject
import com.simprints.infra.config.testtools.project
import org.junit.Test

class ApiProjectTest {

    @Test
    fun `should map correctly the model`() {
        assertThat(apiProject.toDomain()).isEqualTo(project)
    }
}
