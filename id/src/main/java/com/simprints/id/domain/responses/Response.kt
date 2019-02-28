package com.simprints.id.domain.responses

import android.os.Parcelable

interface Response: Parcelable {
    companion object {
        const val BUNDLE_KEY = "ApiResponse"
    }
}
