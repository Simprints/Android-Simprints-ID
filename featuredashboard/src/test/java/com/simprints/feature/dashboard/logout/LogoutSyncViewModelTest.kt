package com.simprints.feature.dashboard.logout

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.feature.dashboard.settings.about.SignerManager
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.SettingsPasswordConfig
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import org.junit.Rule
import org.junit.Test


internal class LogoutSyncViewModelTest {

    private val signerManager = mockk<SignerManager>(relaxed = true)
    private val configManager = mockk<ConfigManager>(relaxed = true)

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @Test
    fun `should logout correctly`() {
        val viewModel = LogoutSyncViewModel(
            signerManager = signerManager,
            externalScope = CoroutineScope(testCoroutineRule.testCoroutineDispatcher),
            configManager = configManager
        )

        viewModel.logout()

        coVerify(exactly = 1) { signerManager.signOut() }
    }

    @Test
    fun `password config should be fetched after initialization`() {
        val config = SettingsPasswordConfig.Locked(password = "123")
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { general } returns mockk {
                every { settingsPassword } returns config
            }
        }
        val viewModel = LogoutSyncViewModel(
            signerManager = signerManager,
            externalScope = CoroutineScope(testCoroutineRule.testCoroutineDispatcher),
            configManager = configManager
        )
        val resultConfig = viewModel.settingsLocked.getOrAwaitValue()
        assertThat(resultConfig).isEqualTo(config)
    }
}
