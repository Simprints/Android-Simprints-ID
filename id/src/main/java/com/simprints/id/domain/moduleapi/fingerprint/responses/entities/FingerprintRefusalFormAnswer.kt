package com.simprints.id.domain.moduleapi.fingerprint.responses.entities

import android.os.Parcelable
import com.simprints.id.domain.moduleapi.app.responses.entities.RefusalFormReason
import kotlinx.android.parcel.Parcelize

@Parcelize
class FingerprintRefusalFormAnswer(val reason: RefusalFormReason?, val optionalText: String = ""): Parcelable
