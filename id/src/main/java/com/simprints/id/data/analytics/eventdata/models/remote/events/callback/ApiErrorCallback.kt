package com.simprints.id.data.analytics.eventdata.models.remote.events.callback

import com.simprints.id.data.analytics.eventdata.models.domain.events.callback.ErrorCallbackEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.callback.ErrorCallbackEvent.Reason.*
import io.realm.internal.Keep

@Keep
class ApiErrorCallback(val reason: ApiReason) : ApiCallback(ApiCallbackType.ERROR) {

    @Keep
    enum class ApiReason {
        DIFFERENT_PROJECT_ID_SIGNED_IN,
        DIFFERENT_USER_ID_SIGNED_IN,
        GUID_NOT_FOUND_ONLINE,
        GUID_NOT_FOUND_OFFLINE, //Deprecated: fingerprint module doesn't return it anymore.
        UNEXPECTED_ERROR,
        BLUETOOTH_NOT_SUPPORTED,
        SCANNER_LOW_BATTERY, //Deprecated: fingerprint module doesn't return it anymore.
        UNKNOWN_BLUETOOTH_ISSUE //Deprecated: fingerprint module doesn't return it anymore.
    }
}

fun ErrorCallbackEvent.Reason.fromDomainToApi() =
    when(this) {
        DIFFERENT_PROJECT_ID_SIGNED_IN -> ApiErrorCallback.ApiReason.DIFFERENT_PROJECT_ID_SIGNED_IN
        DIFFERENT_USER_ID_SIGNED_IN -> ApiErrorCallback.ApiReason.DIFFERENT_USER_ID_SIGNED_IN
        GUID_NOT_FOUND_ONLINE -> ApiErrorCallback.ApiReason.GUID_NOT_FOUND_ONLINE
        UNEXPECTED_ERROR -> ApiErrorCallback.ApiReason.UNEXPECTED_ERROR
        BLUETOOTH_NOT_SUPPORTED -> ApiErrorCallback.ApiReason.BLUETOOTH_NOT_SUPPORTED
    }
