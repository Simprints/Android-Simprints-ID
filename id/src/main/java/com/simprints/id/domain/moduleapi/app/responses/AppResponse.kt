package com.simprints.id.domain.moduleapi.app.responses

import android.os.Parcelable

interface AppResponse: Parcelable {
    companion object {
        const val BUNDLE_KEY = "ApiResponse"
    }
}
