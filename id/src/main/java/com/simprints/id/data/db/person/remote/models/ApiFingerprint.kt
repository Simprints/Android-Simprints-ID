package com.simprints.id.data.db.person.remote.models

import androidx.annotation.Keep
import com.simprints.id.data.db.person.domain.FingerIdentifier
import com.simprints.id.data.db.person.domain.Fingerprint
import com.simprints.core.tools.EncodingUtils

@Keep
data class ApiFingerprint(var finger: FingerIdentifier,
                          val template: String,
                          val quality: Int) {

    constructor (fingerprint: Fingerprint) : this (
        finger = FingerIdentifier.valueOf(fingerprint.finger.name),
        template = EncodingUtils.byteArrayToBase64(fingerprint.templateBytes),
        quality = fingerprint.qualityScore)
}

fun Fingerprint.toApiFingerprint(): ApiFingerprint =
    ApiFingerprint(
        finger = finger,
        template = EncodingUtils.byteArrayToBase64(templateBytes), // TODO: get rid of double bang
        quality = qualityScore
    )

fun ApiFingerprint.toDomainFingerprint(): Fingerprint =
    Fingerprint(
        fingerId = finger,
        isoTemplateBytes = EncodingUtils.base64ToBytes(template)
    )
