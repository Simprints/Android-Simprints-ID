package com.simprints.infra.aichat.engine

import com.simprints.infra.aichat.model.ChatContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Builds the dynamic context section of the system prompt from the current
 * runtime state (screen, step, project config, device info, recent errors/logs).
 */
@Singleton
internal class ContextBuilder @Inject constructor() {

    fun build(context: ChatContext): String = buildString {
        appendLine("## Current User Context")
        appendLine()

        if (context.currentScreen.isNotBlank()) {
            appendLine("- **Current screen**: ${context.currentScreen}")
        }
        if (context.currentStep.isNotBlank()) {
            append("- **Current step**: ${context.currentStep}")
            if (context.totalSteps > 0) {
                append(" (step ${context.currentStepIndex} of ${context.totalSteps})")
            }
            appendLine()
        }
        if (context.workflowType.isNotBlank()) {
            appendLine("- **Workflow**: ${context.workflowType}")
        }

        appendLine("- **Connected to internet**: ${if (context.isConnected) "Yes" else "No"}")

        if (context.projectName.isNotBlank()) {
            appendLine("- **Project**: ${context.projectName}")
        }
        if (context.enabledModalities.isNotEmpty()) {
            appendLine("- **Enabled modalities**: ${context.enabledModalities.joinToString(", ")}")
        }
        if (context.scannerType.isNotBlank()) {
            appendLine("- **Scanner type**: ${context.scannerType}")
        }

        appendDeviceInfo(context)
        appendRecentErrors(context)
        appendRecentLogs(context)
    }

    private fun StringBuilder.appendDeviceInfo(context: ChatContext) {
        val hasDeviceInfo = context.appVersion.isNotBlank() ||
            context.androidVersion.isNotBlank() ||
            context.freeStorageMb >= 0 ||
            context.batteryPercent >= 0

        if (!hasDeviceInfo) return

        appendLine()
        appendLine("### Device Info")
        if (context.appVersion.isNotBlank()) {
            appendLine("- **App version**: ${context.appVersion}")
        }
        if (context.androidVersion.isNotBlank()) {
            appendLine("- **Android version**: ${context.androidVersion}")
        }
        if (context.freeStorageMb >= 0) {
            appendLine("- **Free storage**: ${context.freeStorageMb} MB")
        }
        if (context.batteryPercent >= 0) {
            appendLine("- **Battery**: ${context.batteryPercent}%")
        }
    }

    private fun StringBuilder.appendRecentErrors(context: ChatContext) {
        if (context.recentErrors.isEmpty()) return

        appendLine()
        appendLine("### Recent Errors")
        context.recentErrors.forEach { appendLine("- $it") }
    }

    private fun StringBuilder.appendRecentLogs(context: ChatContext) {
        if (context.recentLogs.isEmpty()) return

        appendLine()
        appendLine("### Recent Activity Log")
        context.recentLogs.forEach { appendLine("- $it") }
    }
}
