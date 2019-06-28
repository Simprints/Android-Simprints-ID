package com.simprints.fingerprint.activities.launch.result

import android.os.Parcelable
import com.simprints.fingerprint.activities.ActResult
import kotlinx.android.parcel.Parcelize

@Parcelize
class LaunchActResult : ActResult, Parcelable {

    companion object {
        const val BUNDLE_KEY = "LaunchResultKey"
    }
}
