package com.simprints.id.domain.moduleapi.app.responses

import android.os.Parcelable

interface AppResponse: Parcelable {

    val type: AppResponseType

    companion object {
        const val BUNDLE_KEY = "ApiResponse"
    }
}

enum class AppResponseType {
    ENROL,
    IDENTIFY,
    REFUSAL,
    VERIFY,
    ERROR
}
