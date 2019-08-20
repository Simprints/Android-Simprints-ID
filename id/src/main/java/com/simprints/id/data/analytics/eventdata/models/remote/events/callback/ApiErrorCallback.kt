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
        @Deprecated("User can't leave the app anymore in case of GUID_NOT_FOUND_OFFLINE. He exits through the ExitForm.")
        GUID_NOT_FOUND_OFFLINE,
        UNEXPECTED_ERROR,
        BLUETOOTH_NOT_SUPPORTED,
        @Deprecated("User can't leave the app anymore in case of SCANNER_LOW_BATTERY. He exits through the ExitForm.")
        SCANNER_LOW_BATTERY,
        @Deprecated("Fingerprint module doesn't triggers it anymore")
        UNKNOWN_BLUETOOTH_ISSUE,
        LOGIN_NOT_COMPLETE
    }
}

fun ErrorCallbackEvent.Reason.fromDomainToApi() =
    when(this) {
        DIFFERENT_PROJECT_ID_SIGNED_IN -> ApiErrorCallback.ApiReason.DIFFERENT_PROJECT_ID_SIGNED_IN
        DIFFERENT_USER_ID_SIGNED_IN -> ApiErrorCallback.ApiReason.DIFFERENT_USER_ID_SIGNED_IN
        GUID_NOT_FOUND_ONLINE -> ApiErrorCallback.ApiReason.GUID_NOT_FOUND_ONLINE
        UNEXPECTED_ERROR -> ApiErrorCallback.ApiReason.UNEXPECTED_ERROR
        BLUETOOTH_NOT_SUPPORTED -> ApiErrorCallback.ApiReason.BLUETOOTH_NOT_SUPPORTED
        LOGIN_NOT_COMPLETE -> ApiErrorCallback.ApiReason.LOGIN_NOT_COMPLETE
    }
