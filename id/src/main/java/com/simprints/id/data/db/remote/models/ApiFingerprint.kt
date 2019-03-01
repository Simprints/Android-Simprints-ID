package com.simprints.id.data.db.remote.models

import com.simprints.core.tools.json.SkipSerialisationField
import com.simprints.id.FingerIdentifier
import com.simprints.id.domain.fingerprint.Fingerprint
import com.simprints.id.tools.utils.EncodingUtils

data class ApiFingerprint(@SkipSerialisationField var fingerId: FingerIdentifier,
                          val template: String,
                          val quality: Int) {

    constructor (fingerprint: Fingerprint) : this (
        fingerId = FingerIdentifier.valueOf(fingerprint.fingerId.name),
        template = EncodingUtils.byteArrayToBase64(fingerprint.templateBytes),
        quality = fingerprint.qualityScore)
}

fun Fingerprint.toFirebaseFingerprint(): ApiFingerprint =
    ApiFingerprint(
        fingerId = fingerId,
        template = EncodingUtils.byteArrayToBase64(templateBytes), // TODO: get rid of double bang
        quality = qualityScore
    )

fun ApiFingerprint.toDomainFingerprint(): Fingerprint =
    Fingerprint(
        fingerId = fingerId,
        isoTemplateBytes = EncodingUtils.base64ToBytes(template)
    )
