package com.simprints.id.orchestrator

import com.simprints.id.orchestrator.steps.Step

interface PersonCreationEventHelper {
    suspend fun addPersonCreationEventIfNeeded(steps: List<Step.Result>)
}
