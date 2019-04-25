package com.simprints.id.data.db.remote.models

import androidx.annotation.Keep
import com.simprints.id.FingerIdentifier
import com.simprints.id.domain.fingerprint.Fingerprint
import com.simprints.id.tools.utils.EncodingUtils

@Keep
data class ApiFingerprint(var finger: FingerIdentifier,
                          val template: String,
                          val quality: Int) {

    constructor (fingerprint: Fingerprint) : this (
        finger = FingerIdentifier.valueOf(fingerprint.finger.name),
        template = EncodingUtils.byteArrayToBase64(fingerprint.templateBytes),
        quality = fingerprint.qualityScore)
}

fun Fingerprint.toFirebaseFingerprint(): ApiFingerprint =
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
