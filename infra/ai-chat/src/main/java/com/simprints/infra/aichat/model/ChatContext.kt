package com.simprints.infra.aichat.model

/**
 * Runtime context injected into the LLM prompt to provide awareness
 * of the user's current situation within the app.
 */
data class ChatContext(
    val currentScreen: String = "",
    val currentStep: String = "",
    val totalSteps: Int = 0,
    val currentStepIndex: Int = 0,
    val projectName: String = "",
    val enabledModalities: List<String> = emptyList(),
    val scannerType: String = "",
    val isConnected: Boolean = false,
    val recentErrors: List<String> = emptyList(),
)
