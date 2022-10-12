package com.simprints.id.orchestrator.cache

import com.simprints.core.domain.workflow.WorkflowCacheClearer
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.orchestrator.steps.Step

interface HotCache: WorkflowCacheClearer {

    var appRequest: AppRequest
    fun save(step: Step)
    fun load(): List<Step>

}
