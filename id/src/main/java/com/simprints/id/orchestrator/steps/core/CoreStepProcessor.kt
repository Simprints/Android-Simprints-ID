package com.simprints.id.orchestrator.steps.core

import android.content.Intent
import com.simprints.id.orchestrator.steps.Step

interface CoreStepProcessor {

    fun buildStepEnrolOrIdentify(projectId: String, userId: String,
                                 moduleId: String, metadata: String): List<Step>

    fun buildStepVerify(projectId: String, userId: String,
                        moduleId: String, metadata: String): List<Step>

    fun processResult(resultCode: Int, data: Intent?): Step.Result?
}
