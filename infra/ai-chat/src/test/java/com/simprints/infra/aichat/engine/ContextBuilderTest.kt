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
            projectName = "Test Project",
            enabledModalities = listOf("FINGERPRINT", "FACE"),
            scannerType = "VERO 2",
            isConnected = true,
            recentErrors = listOf("Scanner timeout", "Bluetooth error"),
        )

        val result = contextBuilder.build(context)

        assertThat(result).contains("ConsentScreen")
        assertThat(result).contains("Fingerprint Capture")
        assertThat(result).contains("step 3 of 5")
        assertThat(result).contains("Test Project")
        assertThat(result).contains("FINGERPRINT, FACE")
        assertThat(result).contains("VERO 2")
        assertThat(result).contains("Yes")
        assertThat(result).contains("Scanner timeout")
        assertThat(result).contains("Bluetooth error")
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
}
