package com.simprints.infra.config.store.remote.models

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.models.ProjectState
import org.junit.Test

class ApiProjectStateTest {
    @Test
    fun toDomain() {
        mapOf(
            ApiProjectState.RUNNING to ProjectState.RUNNING,
            ApiProjectState.PAUSED to ProjectState.PROJECT_PAUSED,
            ApiProjectState.ENDING to ProjectState.PROJECT_ENDING,
            ApiProjectState.ENDED to ProjectState.PROJECT_ENDED,
        ).forEach { (apiProjectState, projectState) ->
            assertThat(apiProjectState.toDomain()).isEqualTo(projectState)
        }
    }
}
