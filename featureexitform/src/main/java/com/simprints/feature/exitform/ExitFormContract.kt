package com.simprints.feature.exitform

import android.os.Bundle
import com.simprints.feature.exitform.config.ExitFormOption

object ExitFormContract {

    const val EXIT_FORM_REQUEST = "exit_form_request"
    const val EXIT_FORM_SUBMITTED = "exit_form_submitted"
    const val EXIT_FORM_SELECTED_OPTION = "exit_form_option"
    const val EXIT_FORM_REASON = "exit_form_reason"

    /**
     * @return true if exit form was successfully submitted.
     */
    fun isFormSubmitted(data: Bundle): Boolean = data.getBoolean(EXIT_FORM_SUBMITTED, false)

    fun getFormOption(data: Bundle): ExitFormOption? =
        data.getSerializable(EXIT_FORM_SELECTED_OPTION) as? ExitFormOption

    fun getFormReason(data: Bundle): String? =
        data.getString(EXIT_FORM_REASON, null)
}
