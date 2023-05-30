package com.simprints.fingerprint.orchestrator.domain

import com.simprints.fingerprint.exceptions.unexpected.result.InvalidResultCodeException

/**
 * Result code for individual ActivityTasks.
 * Specifies what result will be available in the FingerprintTask's returned bundle
 */
enum class ResultCode(val value: Int) {

    /** The FingerprintTask finished normally and returned the FingerprintTask's normal TaskResult */
    OK(-1), // Activity.OK

    /**
     * There is no content in the FingerprintTask's return for some other reason
     * e.g. if the user presses back (and is not overridden)
     */
    CANCELLED(0), // Activity.CANCELLED

    /** The user encountered a terminal error and there is only an AlertTaskResult in the return */
    ALERT(1),

    /** The user submitted a refusal form and there is only a RefusalTaskResult in the return */
    REFUSED(2);

    companion object {

        fun fromValue(value: Int) = values().find { it.value == value } ?:
            throw InvalidResultCodeException.forResultCode(value)
    }
}
