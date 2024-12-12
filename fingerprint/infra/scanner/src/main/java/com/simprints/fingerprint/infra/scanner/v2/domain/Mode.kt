package com.simprints.fingerprint.infra.scanner.v2.domain

/**
 * The modes that Vero 2 can be in, each with its own API
 */
enum class Mode {
    ROOT,
    MAIN,
    CYPRESS_OTA,
    STM_OTA,
}
