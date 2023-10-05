package com.simprints.infra.config.worker

import androidx.work.ListenableWorker
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.ConfigService
import com.simprints.infra.config.testtools.project
import com.simprints.infra.config.testtools.projectConfiguration
import com.simprints.infra.authstore.AuthStore
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ConfigurationWorkerTest {

    companion object {
        private const val PROJECT_ID = "projectId"
    }

    private val authStore = mockk<AuthStore>()
    private val configService = mockk<com.simprints.config.store.domain.ConfigService>(relaxed = true)
    private val configurationWorker =
        worker.ConfigurationWorker(
            mockk(),
            mockk(relaxed = true),
            authStore,
            configService,
        )

    @Test
    fun `should fail if the signed in project id is empty`() = runTest {
        every { authStore.signedInProjectId } returns ""

        val result = configurationWorker.doWork()
        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
    }

    @Test
    fun `should fail if the config service throws an exception`() = runTest {
        every { authStore.signedInProjectId } returns PROJECT_ID
        coEvery { configService.refreshConfiguration(PROJECT_ID) } throws Exception()

        val result = configurationWorker.doWork()
        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
    }

    @Test
    fun `should succeed if the config service doesn't throw an exception`() = runTest {
        every { authStore.signedInProjectId } returns PROJECT_ID
        coEvery { configService.refreshConfiguration(PROJECT_ID) } returns projectConfiguration
        coEvery { configService.refreshProject(PROJECT_ID) } returns project

        val result = configurationWorker.doWork()
        assertThat(result).isEqualTo(ListenableWorker.Result.success())
    }
}
