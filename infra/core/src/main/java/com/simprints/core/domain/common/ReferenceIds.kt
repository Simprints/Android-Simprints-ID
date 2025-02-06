package com.simprints.core.domain.common

import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.fingerprint.FingerprintSample
import java.util.UUID

/**
 * Generate UUID based on the provided face templates.
 */
fun List<ByteArray>.faceReferenceId(): String? = if (isNotEmpty()) {
    sortedBy { it.contentHashCode() }
        .fold(byteArrayOf()) { acc, template -> acc + template }
        .let { UUID.nameUUIDFromBytes(it).toString() }
} else {
    null
}

/**
 * Generate UUID based on the provided face samples.
 */
fun List<FaceSample>.faceReferenceIdFromSamples(): String? = map { it.template }.faceReferenceId()

/**
 * Generate UUID based on the provided fingerprint templates and template quality scores.
 */
fun List<Pair<Int, ByteArray>>.fingerprintReferenceId(): String? = if (isNotEmpty()) {
    sortedBy { it.first }
        .fold(byteArrayOf()) { acc, sample -> acc + sample.second }
        .let { UUID.nameUUIDFromBytes(it).toString() }
} else {
    null
}

/**
 * Generate UUID based on the provided fingerprint templates.
 */
fun List<FingerprintSample>.fingerprintReferenceIdFromSamples(): String? =
    map { it.templateQualityScore to it.template }.fingerprintReferenceId()
