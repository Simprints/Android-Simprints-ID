package com.simprints.feature.chatbot.context

import android.content.Context
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.aichat.model.WorkflowStepInfo
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.ConsentConfiguration
import com.simprints.infra.config.store.models.DeviceConfiguration
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.store.models.IdentificationConfiguration
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.SynchronizationConfiguration
import com.simprints.infra.network.ConnectivityTracker
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.tokenization.asTokenizableRaw
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
        coEvery { configRepository.getDeviceConfiguration() } returns DeviceConfiguration("en", emptyList(), "")
        provider = ChatContextProvider(context, configRepository, connectivityTracker, persistentLogger)
    }

    @Test
    fun `builds context with project config summary`() = runTest {
        every { generalConfig.modalities } returns listOf(Modality.FINGERPRINT, Modality.FACE)
        every { generalConfig.matchingModalities } returns listOf(Modality.FINGERPRINT)
        every { generalConfig.languageOptions } returns listOf("en", "fr")
        every { generalConfig.defaultLanguage } returns "en"
        every { generalConfig.collectLocation } returns true
        every { generalConfig.duplicateBiometricEnrolmentCheck } returns false
        every { projectConfig.general } returns generalConfig
        every { projectConfig.face } returns null
        every { projectConfig.fingerprint } returns null
        every { projectConfig.multifactorId } returns null
        every { projectConfig.consent } returns mockk<ConsentConfiguration>(relaxed = true)
        every { projectConfig.identification } returns mockk<IdentificationConfiguration>(relaxed = true)
        every { projectConfig.synchronization } returns mockk<SynchronizationConfiguration>(relaxed = true)
        coEvery { configRepository.getProjectConfiguration() } returns projectConfig
        coEvery { configRepository.getProject() } returns null
        every { connectivityTracker.isConnected() } returns true

        val result = provider.buildContext()

        assertThat(result.projectConfigSummary).contains("FINGERPRINT, FACE")
        assertThat(result.projectConfigSummary).contains("General")
        assertThat(result.isConnected).isTrue()
    }

    @Test
    fun `builds context when offline`() = runTest {
        coEvery { configRepository.getProjectConfiguration() } returns projectConfig
        coEvery { configRepository.getProject() } returns null
        every { projectConfig.general } returns generalConfig
        every { generalConfig.modalities } returns emptyList()
        every { projectConfig.fingerprint } returns null
        every { connectivityTracker.isConnected() } returns false

        val result = provider.buildContext()

        assertThat(result.isConnected).isFalse()
    }

    @Test
    fun `builds context gracefully when config fails`() = runTest {
        coEvery { configRepository.getProjectConfiguration() } throws RuntimeException("not logged in")
        coEvery { configRepository.getProject() } throws RuntimeException("not logged in")
        coEvery { configRepository.getDeviceConfiguration() } throws RuntimeException("not logged in")
        every { connectivityTracker.isConnected() } returns false

        val result = provider.buildContext()

        assertThat(result.projectConfigSummary).isEmpty()
        assertThat(result.projectName).isEmpty()
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
        val result = provider.buildContext()

        assertThat(result.currentScreen).isEqualTo("ConsentFragment")
    }

    @Test
    fun `updateWorkflow sets workflow state`() = runTest {
        coEvery { configRepository.getProjectConfiguration() } returns projectConfig
        coEvery { configRepository.getProject() } returns null
        every { projectConfig.general } returns generalConfig
        every { generalConfig.modalities } returns emptyList()
        every { projectConfig.fingerprint } returns null
        every { connectivityTracker.isConnected() } returns true

        provider.updateWorkflow("Enrolment")
        val result = provider.buildContext()

        assertThat(result.isInWorkflow).isTrue()
        assertThat(result.workflowType).isEqualTo("Enrolment")
    }

    @Test
    fun `updateSteps provides full step list`() = runTest {
        coEvery { configRepository.getProjectConfiguration() } returns projectConfig
        coEvery { configRepository.getProject() } returns null
        every { projectConfig.general } returns generalConfig
        every { generalConfig.modalities } returns emptyList()
        every { projectConfig.fingerprint } returns null
        every { connectivityTracker.isConnected() } returns true

        provider.updateSteps(
            listOf(
                WorkflowStepInfo("Setup", "Completed"),
                WorkflowStepInfo("Consent", "In Progress"),
                WorkflowStepInfo("Fingerprint Capture", "Not Started"),
            ),
        )
        val result = provider.buildContext()

        assertThat(result.workflowSteps).hasSize(3)
        assertThat(result.workflowSteps[0].name).isEqualTo("Setup")
        assertThat(result.workflowSteps[0].status).isEqualTo("Completed")
        assertThat(result.workflowSteps[2].status).isEqualTo("Not Started")
    }

    @Test
    fun `clearWorkflow resets workflow state`() = runTest {
        coEvery { configRepository.getProjectConfiguration() } returns projectConfig
        coEvery { configRepository.getProject() } returns null
        every { projectConfig.general } returns generalConfig
        every { generalConfig.modalities } returns emptyList()
        every { projectConfig.fingerprint } returns null
        every { connectivityTracker.isConnected() } returns true

        provider.updateWorkflow("Enrolment")
        provider.updateSteps(listOf(WorkflowStepInfo("Setup", "Completed")))
        provider.clearWorkflow()
        val result = provider.buildContext()

        assertThat(result.isInWorkflow).isFalse()
        assertThat(result.workflowType).isEmpty()
        assertThat(result.workflowSteps).isEmpty()
    }

    @Test
    fun `updateActiveAlert sets alert and clearActiveAlert resets it`() = runTest {
        coEvery { configRepository.getProjectConfiguration() } returns projectConfig
        coEvery { configRepository.getProject() } returns null
        every { projectConfig.general } returns generalConfig
        every { generalConfig.modalities } returns emptyList()
        every { projectConfig.fingerprint } returns null
        every { connectivityTracker.isConnected() } returns true

        provider.updateActiveAlert("Rooted Device Detected: security risk")
        val withAlert = provider.buildContext()
        assertThat(withAlert.activeAlert).contains("Rooted Device")

        provider.clearActiveAlert()
        val cleared = provider.buildContext()
        assertThat(cleared.activeAlert).isEmpty()
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

        val result = provider.buildContext()

        assertThat(result.recentLogs).hasSize(2)
        assertThat(result.recentLogs[0]).contains("Network")
        assertThat(result.recentLogs[1]).contains("Intent")
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

        val result = provider.buildContext()

        assertThat(result.recentErrors).hasSize(2)
        assertThat(result.recentErrors[0]).contains("Upload failed")
        assertThat(result.recentErrors[1]).contains("Sync error")
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

        val result = provider.buildContext()

        assertThat(result.recentLogs).isEmpty()
        assertThat(result.recentErrors).isEmpty()
    }

    @Test
    fun `includes device configuration with selected modules`() = runTest {
        coEvery { configRepository.getProjectConfiguration() } returns projectConfig
        coEvery { configRepository.getProject() } returns null
        every { projectConfig.general } returns generalConfig
        every { generalConfig.modalities } returns emptyList()
        every { projectConfig.fingerprint } returns null
        every { connectivityTracker.isConnected() } returns true
        coEvery { configRepository.getDeviceConfiguration() } returns DeviceConfiguration(
            language = "fr",
            selectedModules = listOf("Module A".asTokenizableRaw(), "Module B".asTokenizableRaw()),
            lastInstructionId = "instr-123",
        )

        val result = provider.buildContext()

        assertThat(result.projectConfigSummary).contains("Device Configuration")
        assertThat(result.projectConfigSummary).contains("Language: fr")
        assertThat(result.projectConfigSummary).contains("Module A")
        assertThat(result.projectConfigSummary).contains("Module B")
    }
}
