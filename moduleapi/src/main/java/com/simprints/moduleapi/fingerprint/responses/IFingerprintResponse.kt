package com.simprints.moduleapi.fingerprint.responses

import android.os.Parcelable


interface IFingerprintResponse : Parcelable {

    companion object {
        const val BUNDLE_KEY = "IFingerprintResponseBundleKey"
    }

}
