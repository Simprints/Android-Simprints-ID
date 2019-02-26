package com.simprints.id.session.callout

import android.content.Intent
import com.simprints.id.domain.Constants

@Deprecated("Use IdRequests")
enum class CalloutAction(private val action: String) {

    REGISTER(Constants.SIMPRINTS_REGISTER_INTENT),
    IDENTIFY(Constants.SIMPRINTS_IDENTIFY_INTENT),
    UPDATE(Constants.SIMPRINTS_UPDATE_INTENT),
    VERIFY(Constants.SIMPRINTS_VERIFY_INTENT),
    MISSING("Missing"),
    INVALID("Invalid");

    companion object {

        const val SIMPRINTS_REGISTER_INTENT = "com.simprints.id.REGISTER"
        const val SIMPRINTS_IDENTIFY_INTENT = "com.simprints.id.IDENTIFY"
        const val SIMPRINTS_UPDATE_INTENT = "com.simprints.id.UPDATE"
        const val SIMPRINTS_VERIFY_INTENT = "com.simprints.id.VERIFY"
        const val SIMPRINTS_SELECT_GUID_INTENT = "com.simprints.id.CONFIRM_IDENTITY"

        val validValues = values()
            .filterNot { it == MISSING || it == INVALID }

        val Intent?.calloutAction: CalloutAction
            get() =
                if (this?.action == null ) {
                    MISSING
                } else {
                    REVERSE_LOOKUP[action] ?: INVALID
                }

        private val REVERSE_LOOKUP: Map<String, CalloutAction> =
                values().map { callout -> Pair(callout.action, callout) }
                        .toMap()

    }

    override fun toString(): String {
        return action
    }

}
