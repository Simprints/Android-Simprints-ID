package com.simprints.id.domain.moduleapi.face.responses

import android.os.Parcelable
import com.simprints.id.orchestrator.steps.Step.Result

interface FaceResponse : Parcelable, Result {
    companion object {
        const val BUNDLE_KEY = "FaceResponseBundleKey"
    }
}
