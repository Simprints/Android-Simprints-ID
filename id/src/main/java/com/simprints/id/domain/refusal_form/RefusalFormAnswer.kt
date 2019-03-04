package com.simprints.id.domain.refusal_form

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

//If user taps back-back then nothing is submitted "reason" = null
@Parcelize
class RefusalFormAnswer(val reason: RefusalFormReason?, val optionalText: String = ""): Parcelable
