package com.simprints.id.activities.login.response

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class LoginActivityResponse: Parcelable{
    companion object {
        const val RESULT_CODE_LOGIN_SUCCEED: Int = 1
        const val BUNDLE_KEY = "LoginActResponseBundleKey"
    }
}
