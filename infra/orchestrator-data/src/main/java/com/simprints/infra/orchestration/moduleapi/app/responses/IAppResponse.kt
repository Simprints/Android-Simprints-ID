package com.simprints.infra.orchestration.moduleapi.app.responses

import android.os.Parcelable

interface IAppResponse : Parcelable {

    val type: IAppResponseType

}

enum class IAppResponseType {
    ENROL,
    ENROL_LAST_BIOMETRICS,
    VERIFY,
    REFUSAL,
    IDENTIFY,
    CONFIRMATION,
    ERROR
}
