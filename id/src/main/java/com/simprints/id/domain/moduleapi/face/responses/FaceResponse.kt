package com.simprints.id.domain.moduleapi.face.responses

import android.os.Parcelable
import com.simprints.id.orchestrator.steps.Step.Result
import com.simprints.moduleapi.face.responses.*

interface FaceResponse : Parcelable, Result {

    val type: FaceResponseType

    companion object {
        const val BUNDLE_KEY = "FaceResponseBundleKey"
    }

}

fun IFaceResponse.fromModuleApiToDomain(): FaceResponse =
    when (type) {
        IFaceResponseType.CAPTURE -> (this as IFaceCaptureResponse).fromModuleApiToDomain()
        IFaceResponseType.MATCH -> (this as IFaceMatchResponse).fromModuleApiToDomain()
        IFaceResponseType.EXIT_FORM -> (this as IFaceExitFormResponse).fromModuleApiToDomain()
        IFaceResponseType.ERROR -> (this as IFaceErrorResponse).fromModuleApiToDomain()
    }


enum class FaceResponseType {
    CAPTURE,
    MATCH,
    EXIT_FORM,
    ERROR
}
