package com.simprints.fingerprint.orchestrator.domain

enum class RequestCode(val value: Int) {

    // Activities started normally in orchestrator flow
    LAUNCH(201),
    COLLECT(202),
    MATCHING(203),

    // Activities that interrupt the flow
    ALERT(401),
    REFUSAL(402)
}
