package com.simprints.id.orchestrator.steps.core.response

import android.os.Parcelable
import com.simprints.id.data.exitform.CoreExitFormReason
import kotlinx.parcelize.Parcelize

@Parcelize
class CoreExitFormResponse(val reason: CoreExitFormReason = CoreExitFormReason.OTHER,
                           val optionalText: String = "") : Parcelable, CoreResponse(type = CoreResponseType.CORE_EXIT_FORM)
