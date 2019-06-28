package com.simprints.fingerprint.activities.launch.request

import android.os.Parcelable
import com.simprints.fingerprint.activities.ActRequest
import kotlinx.android.parcel.Parcelize

@Parcelize
class LaunchActRequest(
    val projectId: String,
    val action: Action, // To know which version of the consent text to show
    val language: String,
    val logoExists: Boolean,
    val programName: String,
    val organizationName: String,
    val verifyGuid: String? = null
) : ActRequest, Parcelable {

    @Parcelize
    enum class Action : Parcelable {
        ENROL, IDENTIFY, VERIFY
    }

    companion object {
        const val BUNDLE_KEY = "LaunchRequestKey"
    }
}
