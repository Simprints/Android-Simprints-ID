package com.simprints.moduleapi.app.responses

import android.os.Parcelable


interface IAppResponse : Parcelable {

    val type: IAppResponseType

    companion object {
        const val BUNDLE_KEY = "clientResponseBundleKey"
    }

}

enum class IAppResponseType {
    ENROL,
    VERIFY,
    REFUSAL,
    IDENTIFY,
    ERROR
}
