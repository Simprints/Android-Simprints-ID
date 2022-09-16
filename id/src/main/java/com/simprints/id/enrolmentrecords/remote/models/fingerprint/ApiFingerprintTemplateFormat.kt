package com.simprints.id.enrolmentrecords.remote.models.fingerprint

import androidx.annotation.Keep
import com.simprints.moduleapi.fingerprint.IFingerprintTemplateFormat

@Keep
enum class ApiFingerprintTemplateFormat {
    ISO_19794_2,
    NEC;
}

fun IFingerprintTemplateFormat.toApi(): ApiFingerprintTemplateFormat =
    when (this) {
        IFingerprintTemplateFormat.ISO_19794_2 -> ApiFingerprintTemplateFormat.ISO_19794_2
        IFingerprintTemplateFormat.NEC -> ApiFingerprintTemplateFormat.NEC
    }
