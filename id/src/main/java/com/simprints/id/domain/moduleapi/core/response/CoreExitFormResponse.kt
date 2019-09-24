package com.simprints.id.domain.moduleapi.core.response

import android.os.Parcelable
import com.simprints.id.data.exitform.CoreExitFormReason
import kotlinx.android.parcel.Parcelize

@Parcelize
class CoreExitFormResponse(val reason: CoreExitFormReason = CoreExitFormReason.OTHER,
                           val optionalText: String = "") : Parcelable, CoreResponse(type = CoreResponseType.CORE_EXIT_FORM)
