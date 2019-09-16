package com.simprints.id.domain.moduleapi.core.response

import android.os.Parcelable
import com.simprints.id.data.exitform.FingerprintExitFormReason
import kotlinx.android.parcel.Parcelize

@Parcelize
class FingerprintExitFormResponse(val reason: FingerprintExitFormReason = FingerprintExitFormReason.OTHER,
                                  val optionalText: String = "") : Parcelable, CoreResponse
