package com.simprints.infra.config.store.remote.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.testtools.apiProject
import com.simprints.infra.config.store.testtools.project
import org.junit.Test

class ApiProjectTest {
    @Test
    fun `should map correctly the model`() {
        assertThat(apiProject.toDomain()).isEqualTo(project)
    }
}
