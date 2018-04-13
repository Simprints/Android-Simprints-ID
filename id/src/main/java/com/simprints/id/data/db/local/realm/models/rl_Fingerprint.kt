package com.simprints.id.data.db.local.realm.models

import com.google.gson.annotations.JsonAdapter
import com.simprints.id.data.db.remote.models.fb_Fingerprint
import com.simprints.id.tools.json.FingerIdentifierAsIntJsonConverter
import com.simprints.id.tools.json.TemplateAsByteArrayJsonConverter
import com.simprints.libcommon.Fingerprint
import io.realm.RealmObject

open class rl_Fingerprint : RealmObject {

    @JsonAdapter(FingerIdentifierAsIntJsonConverter::class)
    var fingerId: Int = 0

    @JsonAdapter(TemplateAsByteArrayJsonConverter::class)
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
