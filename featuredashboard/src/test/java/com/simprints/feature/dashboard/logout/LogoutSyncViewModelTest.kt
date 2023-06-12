package com.simprints.feature.dashboard.logout

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.authlogic.AuthManager
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.SettingsPasswordConfig
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import org.junit.Before
import org.junit.Rule
import org.junit.Test


internal class LogoutSyncViewModelTest {

    @MockK
    lateinit var authManager: AuthManager

    @MockK
    lateinit var configManager: ConfigManager

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
    }

    @Test
    fun `should logout correctly`() {
        val viewModel = LogoutSyncViewModel(
            configManager = configManager,
            authManager = authManager,
            externalScope = CoroutineScope(testCoroutineRule.testCoroutineDispatcher)
        )

        viewModel.logout()

        coVerify(exactly = 1) { authManager.signOut() }
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
            configManager = configManager,
            authManager = authManager,
            externalScope = CoroutineScope(testCoroutineRule.testCoroutineDispatcher),
        )
        val resultConfig = viewModel.settingsLocked.getOrAwaitValue()
        assertThat(resultConfig.peekContent()).isEqualTo(config)
    }
}
