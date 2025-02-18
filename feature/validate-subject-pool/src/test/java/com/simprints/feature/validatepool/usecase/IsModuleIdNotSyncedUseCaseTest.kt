package com.simprints.feature.validatepool.usecase

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.infra.config.store.ConfigRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class IsModuleIdNotSyncedUseCaseTest {
    @MockK
    lateinit var configRepository: ConfigRepository

    private lateinit var usecase: IsModuleIdNotSyncedUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery {
            configRepository.getDeviceConfiguration().selectedModules
        } returns listOf(
            "module1".asTokenizableEncrypted(),
            "module2".asTokenizableEncrypted(),
            "module3".asTokenizableEncrypted(),
        )

        usecase = IsModuleIdNotSyncedUseCase(configRepository)
    }

    @Test
    fun `returns true if module is not synced`() = runTest {
        assertThat(usecase("module2".asTokenizableEncrypted())).isFalse()
    }

    @Test
    fun `returns false if module is synced`() = runTest {
        assertThat(usecase("moduleNone".asTokenizableEncrypted())).isTrue()
    }
}
