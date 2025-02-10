package com.simprints.feature.troubleshooting.overview.usecase

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.sync.ConfigSyncCache
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CollectConfigurationDetailsUseCaseTest {
    @MockK
    private lateinit var configManager: ConfigRepository

    @MockK
    private lateinit var configSyncCache: ConfigSyncCache

    private lateinit var useCase: CollectConfigurationDetailsUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { id } returns CONFIG_ID
            every { updatedAt } returns UPDATED_AT
        }
        coEvery { configSyncCache.sinceLastUpdateTime() } returns SINCE_LAST_UPDATE

        useCase = CollectConfigurationDetailsUseCase(
            configManager,
            configSyncCache,
        )
    }

    @Test
    fun `result contains details from all sources`() = runTest {
        val details = useCase()

        assertThat(details).contains(CONFIG_ID)
        assertThat(details).contains(UPDATED_AT)
        assertThat(details).contains(SINCE_LAST_UPDATE)
    }

    companion object {
        private const val CONFIG_ID = "configId"
        private const val UPDATED_AT = "updateDate"
        private const val SINCE_LAST_UPDATE = "sinceLastUpdate"
    }
}
