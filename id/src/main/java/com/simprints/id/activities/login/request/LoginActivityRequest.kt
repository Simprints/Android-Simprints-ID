package com.simprints.id.activities.login.request

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class LoginActivityRequest(val projectIdFromIntent: String,
                           val userIdFromIntent: String): Parcelable {
    companion object {
        const val BUNDLE_KEY = "LoginActivityRequestBundleKey"
    }
}
