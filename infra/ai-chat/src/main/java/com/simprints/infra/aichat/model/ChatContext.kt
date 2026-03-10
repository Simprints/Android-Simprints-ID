package com.simprints.infra.aichat.model

/**
 * Runtime context injected into the LLM prompt to provide awareness
 * of the user's current situation within the app.
 */
data class ChatContext(
    val currentScreen: String = "",
    val isInWorkflow: Boolean = false,
    val workflowType: String = "",
    val workflowSteps: List<WorkflowStepInfo> = emptyList(),
    val requestParameters: Map<String, String> = emptyMap(),
    val activeAlert: String = "",
    val projectName: String = "",
    val projectConfigSummary: String = "",
    val isConnected: Boolean = false,
    val recentErrors: List<String> = emptyList(),
    val recentLogs: List<String> = emptyList(),
    val appVersion: String = "",
    val androidVersion: String = "",
    val freeStorageMb: Long = -1,
    val batteryPercent: Int = -1,
)
