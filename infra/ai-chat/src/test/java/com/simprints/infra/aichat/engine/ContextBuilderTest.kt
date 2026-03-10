package com.simprints.infra.aichat.engine

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.aichat.model.ChatContext
import com.simprints.infra.aichat.model.WorkflowStepInfo
import org.junit.Before
import org.junit.Test

class ContextBuilderTest {

    private lateinit var contextBuilder: ContextBuilder

    @Before
    fun setUp() {
        contextBuilder = ContextBuilder()
    }

    @Test
    fun `builds context when user is in a workflow`() {
        val context = ChatContext(
            currentScreen = "ConsentScreen",
            isInWorkflow = true,
            workflowType = "Enrolment",
            workflowSteps = listOf(
                WorkflowStepInfo("Setup", "Completed"),
                WorkflowStepInfo("Consent", "In Progress"),
                WorkflowStepInfo("Fingerprint Capture", "Not Started"),
                WorkflowStepInfo("Face Capture", "Not Started"),
                WorkflowStepInfo("Enrol Biometric", "Not Started"),
            ),
            projectName = "Test Project",
            projectConfigSummary = "**General**\n- Modalities: FINGERPRINT, FACE",
            isConnected = true,
            recentErrors = listOf("Scanner timeout", "Bluetooth error"),
            recentLogs = listOf("Intent: Enrolment — started"),
            appVersion = "2025.2.0",
            androidVersion = "Android 14 (API 34)",
            freeStorageMb = 2048,
            batteryPercent = 75,
        )

        val result = contextBuilder.build(context)

        assertThat(result).contains("In a workflow")
        assertThat(result).contains("Enrolment")
        assertThat(result).contains("ConsentScreen")
        assertThat(result).contains("Test Project")
        assertThat(result).contains("Yes")
        assertThat(result).contains("Scanner timeout")
        assertThat(result).contains("Bluetooth error")
        assertThat(result).contains("2025.2.0")
        assertThat(result).contains("Android 14 (API 34)")
        assertThat(result).contains("2048 MB")
        assertThat(result).contains("75%")
        assertThat(result).contains("Enrolment — started")
    }

    @Test
    fun `renders workflow steps table`() {
        val context = ChatContext(
            isInWorkflow = true,
            workflowSteps = listOf(
                WorkflowStepInfo("Setup", "Completed"),
                WorkflowStepInfo("Consent", "In Progress"),
                WorkflowStepInfo("Fingerprint Capture", "Not Started"),
            ),
        )

        val result = contextBuilder.build(context)

        assertThat(result).contains("Workflow Steps")
        assertThat(result).contains("| 1 | Setup | Completed |")
        assertThat(result).contains("| 2 | Consent | In Progress |")
        assertThat(result).contains("| 3 | Fingerprint Capture | Not Started |")
    }

    @Test
    fun `shows not in workflow when at dashboard`() {
        val context = ChatContext(
            isInWorkflow = false,
            currentScreen = "Dashboard",
        )

        val result = contextBuilder.build(context)

        assertThat(result).contains("App opened from launcher")
        assertThat(result).doesNotContain("Workflow Steps")
        assertThat(result).contains("Dashboard")
    }

    @Test
    fun `builds context with minimal fields`() {
        val context = ChatContext()

        val result = contextBuilder.build(context)

        assertThat(result).contains("Connected to internet")
        assertThat(result).contains("No")
        assertThat(result).contains("App opened from launcher")
        assertThat(result).doesNotContain("Current screen")
        assertThat(result).doesNotContain("Project")
        assertThat(result).doesNotContain("Device Info")
        assertThat(result).doesNotContain("Recent Activity Log")
    }

    @Test
    fun `shows project config summary when present`() {
        val context = ChatContext(
            projectConfigSummary = "**General**\n- Modalities: FINGERPRINT, FACE\n**Consent**\n- Program: Test",
        )

        val result = contextBuilder.build(context)

        assertThat(result).contains("Project Configuration")
        assertThat(result).contains("Modalities: FINGERPRINT, FACE")
        assertThat(result).contains("Program: Test")
    }

    @Test
    fun `omits project config section when empty`() {
        val context = ChatContext()
        val result = contextBuilder.build(context)
        assertThat(result).doesNotContain("Project Configuration")
    }

    @Test
    fun `shows recent errors section when errors present`() {
        val context = ChatContext(
            recentErrors = listOf("Error 1", "Error 2"),
        )

        val result = contextBuilder.build(context)

        assertThat(result).contains("Recent Errors")
        assertThat(result).contains("Error 1")
        assertThat(result).contains("Error 2")
    }

    @Test
    fun `omits recent errors section when no errors`() {
        val context = ChatContext(recentErrors = emptyList())
        val result = contextBuilder.build(context)
        assertThat(result).doesNotContain("Recent Errors")
    }

    @Test
    fun `shows device info section when data available`() {
        val context = ChatContext(
            appVersion = "2025.1.0",
            freeStorageMb = 512,
            batteryPercent = 30,
        )

        val result = contextBuilder.build(context)

        assertThat(result).contains("Device Info")
        assertThat(result).contains("512 MB")
        assertThat(result).contains("30%")
    }

    @Test
    fun `omits device info section when no data available`() {
        val context = ChatContext()
        val result = contextBuilder.build(context)
        assertThat(result).doesNotContain("Device Info")
    }

    @Test
    fun `shows recent logs section when logs present`() {
        val context = ChatContext(
            recentLogs = listOf("Intent: Enrol — started", "Network: Sync — completed"),
        )

        val result = contextBuilder.build(context)

        assertThat(result).contains("Recent Activity Log")
        assertThat(result).contains("Intent: Enrol — started")
        assertThat(result).contains("Network: Sync — completed")
    }

    @Test
    fun `shows workflow type when in workflow`() {
        val context = ChatContext(isInWorkflow = true, workflowType = "Identification")

        val result = contextBuilder.build(context)

        assertThat(result).contains("Workflow type")
        assertThat(result).contains("Identification")
    }
}
