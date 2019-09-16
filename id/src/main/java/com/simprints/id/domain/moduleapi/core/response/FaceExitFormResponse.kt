package com.simprints.id.domain.moduleapi.core.response

import android.os.Parcelable
import com.simprints.id.data.exitform.FaceExitFormReason
import kotlinx.android.parcel.Parcelize

@Parcelize
class FaceExitFormResponse(val reason: FaceExitFormReason = FaceExitFormReason.OTHER,
                           val optionalText: String = "") : Parcelable, CoreResponse
