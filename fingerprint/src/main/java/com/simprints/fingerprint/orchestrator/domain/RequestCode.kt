package com.simprints.fingerprint.orchestrator.domain

/**
 * This enum class represent the request code used to start other activities while completing a
 * fingerprint requested flow. It can be split in 2 sections: the request-code to activities that
 * progress the fingerprint flow, and the request-code to activities shown as a result of an
 * interruption of the fingerprint flow.
 *
 * @property value  the integer value representing the request code
 */
enum class RequestCode(val value: Int) {

    // Activities started normally in orchestrator flow
    CONNECT(201),
    COLLECT(202),
    MATCHING(203),

    // Activities that interrupt the flow
    ALERT(401),
    REFUSAL(402)
}
