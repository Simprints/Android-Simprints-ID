package com.simprints.infra.eventsync

import com.simprints.core.domain.common.Modality
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.eventsync.status.down.EventDownSyncScopeRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class DeleteModulesUseCaseTest {
    @MockK
    private lateinit var configRepository: ConfigRepository

    @MockK
    private lateinit var downSyncScopeRepository: EventDownSyncScopeRepository

    private lateinit var useCase: DeleteModulesUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        useCase = DeleteModulesUseCase(
            configRepository = configRepository,
            downSyncScopeRepository = downSyncScopeRepository,
        )
    }

    @Test
    fun `deletes operations for provided modules using current project modalities`() = runTest {
        val unselectedModules = listOf("module-1", "module-2")
        val projectModalities = listOf(Modality.FINGERPRINT, Modality.FACE)
        coEvery { configRepository.getProjectConfiguration() } returns createProjectConfiguration(projectModalities)

        useCase(unselectedModules)

        coVerify(exactly = 1) {
            downSyncScopeRepository.deleteOperations(
                unselectedModules,
                modes = projectModalities,
            )
        }
    }

    private fun createProjectConfiguration(modalities: List<Modality>): ProjectConfiguration = mockk {
        every { general.modalities } returns modalities
    }
}
