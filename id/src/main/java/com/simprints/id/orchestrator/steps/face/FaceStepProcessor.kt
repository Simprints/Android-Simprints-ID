package com.simprints.id.orchestrator.steps.face

import android.content.Intent
import com.simprints.id.orchestrator.steps.Step

interface FaceStepProcessor {

    fun buildStepEnrol(projectId: String,
                       userId: String,
                       moduleId: String): Step

    fun buildStepIdentify(projectId: String,
                          userId: String,
                          moduleId: String): Step

    fun buildStepVerify(projectId: String,
                        userId: String,
                        moduleId: String): Step

    fun processResult(requestCode: Int,
                      resultCode: Int,
                      data: Intent?): Step.Result?
}
