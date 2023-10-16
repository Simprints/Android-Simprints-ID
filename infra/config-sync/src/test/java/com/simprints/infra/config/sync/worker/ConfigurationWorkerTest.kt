package com.simprints.infra.config.sync.worker

import androidx.work.ListenableWorker
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.sync.testtools.project
import com.simprints.infra.config.sync.testtools.projectConfiguration
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
    private val configRepository = mockk<ConfigRepository>(relaxed = true)
    private val configurationWorker = ConfigurationWorker(
        context = mockk(),
        params = mockk(relaxed = true),
        authStore = authStore,
        configRepository = configRepository
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
        coEvery { configRepository.refreshConfiguration(PROJECT_ID) } throws Exception()

        val result = configurationWorker.doWork()
        assertThat(result).isEqualTo(ListenableWorker.Result.failure())
    }

    @Test
    fun `should succeed if the config service doesn't throw an exception`() = runTest {
        every { authStore.signedInProjectId } returns PROJECT_ID
        coEvery { configRepository.refreshConfiguration(PROJECT_ID) } returns projectConfiguration
        coEvery { configRepository.refreshProject(PROJECT_ID) } returns project

        val result = configurationWorker.doWork()
        assertThat(result).isEqualTo(ListenableWorker.Result.success())
    }
}
