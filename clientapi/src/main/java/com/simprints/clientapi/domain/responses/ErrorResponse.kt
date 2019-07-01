package com.simprints.clientapi.domain.responses

import android.os.Parcelable
import com.simprints.clientapi.activities.errors.ClientApiAlert
import com.simprints.clientapi.domain.responses.ErrorResponse.Reason.Companion.fromAlertTypeToDomain
import com.simprints.clientapi.domain.responses.ErrorResponse.Reason.Companion.fromModuleApiToDomain
import com.simprints.moduleapi.app.responses.IAppErrorReason
import com.simprints.moduleapi.app.responses.IAppErrorResponse
import kotlinx.android.parcel.Parcelize
import timber.log.Timber

@Parcelize
data class ErrorResponse(val reason: Reason) : Parcelable {

    constructor(response: IAppErrorResponse) : this(fromModuleApiToDomain(response.reason))

    constructor(response: ClientApiAlert) : this(fromAlertTypeToDomain(response))

    fun skipCheckAfterError(): Boolean =
        when (reason) {
            Reason.UNEXPECTED_ERROR,
            Reason.DIFFERENT_PROJECT_ID_SIGNED_IN,
            Reason.DIFFERENT_USER_ID_SIGNED_IN,
            Reason.INVALID_CLIENT_REQUEST,
            Reason.INVALID_METADATA,
            Reason.INVALID_MODULE_ID,
            Reason.INVALID_PROJECT_ID,
            Reason.INVALID_SELECTED_ID,
            Reason.INVALID_SESSION_ID,
            Reason.INVALID_USER_ID,
            Reason.INVALID_VERIFY_ID -> true
            else -> false
        }.also {
            Timber.d("TEST_TEST: $reason")
        }

    enum class Reason {
        INVALID_CLIENT_REQUEST,
        INVALID_METADATA,
        INVALID_MODULE_ID,
        INVALID_PROJECT_ID,
        INVALID_SELECTED_ID,
        INVALID_SESSION_ID,
        INVALID_USER_ID,
        INVALID_VERIFY_ID,
        DIFFERENT_PROJECT_ID_SIGNED_IN,
        DIFFERENT_USER_ID_SIGNED_IN,
        GUID_NOT_FOUND_ONLINE,
        GUID_NOT_FOUND_OFFLINE,
        UNEXPECTED_ERROR,
        BLUETOOTH_NOT_SUPPORTED,
        SCANNER_LOW_BATTERY,
        UNKNOWN_BLUETOOTH_ISSUE;

        companion object {

            fun fromModuleApiToDomain(iAppErrorReason: IAppErrorReason): Reason =
                when (iAppErrorReason) {
                    IAppErrorReason.DIFFERENT_PROJECT_ID_SIGNED_IN -> DIFFERENT_PROJECT_ID_SIGNED_IN
                    IAppErrorReason.DIFFERENT_USER_ID_SIGNED_IN -> DIFFERENT_USER_ID_SIGNED_IN
                    IAppErrorReason.GUID_NOT_FOUND_ONLINE -> GUID_NOT_FOUND_ONLINE
                    IAppErrorReason.GUID_NOT_FOUND_OFFLINE -> GUID_NOT_FOUND_OFFLINE
                    IAppErrorReason.UNEXPECTED_ERROR -> UNEXPECTED_ERROR
                    IAppErrorReason.BLUETOOTH_NOT_SUPPORTED -> BLUETOOTH_NOT_SUPPORTED
                    IAppErrorReason.SCANNER_LOW_BATTERY -> SCANNER_LOW_BATTERY
                    IAppErrorReason.UNKNOWN_BLUETOOTH_ISSUE -> UNKNOWN_BLUETOOTH_ISSUE
                }

            fun fromAlertTypeToDomain(clientApiAlert: ClientApiAlert): Reason =
                when (clientApiAlert) {
                    ClientApiAlert.INVALID_CLIENT_REQUEST -> INVALID_CLIENT_REQUEST
                    ClientApiAlert.INVALID_METADATA -> INVALID_METADATA
                    ClientApiAlert.INVALID_MODULE_ID -> INVALID_MODULE_ID
                    ClientApiAlert.INVALID_PROJECT_ID -> INVALID_PROJECT_ID
                    ClientApiAlert.INVALID_SELECTED_ID -> INVALID_SELECTED_ID
                    ClientApiAlert.INVALID_SESSION_ID -> INVALID_SESSION_ID
                    ClientApiAlert.INVALID_USER_ID -> INVALID_USER_ID
                    ClientApiAlert.INVALID_VERIFY_ID -> INVALID_VERIFY_ID
                }
        }
    }
}
