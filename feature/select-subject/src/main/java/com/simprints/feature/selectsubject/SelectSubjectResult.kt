package com.simprints.feature.selectsubject

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class SelectSubjectResult(
    val success: Boolean,
) : Parcelable

