package com.simprints.id.domain.moduleapi.core.response

import android.os.Parcelable
import com.simprints.id.data.exitform.FingerprintExitFormReason
import kotlinx.android.parcel.Parcelize

@Parcelize
class CoreFingerprintExitFormResponse(val reason: FingerprintExitFormReason = FingerprintExitFormReason.OTHER,
                                      val optionalText: String = "") : Parcelable, CoreResponse(type= CoreResponseType.FINGERPRINT_EXIT_FORM)
