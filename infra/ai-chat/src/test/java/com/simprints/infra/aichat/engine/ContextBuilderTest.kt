package com.simprints.infra.aichat.engine

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.aichat.model.ChatContext
import org.junit.Before
import org.junit.Test

class ContextBuilderTest {

    private lateinit var contextBuilder: ContextBuilder

    @Before
    fun setUp() {
        contextBuilder = ContextBuilder()
    }

    @Test
    fun `builds context with all fields populated`() {
        val context = ChatContext(
            currentScreen = "ConsentScreen",
            currentStep = "Fingerprint Capture",
            totalSteps = 5,
            currentStepIndex = 3,
            workflowType = "Enrolment",
            projectName = "Test Project",
            enabledModalities = listOf("FINGERPRINT", "FACE"),
            scannerType = "VERO 2",
            isConnected = true,
            recentErrors = listOf("Scanner timeout", "Bluetooth error"),
            recentLogs = listOf("Intent: Enrolment — started"),
            appVersion = "2025.2.0",
            androidVersion = "Android 14 (API 34)",
            freeStorageMb = 2048,
            batteryPercent = 75,
        )

        val result = contextBuilder.build(context)

        assertThat(result).contains("ConsentScreen")
        assertThat(result).contains("Fingerprint Capture")
        assertThat(result).contains("step 3 of 5")
        assertThat(result).contains("Enrolment")
        assertThat(result).contains("Test Project")
        assertThat(result).contains("FINGERPRINT, FACE")
        assertThat(result).contains("VERO 2")
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
    fun `builds context with minimal fields`() {
        val context = ChatContext()

        val result = contextBuilder.build(context)

        assertThat(result).contains("Connected to internet")
        assertThat(result).contains("No")
        assertThat(result).doesNotContain("Current screen")
        assertThat(result).doesNotContain("Current step")
        assertThat(result).doesNotContain("Project")
        assertThat(result).doesNotContain("Device Info")
        assertThat(result).doesNotContain("Recent Activity Log")
    }

    @Test
    fun `omits step count when total is zero`() {
        val context = ChatContext(
            currentStep = "Login",
            totalSteps = 0,
        )

        val result = contextBuilder.build(context)

        assertThat(result).contains("Login")
        assertThat(result).doesNotContain("step 0 of 0")
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
    fun `shows workflow type when set`() {
        val context = ChatContext(workflowType = "Identification")

        val result = contextBuilder.build(context)

        assertThat(result).contains("Workflow")
        assertThat(result).contains("Identification")
    }
}
