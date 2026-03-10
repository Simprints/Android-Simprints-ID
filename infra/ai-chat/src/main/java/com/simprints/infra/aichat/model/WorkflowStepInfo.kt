package com.simprints.infra.aichat.model

/**
 * Lightweight representation of an orchestrator step for chat context.
 * Used to communicate step state across module boundaries.
 */
data class WorkflowStepInfo(
    val name: String,
    val status: String,
)
