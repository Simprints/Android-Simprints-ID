package com.simprints.id.domain.calloutValidation

import com.simprints.libsimprints.Constants
import java.security.InvalidParameterException

enum class CalloutType(val intentAction: String) {

    REGISTER(Constants.SIMPRINTS_REGISTER_INTENT),
    IDENTIFY(Constants.SIMPRINTS_IDENTIFY_INTENT),
    UPDATE(Constants.SIMPRINTS_UPDATE_INTENT),
    VERIFY(Constants.SIMPRINTS_VERIFY_INTENT),
    INVALID_OR_MISSING("Invalid or missing");

    companion object {

        private val ACTION_TO_CALLOUT_TYPE: Map<String, CalloutType> =
                values().map { callout -> Pair(callout.intentAction, callout) }
                        .toMap()

        fun forAction(action: String): CalloutType =
                ACTION_TO_CALLOUT_TYPE[action] ?: throw InvalidParameterException()

        fun forActionOrDefault(action: String, defaultValue: CalloutType): CalloutType =
                ACTION_TO_CALLOUT_TYPE[action] ?: defaultValue

    }

}
