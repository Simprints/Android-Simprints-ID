package com.simprints.infra.config.store.local.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.testtools.project
import com.simprints.infra.config.store.testtools.protoProject
import org.junit.Test

class ProjectTest {
    @Test
    fun `should map correctly the model`() {
        assertThat(protoProject.toDomain()).isEqualTo(project)
        assertThat(project.toProto()).isEqualTo(protoProject)
    }
}
