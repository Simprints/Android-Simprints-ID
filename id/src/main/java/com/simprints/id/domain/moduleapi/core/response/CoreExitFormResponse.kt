package com.simprints.id.domain.moduleapi.core.response

import android.os.Parcelable
import com.simprints.id.data.exitform.ExitFormReason
import kotlinx.android.parcel.Parcelize

@Parcelize
class CoreExitFormResponse(val reason: ExitFormReason = ExitFormReason.OTHER, val optionalText: String = "") : Parcelable, CoreResponse
