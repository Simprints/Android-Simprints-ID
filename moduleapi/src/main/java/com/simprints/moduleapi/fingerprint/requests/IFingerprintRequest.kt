package com.simprints.moduleapi.fingerprint.requests

import android.os.Parcelable
import com.simprints.moduleapi.IRequest


interface IFingerprintRequest : Parcelable, IRequest {

    companion object {
        const val BUNDLE_KEY = "FingerprintRequestBundleKey"
    }
}
