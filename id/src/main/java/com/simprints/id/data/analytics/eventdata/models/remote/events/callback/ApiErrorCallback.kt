package com.simprints.id.data.analytics.eventdata.models.remote.events.callback

import com.simprints.id.data.analytics.eventdata.models.domain.events.callback.ErrorCallbackEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.callback.ErrorCallbackEvent.Reason.*
import io.realm.internal.Keep

@Keep
class ApiErrorCallback(val reason: ApiReason) : ApiCallback(ApiCallbackType.ERROR) {

    enum class ApiReason {
        DIFFERENT_PROJECT_ID_SIGNED_IN,
        DIFFERENT_USER_ID_SIGNED_IN,
        GUID_NOT_FOUND_ONLINE,
        GUID_NOT_FOUND_OFFLINE,
        UNEXPECTED_ERROR,
        BLUETOOTH_NOT_SUPPORTED,
        SCANNER_LOW_BATTERY,
        UNKNOWN_BLUETOOTH_ISSUE
    }
}

fun ErrorCallbackEvent.Reason.fromDomainToApi() =
    when(this) {
        DIFFERENT_PROJECT_ID_SIGNED_IN -> ApiErrorCallback.ApiReason.DIFFERENT_PROJECT_ID_SIGNED_IN
        DIFFERENT_USER_ID_SIGNED_IN -> ApiErrorCallback.ApiReason.DIFFERENT_USER_ID_SIGNED_IN
        GUID_NOT_FOUND_ONLINE -> ApiErrorCallback.ApiReason.GUID_NOT_FOUND_ONLINE
        GUID_NOT_FOUND_OFFLINE -> ApiErrorCallback.ApiReason.GUID_NOT_FOUND_ONLINE
        UNEXPECTED_ERROR -> ApiErrorCallback.ApiReason.UNEXPECTED_ERROR
        BLUETOOTH_NOT_SUPPORTED -> ApiErrorCallback.ApiReason.BLUETOOTH_NOT_SUPPORTED
        SCANNER_LOW_BATTERY -> ApiErrorCallback.ApiReason.SCANNER_LOW_BATTERY
        UNKNOWN_BLUETOOTH_ISSUE -> ApiErrorCallback.ApiReason.UNKNOWN_BLUETOOTH_ISSUE
    }
