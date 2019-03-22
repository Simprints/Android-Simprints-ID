package com.simprints.id.domain.moduleapi.fingerprint.responses.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class FingerprintRefusalFormAnswer(val reason: FingerprintRefusalFormReason?,
                                   val optionalText: String = ""): Parcelable

