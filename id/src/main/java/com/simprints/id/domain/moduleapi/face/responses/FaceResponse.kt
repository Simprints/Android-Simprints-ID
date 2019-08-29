package com.simprints.id.domain.moduleapi.face.responses

import android.os.Parcelable
import com.simprints.id.orchestrator.steps.Step.Result
import com.simprints.moduleapi.face.responses.*

interface FaceResponse : Parcelable, Result {
    companion object {
        const val BUNDLE_KEY = "FaceResponseBundleKey"
    }
}

fun IFaceResponse.fromModuleApiToDomain(): FaceResponse =
    when (type) {
        IFaceResponseType.CAPTURE -> (this as IFaceCaptureResponse).fromModuleApiToDomain()
        IFaceResponseType.VERIFY -> (this as IFaceVerifyResponse).fromModuleApiToDomain()
        IFaceResponseType.IDENTIFY -> (this as IFaceIdentifyResponse).fromModuleApiToDomain()
        IFaceResponseType.EXIT_FORM -> TODO() //STOPSHIP
        IFaceResponseType.ERROR -> TODO() //STOPSHIP
    }


