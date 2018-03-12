package com.simprints.id.libdata.models.realm

import com.simprints.id.libdata.models.firebase.fb_Fingerprint
import com.simprints.libcommon.Fingerprint
import io.realm.RealmObject

open class rl_Fingerprint : RealmObject {
    var fingerId: Int = 0
    var template: ByteArray? = null
    var qualityScore: Int = 0

    constructor()

    constructor(print: fb_Fingerprint) {
        val catchPrint: Fingerprint
        try {
            catchPrint = Fingerprint(print.fingerId, print.template)
        } catch (ignored: IllegalArgumentException) {
            return
        }

        this.fingerId = catchPrint.fingerId.ordinal
        this.qualityScore = catchPrint.qualityScore
        this.template = catchPrint.templateBytes
    }
}
