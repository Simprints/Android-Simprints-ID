package com.simprints.infra.config.local.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.testtools.project
import com.simprints.infra.config.testtools.protoProject
import org.junit.Test

class ProjectTest {

    @Test
    fun `should map correctly the model`() {
        assertThat(protoProject.toDomain()).isEqualTo(project)
        assertThat(project.toProto()).isEqualTo(protoProject)
    }
}
