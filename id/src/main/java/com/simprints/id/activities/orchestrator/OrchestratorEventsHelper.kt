package com.simprints.id.activities.orchestrator

import com.simprints.id.domain.moduleapi.app.responses.AppResponse

interface OrchestratorEventsHelper {
    fun addCallbackEventInSessions(appResponse: AppResponse)
}
