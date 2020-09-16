package com.simprints.id.orchestrator.responsebuilders.adjudication

import com.simprints.id.orchestrator.steps.Step

interface EnrolResponseAdjudicationHelper {
    fun getAdjudicationAction(isEnrolmentPlus: Boolean, steps: List<Step>):
        EnrolAdjudicationAction
}
