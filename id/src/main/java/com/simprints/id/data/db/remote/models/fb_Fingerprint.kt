package com.simprints.id.data.db.remote.models

import com.simprints.core.tools.json.SkipSerialisationField
import com.simprints.id.FingerIdentifier
import com.simprints.id.domain.fingerprint.Fingerprint
import com.simprints.id.domain.fingerprint.Utils

data class fb_Fingerprint(@SkipSerialisationField var fingerId: FingerIdentifier,
                          val template: String,
                          val quality: Int) {

    constructor (fingerprint: Fingerprint) : this (
        fingerId = FingerIdentifier.valueOf(fingerprint.fingerId.name),
        template = Utils.byteArrayToBase64(fingerprint.templateBytes),
        quality = fingerprint.qualityScore)
}

fun Fingerprint.toFirebaseFingerprint(): fb_Fingerprint =
    fb_Fingerprint(
        fingerId = fingerId,
        template = Utils.byteArrayToBase64(templateBytes), // TODO: get rid of double bang
        quality = qualityScore
    )

fun fb_Fingerprint.toDomainFingerprint(): Fingerprint =
    Fingerprint(
        fingerId = fingerId,
        isoTemplateBytes = Utils.base64ToBytes(template)
    )
