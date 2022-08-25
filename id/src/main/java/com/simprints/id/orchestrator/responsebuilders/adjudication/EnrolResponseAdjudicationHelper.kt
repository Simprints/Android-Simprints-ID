package com.simprints.id.orchestrator.responsebuilders.adjudication

import com.simprints.id.orchestrator.steps.Step
import com.simprints.infra.config.domain.models.ProjectConfiguration

interface EnrolResponseAdjudicationHelper {
    fun getAdjudicationAction(
        projectConfiguration: ProjectConfiguration,
        steps: List<Step>
    ): EnrolAdjudicationAction
}
