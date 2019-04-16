package com.simprints.id.domain.moduleapi.face.responses

import android.os.Parcelable
import com.simprints.id.domain.modality.ModalityResponse

interface FaceResponse: Parcelable, ModalityResponse {
    companion object {
        const val BUNDLE_KEY = "FaceResponseBundleKey"
    }
}
