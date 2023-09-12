package com.simprints.feature.exitform

import android.os.Parcelable
import androidx.annotation.Keep
import com.simprints.feature.exitform.config.ExitFormOption
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class ExitFormResult(
        val wasSubmitted: Boolean,
        val selectedOption: ExitFormOption? = null,
        val reason: String? = null,
) : Parcelable {

    fun submittedOption() = selectedOption?.takeIf { wasSubmitted } ?: ExitFormOption.Other
}
