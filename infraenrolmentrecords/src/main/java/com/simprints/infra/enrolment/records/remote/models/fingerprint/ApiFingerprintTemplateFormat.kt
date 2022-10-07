package com.simprints.infra.enrolment.records.remote.models.fingerprint

import androidx.annotation.Keep
import com.simprints.moduleapi.fingerprint.IFingerprintTemplateFormat

@Keep
internal enum class ApiFingerprintTemplateFormat {
    ISO_19794_2,
    NEC;
}

internal fun IFingerprintTemplateFormat.toApi(): ApiFingerprintTemplateFormat =
    when (this) {
        IFingerprintTemplateFormat.ISO_19794_2 -> ApiFingerprintTemplateFormat.ISO_19794_2
        IFingerprintTemplateFormat.NEC -> ApiFingerprintTemplateFormat.NEC
    }
