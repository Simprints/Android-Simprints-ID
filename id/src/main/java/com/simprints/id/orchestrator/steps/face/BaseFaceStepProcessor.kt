package com.simprints.id.orchestrator.steps.face

import android.content.Intent
import com.simprints.id.domain.moduleapi.face.FaceToDomainResponse.fromFaceToDomainResponse
import com.simprints.id.orchestrator.steps.Step
import com.simprints.id.orchestrator.steps.Step.Request
import com.simprints.id.orchestrator.steps.StepProcessor
import com.simprints.moduleapi.face.requests.IFaceRequest
import com.simprints.moduleapi.face.responses.IFaceResponse
import com.simprints.moduleapi.face.requests.IFaceRequest.Companion.BUNDLE_KEY as REQUEST_BUNDLE_KEY
import com.simprints.moduleapi.face.responses.IFaceResponse.Companion.BUNDLE_KEY as RESPONSE_BUNDLE_KEY

abstract class BaseFaceStepProcessor : StepProcessor {

    companion object {
        const val FACE_REQUEST_CODE = 200
        private const val ACTIVITY_CLASS_NAME = "com.simprints.face.activities.FaceCaptureActivity"
    }

    protected fun buildIntent(faceRequest: IFaceRequest,
                              packageName: String): Request =
        with(Intent().setClassName(packageName, ACTIVITY_CLASS_NAME)) {
            putExtra(REQUEST_BUNDLE_KEY, faceRequest)
            Request(requestCode, this)
        }

    override fun processResult(requestCode: Int, resultCode: Int, data: Intent?): Step.Result? =
        data?.getParcelableExtra<IFaceResponse>(RESPONSE_BUNDLE_KEY)?.let {
            fromFaceToDomainResponse(it)
        }
}
