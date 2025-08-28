package com.simprints.feature.logincheck.usecases

import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.models.Frequency
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.logging.Simber
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class ExtractCrashKeysUseCaseTest {
    @MockK
    lateinit var configManager: ConfigManager

    @MockK
    lateinit var authStore: AuthStore

    private lateinit var useCase: ExtractCrashKeysUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        mockkObject(Simber)

        useCase = ExtractCrashKeysUseCase(configManager, authStore)
    }

    @After
    fun cleanUp() {
        unmockkObject(Simber)
    }

    @Test
    fun `Sets values to Simber`() = runTest {
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { synchronization.down.simprints?.frequency } returns Frequency.PERIODICALLY
        }
        coEvery { configManager.getDeviceConfiguration() } returns mockk {
            every { selectedModules } returns listOf(
                "module1".asTokenizableRaw(),
                "module2".asTokenizableRaw(),
            )
        }
        every { authStore.signedInProjectId } returns "projectId"

        useCase(ActionFactory.getIdentifyRequest())

        verify {
            Simber.setUserProperty(any(), "projectId")
            Simber.setUserProperty(any(), "userId")
            Simber.setUserProperty(any(), "[module1, module2]")
            Simber.setUserProperty(any(), "PERIODICALLY")
        }
    }
}
