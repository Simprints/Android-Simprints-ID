package com.simprints.feature.exitform.config

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class ExitFormConfiguration(
    val title: String?,
    @StringRes val titleRes: Int?,
    val backButton: String?,
    @StringRes val backButtonRes: Int?,
    val visibleOptions: Set<ExitFormOption>,
) : Parcelable
