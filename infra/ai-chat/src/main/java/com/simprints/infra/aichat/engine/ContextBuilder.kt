package com.simprints.infra.aichat.engine

import com.simprints.infra.aichat.model.ChatContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Builds the dynamic context section of the system prompt from the current
 * runtime state (workflow, project config, device info, recent errors/logs).
 */
@Singleton
internal class ContextBuilder @Inject constructor() {

    fun build(context: ChatContext): String = buildString {
        appendLine("## Current User Context")
        appendLine()

        appendWorkflowState(context)
        appendRequestParameters(context)

        if (context.currentScreen.isNotBlank()) {
            appendLine("- **Current screen**: ${context.currentScreen}")
        }

        appendLine("- **Connected to internet**: ${if (context.isConnected) "Yes" else "No"}")

        if (context.projectName.isNotBlank()) {
            appendLine("- **Project**: ${context.projectName}")
        }

        appendProjectConfig(context)
        appendDeviceInfo(context)
        appendRecentErrors(context)
        appendRecentLogs(context)
    }

    private fun StringBuilder.appendWorkflowState(context: ChatContext) {
        if (context.isInWorkflow) {
            appendLine("- **User state**: In a workflow")
            if (context.workflowType.isNotBlank()) {
                appendLine("- **Workflow type**: ${context.workflowType}")
            }
            if (context.workflowSteps.isNotEmpty()) {
                appendLine()
                appendLine("### Workflow Steps")
                appendLine("| # | Step | Status |")
                appendLine("|---|------|--------|")
                context.workflowSteps.forEachIndexed { index, step ->
                    appendLine("| ${index + 1} | ${step.name} | ${step.status} |")
                }
                appendLine()
            }
        } else {
            appendLine("- **User state**: App opened from launcher (no active workflow)")
        }
    }

    private fun StringBuilder.appendRequestParameters(context: ChatContext) {
        if (context.requestParameters.isEmpty()) return

        appendLine()
        appendLine("### Request Parameters")
        appendLine("These are the parameters sent by the calling app that triggered this workflow.")
        context.requestParameters.forEach { (key, value) ->
            appendLine("- **$key**: $value")
        }
    }

    private fun StringBuilder.appendProjectConfig(context: ChatContext) {
        if (context.projectConfigSummary.isBlank()) return

        appendLine()
        appendLine("### Project Configuration")
        appendLine(context.projectConfigSummary)
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
