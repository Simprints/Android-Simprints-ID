package com.simprints.feature.exitform

import androidx.annotation.Keep
import com.simprints.feature.exitform.config.ExitFormOption
import java.io.Serializable

@Keep
data class ExitFormResult(
        val wasSubmitted: Boolean,
        val selectedOption: ExitFormOption? = null,
        val reason: String? = null,
) : Serializable {

    fun submittedOption() = selectedOption?.takeIf { wasSubmitted }
}
