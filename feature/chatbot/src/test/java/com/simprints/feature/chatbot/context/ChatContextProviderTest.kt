package com.simprints.feature.chatbot.context

import android.content.Context
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.network.ConnectivityTracker
import com.simprints.core.domain.common.Modality
import com.simprints.logging.persistent.LogEntry
import com.simprints.logging.persistent.LogEntryType
import com.simprints.logging.persistent.PersistentLogger
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ChatContextProviderTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var configRepository: ConfigRepository

    @MockK
    private lateinit var connectivityTracker: ConnectivityTracker

    @RelaxedMockK
    private lateinit var projectConfig: ProjectConfiguration

    @RelaxedMockK
    private lateinit var generalConfig: GeneralConfiguration

    @RelaxedMockK
    private lateinit var context: Context

    @RelaxedMockK
    private lateinit var persistentLogger: PersistentLogger

    private lateinit var provider: ChatContextProvider

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        provider = ChatContextProvider(context, configRepository, connectivityTracker, persistentLogger)
    }

    @Test
    fun `builds context with project config`() = runTest {
        every { generalConfig.modalities } returns listOf(Modality.FINGERPRINT, Modality.FACE)
        every { projectConfig.general } returns generalConfig
        every { projectConfig.fingerprint } returns null
        coEvery { configRepository.getProjectConfiguration() } returns projectConfig
        coEvery { configRepository.getProject() } returns null
        every { connectivityTracker.isConnected() } returns true

        val context = provider.buildContext()

        assertThat(context.enabledModalities).containsExactly("FINGERPRINT", "FACE")
        assertThat(context.isConnected).isTrue()
    }

    @Test
    fun `builds context when offline`() = runTest {
        coEvery { configRepository.getProjectConfiguration() } returns projectConfig
        coEvery { configRepository.getProject() } returns null
        every { projectConfig.general } returns generalConfig
        every { generalConfig.modalities } returns emptyList()
        every { projectConfig.fingerprint } returns null
        every { connectivityTracker.isConnected() } returns false

        val context = provider.buildContext()

        assertThat(context.isConnected).isFalse()
    }

    @Test
    fun `builds context gracefully when config fails`() = runTest {
        coEvery { configRepository.getProjectConfiguration() } throws RuntimeException("not logged in")
        coEvery { configRepository.getProject() } throws RuntimeException("not logged in")
        every { connectivityTracker.isConnected() } returns false

        val context = provider.buildContext()

        assertThat(context.enabledModalities).isEmpty()
        assertThat(context.projectName).isEmpty()
    }

    @Test
    fun `updateScreen changes current screen`() = runTest {
        coEvery { configRepository.getProjectConfiguration() } returns projectConfig
        coEvery { configRepository.getProject() } returns null
        every { projectConfig.general } returns generalConfig
        every { generalConfig.modalities } returns emptyList()
        every { projectConfig.fingerprint } returns null
        every { connectivityTracker.isConnected() } returns true

        provider.updateScreen("ConsentFragment")
        val context = provider.buildContext()

        assertThat(context.currentScreen).isEqualTo("ConsentFragment")
    }

    @Test
    fun `updateStep changes step info`() = runTest {
        coEvery { configRepository.getProjectConfiguration() } returns projectConfig
        coEvery { configRepository.getProject() } returns null
        every { projectConfig.general } returns generalConfig
        every { generalConfig.modalities } returns emptyList()
        every { projectConfig.fingerprint } returns null
        every { connectivityTracker.isConnected() } returns true

        provider.updateStep("Fingerprint Capture", 2, 5)
        val context = provider.buildContext()

        assertThat(context.currentStep).isEqualTo("Fingerprint Capture")
        assertThat(context.currentStepIndex).isEqualTo(2)
        assertThat(context.totalSteps).isEqualTo(5)
    }

    @Test
    fun `extracts scanner type from config`() = runTest {
        every { generalConfig.modalities } returns listOf(Modality.FINGERPRINT)
        every { projectConfig.general } returns generalConfig
        val fingerprintConfig = mockk<FingerprintConfiguration> {
            every { allowedScanners } returns listOf(FingerprintConfiguration.VeroGeneration.VERO_2)
        }
        every { projectConfig.fingerprint } returns fingerprintConfig
        coEvery { configRepository.getProjectConfiguration() } returns projectConfig
        coEvery { configRepository.getProject() } returns null
        every { connectivityTracker.isConnected() } returns true

        val context = provider.buildContext()

        assertThat(context.scannerType).contains("VERO 2")
    }

    @Test
    fun `updateWorkflow changes workflow type`() = runTest {
        coEvery { configRepository.getProjectConfiguration() } returns projectConfig
        coEvery { configRepository.getProject() } returns null
        every { projectConfig.general } returns generalConfig
        every { generalConfig.modalities } returns emptyList()
        every { projectConfig.fingerprint } returns null
        every { connectivityTracker.isConnected() } returns true

        provider.updateWorkflow("Enrolment")
        val context = provider.buildContext()

        assertThat(context.workflowType).isEqualTo("Enrolment")
    }

    @Test
    fun `collects recent logs from persistent logger`() = runTest {
        coEvery { configRepository.getProjectConfiguration() } returns projectConfig
        coEvery { configRepository.getProject() } returns null
        every { projectConfig.general } returns generalConfig
        every { generalConfig.modalities } returns emptyList()
        every { projectConfig.fingerprint } returns null
        every { connectivityTracker.isConnected() } returns true
        coEvery { persistentLogger.get(LogEntryType.Intent) } returns listOf(
            LogEntry(timestampMs = 100, type = LogEntryType.Intent, title = "Enrolment", body = "started"),
        )
        coEvery { persistentLogger.get(LogEntryType.Network) } returns listOf(
            LogEntry(timestampMs = 200, type = LogEntryType.Network, title = "Sync", body = "completed"),
        )

        val context = provider.buildContext()

        assertThat(context.recentLogs).hasSize(2)
        assertThat(context.recentLogs[0]).contains("Network")
        assertThat(context.recentLogs[1]).contains("Intent")
    }

    @Test
    fun `collects recent errors from network logs`() = runTest {
        coEvery { configRepository.getProjectConfiguration() } returns projectConfig
        coEvery { configRepository.getProject() } returns null
        every { projectConfig.general } returns generalConfig
        every { generalConfig.modalities } returns emptyList()
        every { projectConfig.fingerprint } returns null
        every { connectivityTracker.isConnected() } returns true
        coEvery { persistentLogger.get(LogEntryType.Intent) } returns emptyList()
        coEvery { persistentLogger.get(LogEntryType.Network) } returns listOf(
            LogEntry(timestampMs = 100, type = LogEntryType.Network, title = "Sync error", body = "timeout"),
            LogEntry(timestampMs = 200, type = LogEntryType.Network, title = "Upload failed", body = "no connection"),
            LogEntry(timestampMs = 300, type = LogEntryType.Network, title = "Sync", body = "completed"),
        )

        val context = provider.buildContext()

        assertThat(context.recentErrors).hasSize(2)
        assertThat(context.recentErrors[0]).contains("Upload failed")
        assertThat(context.recentErrors[1]).contains("Sync error")
    }

    @Test
    fun `gracefully handles persistent logger failure`() = runTest {
        coEvery { configRepository.getProjectConfiguration() } returns projectConfig
        coEvery { configRepository.getProject() } returns null
        every { projectConfig.general } returns generalConfig
        every { generalConfig.modalities } returns emptyList()
        every { projectConfig.fingerprint } returns null
        every { connectivityTracker.isConnected() } returns true
        coEvery { persistentLogger.get(any()) } throws RuntimeException("DB error")

        val context = provider.buildContext()

        assertThat(context.recentLogs).isEmpty()
        assertThat(context.recentErrors).isEmpty()
    }
}
