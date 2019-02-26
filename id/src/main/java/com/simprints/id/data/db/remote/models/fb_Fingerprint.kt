package com.simprints.id.data.db.remote.models

import com.simprints.core.tools.json.SkipSerialisationField
import com.simprints.id.FingerIdentifier
import com.simprints.id.domain.fingerprint.Fingerprint
import com.simprints.id.domain.fingerprint.IdFingerprint
import com.simprints.id.domain.fingerprint.Utils
import com.simprints.id.domain.fingerprint.IdFingerprint as LibFingerprint

data class fb_Fingerprint(@SkipSerialisationField var fingerId: FingerIdentifier,
                          val template: String,
                          val quality: Int) {

    constructor (fingerprint: Fingerprint) : this (
        fingerId = FingerIdentifier.valueOf(fingerprint.fingerId.name),
        template = Utils.byteArrayToBase64(fingerprint.templateBytes),
        quality = fingerprint.qualityScore)
}

fun IdFingerprint.toFirebaseFingerprint(): fb_Fingerprint =
    fb_Fingerprint(
        fingerId = FingerIdentifier.values()[fingerId],
        template = Utils.byteArrayToBase64(template!!), // TODO: get rid of double bang
        quality = qualityScore
    )

fun fb_Fingerprint.toDomainFingerprint(): IdFingerprint =
    IdFingerprint(
        fingerId = fingerId.ordinal,
        template = Utils.base64ToBytes(template),
        qualityScore = quality
    )
