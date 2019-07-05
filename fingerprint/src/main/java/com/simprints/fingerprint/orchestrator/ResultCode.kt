package com.simprints.fingerprint.orchestrator

/**
 * Result code for individual ActivityTasks.
 * Specifies what result will be available in the ActivityTask's returned bundle
 */
enum class ResultCode(val value: Int) {

    /** The ActivityTask finished normally and returned the ActivityTask's normal ActResult */
    OK(-1), // Activity.OK

    /**
     * There is no content in the ActivityTask's return for some other reason
     * e.g. if the user presses back (and is not overridden)
     */
    CANCELLED(0), // Activity.CANCELLED

    /** The user encountered a terminal error and there is only an AlertActResult in the return */
    ALERT(1),

    /** The user submitted a refusal form and there is only a RefusalActResult in the return */
    REFUSED(2);

    companion object {

        fun fromValue(value: Int) = values().find { it.value == value } ?:
            throw Throwable("Woops") // TODO
    }
}
