package com.simprints.id.orchestrator.cache

import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.orchestrator.steps.Step

interface HotCache {

    var appRequest: AppRequest
    fun save(step: Step)
    fun load(): List<Step>

    fun clearSteps()
}
