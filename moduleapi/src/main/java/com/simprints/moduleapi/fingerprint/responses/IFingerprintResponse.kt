package com.simprints.moduleapi.fingerprint.responses

import android.os.Parcelable
import com.simprints.moduleapi.IResponse


interface IFingerprintResponse : Parcelable, IResponse {

    val type: IFingerprintResponseType

    companion object {
        const val BUNDLE_KEY = "IFingerprintResponseBundleKey"
    }

}

enum class IFingerprintResponseType {
    ENROL,
    MATCH,
    REFUSAL,
    ERROR
}
