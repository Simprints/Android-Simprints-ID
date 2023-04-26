package com.simprints.id.orchestrator.steps.core.response

import android.os.Parcelable
import com.simprints.id.data.exitform.ExitFormReason
import kotlinx.parcelize.Parcelize

@Parcelize
class ExitFormResponse(
    val reason: ExitFormReason = ExitFormReason.OTHER,
    val optionalText: String = ""
) : Parcelable, CoreResponse(type = CoreResponseType.EXIT_FORM)
