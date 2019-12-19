package com.simprints.id.exitformhandler

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
open class ExitFormResult(val type: ExitFormType): Parcelable {

    companion object {
        const val EXIT_FORM_BUNDLE_KEY = "exit_form_from_core_bundle"
    }

    enum class ExitFormType {
        CORE_EXIT_FORM,
        CORE_FINGERPRINT_EXIT_FROM,
        CORE_FACE_EXIT_FORM
    }
}
