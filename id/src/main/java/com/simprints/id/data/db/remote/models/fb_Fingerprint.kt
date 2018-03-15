package com.simprints.id.data.db.remote.models

import com.google.firebase.database.Exclude
import com.simprints.libcommon.Fingerprint
import com.simprints.libcommon.Utils
import com.simprints.libsimprints.FingerIdentifier

data class fb_Fingerprint(val fingerId: FingerIdentifier = FingerIdentifier.LEFT_THUMB,
                          val template: String = "",
                          val qualityScore: Int = 0) {

    constructor (fingerprint: Fingerprint) : this(
        fingerId = fingerprint.fingerId,
        template = Utils.byteArrayToBase64(fingerprint.templateBytes),
        qualityScore = fingerprint.qualityScore)

    @Exclude
    fun toMap(): Map<String, Any> {
        return mapOf(
            "fingerId" to fingerId.name,
            "template" to template,
            "qualityScore" to qualityScore)
    }
}
