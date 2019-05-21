package com.simprints.moduleapi.fingerprint.responses

import android.os.Parcelable


interface IFingerprintResponse : Parcelable {

    val type: IFingerprintResponseType

    companion object {
        const val BUNDLE_KEY = "IFingerprintResponseBundleKey"
    }

}

enum class IFingerprintResponseType {
    ENROL,
    VERIFY,
    IDENTIFY,
    REFUSAL,
    ERROR
}
