package com.simprints.fingerprint.data.domain.fingerprint

/**
 * This class represents a subject's fingerprint biometric identity
 *
 * @param subjectId  the unique id of the subject
 * @param fingerprints  the list of fingerprints captured during biometric capture
 */
class FingerprintIdentity(
    val subjectId: String,
    val fingerprints: List<Fingerprint>
)
