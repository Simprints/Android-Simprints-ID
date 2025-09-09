package com.simprints.core.domain.sample

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Identity(
    val subjectId: String,
    val samples: List<Sample>,
) : Parcelable
