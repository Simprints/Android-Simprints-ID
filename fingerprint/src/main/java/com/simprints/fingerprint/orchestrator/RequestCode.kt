package com.simprints.fingerprint.orchestrator

enum class RequestCode(val value: Int) {

    LAUNCH(PREFIX + 1),
    COLLECT(PREFIX + 2),
    MATCHING(PREFIX + 3),
    ALERT(PREFIX + 5),
    REFUSAL(PREFIX + 6);
}

private const val PREFIX = 200
