package com.simprints.feature.dashboard.logout

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.simprints.feature.dashboard.settings.about.AboutViewModel
import com.simprints.feature.dashboard.settings.about.SignerManager
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import org.junit.Rule
import org.junit.Test


internal class LogoutSyncViewModelTest {

    private val signerManager = mockk<SignerManager>(relaxed = true)

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @Test
    fun `should logout correctly`() {
        val viewModel = LogoutSyncViewModel(
            signerManager = signerManager,
            externalScope = CoroutineScope(testCoroutineRule.testCoroutineDispatcher),
        )

        viewModel.logout()

        coVerify(exactly = 1) { signerManager.signOut() }
    }
}
