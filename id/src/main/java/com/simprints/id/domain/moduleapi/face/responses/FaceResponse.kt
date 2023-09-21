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


enum class FaceResponseType {
    CAPTURE,
    MATCH,
    EXIT_FORM,
    ERROR,
    CONFIGURATION
}
