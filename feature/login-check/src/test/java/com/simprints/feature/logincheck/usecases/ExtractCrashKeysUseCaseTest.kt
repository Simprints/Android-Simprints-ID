package com.simprints.feature.logincheck.usecases

import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.SynchronizationConfiguration
import com.simprints.infra.logging.Simber
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class ExtractCrashKeysUseCaseTest {

    @MockK
    lateinit var configRepository: ConfigRepository

    @MockK
    lateinit var authStore: AuthStore

    private lateinit var useCase: ExtractCrashKeysUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        mockkObject(Simber)

        useCase = ExtractCrashKeysUseCase(configRepository, authStore)
    }

    @After
    fun cleanUp() {
        unmockkObject(Simber)
    }

    @Test
    fun `Sets values to Simber`() = runTest {
        coEvery { configRepository.getProjectConfiguration() } returns mockk {
            every { synchronization.frequency } returns SynchronizationConfiguration.Frequency.PERIODICALLY
        }
        coEvery { configRepository.getDeviceConfiguration() } returns mockk {
            every { selectedModules } returns listOf(
                "module1".asTokenizableRaw(),
                "module2".asTokenizableRaw()
            )
        }
        every { authStore.signedInProjectId } returns "projectId"

        useCase(ActionFactory.getFlowRequest())

        verify {
            Simber.i("projectId")
            Simber.i("userId")
            Simber.i("[module1, module2]")
            Simber.i("PERIODICALLY")
        }
    }
}
