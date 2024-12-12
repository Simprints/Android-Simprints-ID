package com.simprints.feature.dashboard.logout.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.simprints.infra.authlogic.AuthManager
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LogoutUseCaseTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var syncOrchestrator: SyncOrchestrator

    @MockK
    private lateinit var authManager: AuthManager

    private lateinit var useCase: LogoutUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        useCase = LogoutUseCase(
            syncOrchestrator = syncOrchestrator,
            authManager = authManager,
        )
    }

    @Test
    fun `Fully logs out when called`() = runTest {
        useCase.invoke()

        coVerify {
            syncOrchestrator.cancelBackgroundWork()
            syncOrchestrator.deleteEventSyncInfo()
            authManager.signOut()
        }
    }
}
