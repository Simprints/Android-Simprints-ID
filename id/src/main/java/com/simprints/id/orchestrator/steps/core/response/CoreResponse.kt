package com.simprints.id.orchestrator.steps.core.response

import android.os.Parcelable
import com.simprints.id.orchestrator.steps.Step
import kotlinx.parcelize.Parcelize

@Parcelize
open class CoreResponse(val type: CoreResponseType): Parcelable, Step.Result {
    companion object {
        const val CORE_STEP_BUNDLE = "core_step_bundle"
    }
}

enum class CoreResponseType {
    CONSENT,
    CORE_EXIT_FORM,
    FINGERPRINT_EXIT_FORM,
    FACE_EXIT_FORM,
    FETCH_GUID,
    GUID_SELECTION,
    SETUP,
    ENROL_LAST_BIOMETRICS
}
