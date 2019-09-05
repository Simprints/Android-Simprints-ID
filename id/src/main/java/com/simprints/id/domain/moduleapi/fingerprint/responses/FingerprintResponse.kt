package com.simprints.id.domain.moduleapi.fingerprint.responses

import android.os.Parcelable
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.orchestrator.steps.Step.Result

abstract class FingerprintResponse: Parcelable, Result {
    abstract val type: FingerprintTypeResponse

    override fun toJson(): String = JsonHelper.toJson(this)

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
