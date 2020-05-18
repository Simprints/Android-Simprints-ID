package com.simprints.clientapi.domain.responses

import android.os.Parcelable
import com.simprints.clientapi.activities.errors.ClientApiAlert
import com.simprints.clientapi.domain.responses.ErrorResponse.Reason.Companion.fromAlertTypeToDomain
import com.simprints.clientapi.domain.responses.ErrorResponse.Reason.Companion.fromModuleApiToDomain
import com.simprints.moduleapi.app.responses.IAppErrorReason
import com.simprints.moduleapi.app.responses.IAppErrorResponse
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ErrorResponse(val reason: Reason) : Parcelable {

    constructor(response: IAppErrorResponse) : this(fromModuleApiToDomain(response.reason))

    constructor(response: ClientApiAlert) : this(fromAlertTypeToDomain(response))

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
        LOGIN_NOT_COMPLETE,
        UNEXPECTED_ERROR,
        BLUETOOTH_NOT_SUPPORTED,
        ROOTED_DEVICE,
        INVALID_STATE_FOR_INTENT_ACTION;

        companion object {

            fun fromModuleApiToDomain(iAppErrorReason: IAppErrorReason): Reason =
                when (iAppErrorReason) {
                    IAppErrorReason.DIFFERENT_PROJECT_ID_SIGNED_IN -> DIFFERENT_PROJECT_ID_SIGNED_IN
                    IAppErrorReason.DIFFERENT_USER_ID_SIGNED_IN -> DIFFERENT_USER_ID_SIGNED_IN
                    IAppErrorReason.GUID_NOT_FOUND_ONLINE -> GUID_NOT_FOUND_ONLINE
                    IAppErrorReason.UNEXPECTED_ERROR -> UNEXPECTED_ERROR
                    IAppErrorReason.BLUETOOTH_NOT_SUPPORTED -> BLUETOOTH_NOT_SUPPORTED
                    IAppErrorReason.LOGIN_NOT_COMPLETE -> LOGIN_NOT_COMPLETE
                    IAppErrorReason.ROOTED_DEVICE -> ROOTED_DEVICE
                    IAppErrorReason.ENROLMENT_LAST_BIOMETRIC_FAILED -> INVALID_STATE_FOR_INTENT_ACTION
                }

            fun fromAlertTypeToDomain(clientApiAlert: ClientApiAlert): Reason =
                when (clientApiAlert) {
                    ClientApiAlert.INVALID_METADATA -> INVALID_METADATA
                    ClientApiAlert.INVALID_MODULE_ID -> INVALID_MODULE_ID
                    ClientApiAlert.INVALID_PROJECT_ID -> INVALID_PROJECT_ID
                    ClientApiAlert.INVALID_SELECTED_ID -> INVALID_SELECTED_ID
                    ClientApiAlert.INVALID_SESSION_ID -> INVALID_SESSION_ID
                    ClientApiAlert.INVALID_USER_ID -> INVALID_USER_ID
                    ClientApiAlert.INVALID_VERIFY_ID -> INVALID_VERIFY_ID
                    ClientApiAlert.ROOTED_DEVICE -> ROOTED_DEVICE
                    ClientApiAlert.INVALID_STATE_FOR_INTENT_ACTION -> INVALID_STATE_FOR_INTENT_ACTION
                }
        }
    }
}
