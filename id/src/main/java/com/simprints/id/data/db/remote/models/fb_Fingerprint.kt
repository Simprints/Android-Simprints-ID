package com.simprints.id.data.db.remote.models

import com.simprints.id.domain.Fingerprint
import com.simprints.core.tools.json.SkipSerialisationField
import com.simprints.libcommon.Utils
import com.simprints.libsimprints.FingerIdentifier
import com.simprints.libcommon.Fingerprint as LibFingerprint

data class fb_Fingerprint(@SkipSerialisationField var fingerId: FingerIdentifier,
                          val template: String,
                          val quality: Int) {

    constructor (fingerprint: LibFingerprint) : this (
        fingerId = fingerprint.fingerId,
        template = Utils.byteArrayToBase64(fingerprint.templateBytes),
        quality = fingerprint.qualityScore)
}

fun Fingerprint.toFirebaseFingerprint(): fb_Fingerprint =
    fb_Fingerprint(
        fingerId = FingerIdentifier.values()[fingerId],
        template = Utils.byteArrayToBase64(template!!), // TODO: get rid of double bang
        quality = qualityScore
    )

fun fb_Fingerprint.toDomainFingerprint(): Fingerprint =
    Fingerprint(
        fingerId = fingerId.ordinal,
        template = Utils.base64ToBytes(template),
        qualityScore = quality
    )
