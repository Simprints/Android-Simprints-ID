package com.simprints.id.domain.moduleapi.face.responses

import android.os.Parcelable
import com.simprints.id.domain.modal.ModalResponse

interface FaceResponse: Parcelable, ModalResponse {
    companion object {
        const val BUNDLE_KEY = "FaceResponseBundleKey"
    }
}
