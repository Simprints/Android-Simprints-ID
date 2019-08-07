package com.simprints.id.orchestrator.modality.steps.face

import android.content.Intent
import com.simprints.id.orchestrator.modality.steps.Step.Request
import com.simprints.id.orchestrator.modality.steps.StepProcessor
import com.simprints.moduleapi.face.requests.IFaceRequest

abstract class BaseFaceStepProcessor : StepProcessor {

    companion object {
        const val FACE_REQUEST_CODE = 200
        private const val ACTIVITY_CLASS_NAME = "com.simprints.face.activities.FaceCaptureActivity"
    }

    protected fun buildIntent(faceRequest: IFaceRequest,
                              packageName: String): Request {
        val intent = Intent().setClassName(packageName, ACTIVITY_CLASS_NAME)
        intent.putExtra(IFaceRequest.BUNDLE_KEY, faceRequest)
        return Request(requestCode, intent)
    }
}
