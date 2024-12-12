package com.simprints.fingerprint.infra.scanner.domain

/**
 * This enum class represents the different vero scanners we use.
 *
 * - [VERO_1]  this is the version 1 of the Vero Scanner.
 * - [VERO_2]  this version 2 of the Vero scanner, with over the air firmware updates and bonus features
 */
enum class ScannerGeneration {
    VERO_1,
    VERO_2,
}
