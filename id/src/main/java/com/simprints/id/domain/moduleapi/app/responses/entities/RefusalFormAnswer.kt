package com.simprints.id.domain.moduleapi.app.responses.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class RefusalFormAnswer(val reason: RefusalFormReason, val optionalText: String = ""): Parcelable
