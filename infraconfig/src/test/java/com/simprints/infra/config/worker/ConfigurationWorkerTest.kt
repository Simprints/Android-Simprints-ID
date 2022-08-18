package com.simprints.infra.config.worker

import androidx.work.ListenableWorker
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.domain.ConfigService
import com.simprints.infra.config.testtools.projectConfiguration
import com.simprints.infra.login.LoginManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ConfigurationWorkerTest {

    companion object {
        private const val PROJECT_ID = "projectId"
    }

    private val loginManager = mockk<LoginManager>()
    private val configService = mockk<ConfigService>()
    private val configurationWorker =
        ConfigurationWorker(mockk(), mockk(relaxed = true), loginManager, configService)

    @Test
    fun `should fail if the signed in project id is empty`() = runTest {
        every { loginManager.getSignedInProjectIdOrEmpty() } returns ""

        val result = configurationWorker.doWork()
        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
    }

    @Test
    fun `should fail if the config service throws an exception`() = runTest {
        every { loginManager.getSignedInProjectIdOrEmpty() } returns PROJECT_ID
        coEvery { configService.refreshConfiguration(PROJECT_ID) } throws Exception()

        val result = configurationWorker.doWork()
        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
    }

    @Test
    fun `should succeed if the config service doesn't throw an exception`() = runTest {
        every { loginManager.getSignedInProjectIdOrEmpty() } returns PROJECT_ID
        coEvery { configService.refreshConfiguration(PROJECT_ID) } returns projectConfiguration

        val result = configurationWorker.doWork()
        assertThat(result).isEqualTo(ListenableWorker.Result.success())
    }
}
