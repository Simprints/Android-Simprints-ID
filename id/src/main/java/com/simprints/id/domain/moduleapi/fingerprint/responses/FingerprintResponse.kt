package com.simprints.id.domain.moduleapi.fingerprint.responses

import android.os.Parcelable
import com.simprints.id.orchestrator.steps.Step.Result

interface FingerprintResponse: Parcelable, Result {

    val type: FingerprintTypeResponse

    companion object {
        const val BUNDLE_KEY = "FingerprintResponseBundleKey"
    }

}

enum class FingerprintTypeResponse {
    ENROL,
    MATCH,
    REFUSAL,
    ERROR
}
