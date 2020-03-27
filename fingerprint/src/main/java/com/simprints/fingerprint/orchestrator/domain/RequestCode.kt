package com.simprints.fingerprint.orchestrator.domain

enum class RequestCode(val value: Int) {

    // Activities started normally in orchestrator flow
    CONNECT(201),
    COLLECT(202),
    MATCHING(203),

    // Activities that are shown momentarily during the connecting flow
    BLUETOOTH_OFF(301),
    NFC_OFF(302),
    NFC_PAIR(303),
    SERIAL_ENTRY_PAIR(304),
    TURN_ON_SCANNER(305),

    // Activities that interrupt the flow
    ALERT(401),
    REFUSAL(402)
}
