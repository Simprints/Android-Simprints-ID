package com.simprints.id.domain.callout

import android.content.Intent
import com.simprints.libsimprints.Constants

enum class CalloutAction(private val action: String) {

    REGISTER(Constants.SIMPRINTS_REGISTER_INTENT),
    IDENTIFY(Constants.SIMPRINTS_IDENTIFY_INTENT),
    UPDATE(Constants.SIMPRINTS_UPDATE_INTENT),
    VERIFY(Constants.SIMPRINTS_VERIFY_INTENT),
    MISSING("Missing"),
    INVALID("Invalid");

    companion object {

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
