package com.simprints.feature.clientapi.logincheck.usecase

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.projectsecuritystore.SecurityStateRepository
import com.simprints.infra.projectsecuritystore.securitystate.models.SecurityState
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test

class GetProjectStateUseCaseTest {

    @MockK
    lateinit var securityStateRepository: SecurityStateRepository


    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
    }

    @Test
    fun `Returns correct project status`() {
        mapOf(
            SecurityState.Status.PROJECT_PAUSED to GetProjectStateUseCase.ProjectState.PAUSED,
            SecurityState.Status.PROJECT_ENDING to GetProjectStateUseCase.ProjectState.ENDING,
            SecurityState.Status.COMPROMISED to GetProjectStateUseCase.ProjectState.ENDED,
            SecurityState.Status.PROJECT_ENDED to GetProjectStateUseCase.ProjectState.ENDED,
            SecurityState.Status.RUNNING to GetProjectStateUseCase.ProjectState.ACTIVE,
        ).forEach { (status, expectedState) ->
            every { securityStateRepository.getSecurityStatusFromLocal() } returns status
            assertThat(GetProjectStateUseCase(securityStateRepository)()).isEqualTo(expectedState)
        }
    }
}
