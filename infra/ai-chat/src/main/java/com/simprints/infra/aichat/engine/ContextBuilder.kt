package com.simprints.infra.aichat.engine

import com.simprints.infra.aichat.model.ChatContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Builds the dynamic context section of the system prompt from the current
 * runtime state (screen, step, project config, recent errors).
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

        if (context.recentErrors.isNotEmpty()) {
            appendLine()
            appendLine("### Recent Errors")
            context.recentErrors.forEach { appendLine("- $it") }
        }
    }
}
