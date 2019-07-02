package com.simprints.fingerprint.activities.collect.request

import android.os.Parcelable
import com.simprints.fingerprint.activities.ActRequest
import com.simprints.fingerprint.activities.collect.models.FingerIdentifier
import com.simprints.fingerprint.data.domain.Action
import kotlinx.android.parcel.Parcelize

@Parcelize
class CollectFingerprintsActRequest(val projectId: String,
                                    val userId: String,
                                    val moduleId: String,
                                    val action: Action, // To know which title to show, also currently saves person in DB if enrol
                                    val language: String,
                                    val fingerStatus: Map<FingerIdentifier, Boolean>) : ActRequest, Parcelable {

    companion object {
        const val BUNDLE_KEY = "CollectRequestBundleKey"
    }
}
