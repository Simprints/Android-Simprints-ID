package com.simprints.id.domain.moduleapi.fingerprint.responses

import android.os.Parcelable
import com.simprints.id.orchestrator.modality.flows.interfaces.ModalityFlow

interface FingerprintResponse: Parcelable, ModalityFlow.Response {
    val type: FingerprintTypeResponse

    companion object {
        const val BUNDLE_KEY = "FingerprintResponseBundleKey"
    }
}

enum class FingerprintTypeResponse {
    ENROL,
    IDENTIFY,
    VERIFY,
    REFUSAL,
    ERROR
}
