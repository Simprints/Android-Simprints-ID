package com.simprints.id.data.db.remote.models

import com.simprints.id.tools.json.SkipSerialisationField
import com.simprints.libcommon.Fingerprint
import com.simprints.libcommon.Utils
import com.simprints.libsimprints.FingerIdentifier

data class fb_Fingerprint(@SkipSerialisationField val fingerId: FingerIdentifier = FingerIdentifier.LEFT_THUMB,
                          val template: String = "",
                          val quality: Int = 0) {

    constructor (fingerprint: Fingerprint) : this (
        fingerId = fingerprint.fingerId,
        template = Utils.byteArrayToBase64(fingerprint.templateBytes),
        quality = fingerprint.qualityScore)
}
