package com.simprints.id.domain.moduleapi.core.response

import android.os.Parcelable
import com.simprints.id.orchestrator.steps.Step
import kotlinx.android.parcel.Parcelize

@Parcelize
open class CoreResponse(val type: CoreResponseType): Parcelable, Step.Result {
    companion object {
        const val CORE_STEP_BUNDLE = "core_step_bundle"
    }
}

enum class CoreResponseType {
    CONSENT,
    EXIT_FORM,
    FETCH_GUID
}
