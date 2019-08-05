package com.simprints.id.domain.moduleapi.face.responses

import android.os.Parcelable
import com.simprints.id.orchestrator.modality.flows.interfaces.ModalityFlow

interface FaceResponse: Parcelable, ModalityFlow.Result {
    companion object {
        const val BUNDLE_KEY = "FaceResponseBundleKey"
    }
}
