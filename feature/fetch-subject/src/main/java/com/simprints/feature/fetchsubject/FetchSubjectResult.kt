package com.simprints.feature.fetchsubject

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class FetchSubjectResult(
    val found: Boolean,
    val wasOnline: Boolean = false
) : Parcelable
