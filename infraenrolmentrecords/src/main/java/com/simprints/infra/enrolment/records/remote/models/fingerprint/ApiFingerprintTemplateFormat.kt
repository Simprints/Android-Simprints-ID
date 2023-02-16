package com.simprints.infra.enrolment.records.remote.models.fingerprint

import androidx.annotation.Keep
import com.simprints.moduleapi.fingerprint.IFingerprintTemplateFormat

@Keep
internal enum class ApiFingerprintTemplateFormat {
    ISO_19794_2,
    NEC_1;
}

internal fun IFingerprintTemplateFormat.toApi(): ApiFingerprintTemplateFormat =
    when (this) {
        IFingerprintTemplateFormat.ISO_19794_2 -> ApiFingerprintTemplateFormat.ISO_19794_2
        IFingerprintTemplateFormat.NEC_1 -> ApiFingerprintTemplateFormat.NEC_1
    }
