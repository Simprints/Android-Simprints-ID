package com.simprints.id.orchestrator.steps.core

import android.content.Intent
import com.simprints.id.orchestrator.steps.Step

interface CoreStepProcessor {

    fun buildStepEnrolOrIdentify(): List<Step>

    fun buildStepVerify(): List<Step>

    fun processResult(resultCode: Int, data: Intent?): Step.Result?
}
