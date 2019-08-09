package com.simprints.moduleapi.app.responses

import android.os.Parcelable
import com.simprints.moduleapi.IResponse


interface IAppResponse : Parcelable, IResponse {

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
    CONFIRMATION,
    ERROR
}
