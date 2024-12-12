package com.simprints.infra.config.store.local.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.testtools.project
import com.simprints.infra.config.store.testtools.protoProject
import org.junit.Test

class ProtoProjectTest {
    @Test
    fun `domain project model mapped to proto model correctly`() {
        val result = project.toProto()
        assertThat(result).isEqualTo(protoProject)
    }

    @Test
    fun `proto project model mapped to domain model correctly`() {
        val result = protoProject.toDomain()
        assertThat(result).isEqualTo(project)
    }

    @Test
    fun `proto project model without state mapped to domain model correctly`() {
        val result = protoProject
            .toBuilder()
            .clearState()
            .build()
            .toDomain()
        assertThat(result).isEqualTo(project)
    }
}
