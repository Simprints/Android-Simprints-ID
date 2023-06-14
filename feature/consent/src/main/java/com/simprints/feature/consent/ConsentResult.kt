package com.simprints.feature.consent

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class ConsentResult(
    val accepted: Boolean,
) : Parcelable
