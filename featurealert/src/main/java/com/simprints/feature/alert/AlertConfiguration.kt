package com.simprints.feature.alert

import android.os.Parcelable
import androidx.annotation.DrawableRes
import kotlinx.parcelize.Parcelize

// TODO define more detailed params
@Parcelize
data class AlertConfiguration(
    val title: String,
    val message: String,
    @DrawableRes val image: Int,
    val leftButtonText: String,
    val rightButtonText: String? = null,
) : Parcelable
