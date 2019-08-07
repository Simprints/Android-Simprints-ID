package com.simprints.id.orchestrator.steps

import android.content.Intent
import com.simprints.id.orchestrator.steps.Step.Result

interface StepProcessor {

    val requestCode: Int

    fun processResult(requestCode: Int, resultCode: Int, data: Intent?): Result?
}

fun StepProcessor.canProcessRequestCode(receivedRequestCode: Int) = receivedRequestCode == requestCode
