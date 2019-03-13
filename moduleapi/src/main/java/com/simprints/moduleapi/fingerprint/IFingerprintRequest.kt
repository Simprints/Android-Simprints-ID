package com.simprints.moduleapi.fingerprint

import android.os.Parcelable


interface IFingerprintRequest : Parcelable {

    companion object {
        const val BUNDLE_KEY = "FingerprintRequestBundleKey"
    }

    val projectId: String
    val userId: String
    val moduleId: String
    val metadata: String
}
