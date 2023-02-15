package com.simprints.eventsystem.event.domain.models.fingerprint

import androidx.annotation.Keep
import com.simprints.moduleapi.fingerprint.IFingerprintTemplateFormat

@Keep
enum class FingerprintTemplateFormat {
    ISO_19794_2,
    NEC_1;

    fun fromDomainToModuleApi(): IFingerprintTemplateFormat =
        when (this) {
            ISO_19794_2 -> IFingerprintTemplateFormat.ISO_19794_2
            NEC_1 -> IFingerprintTemplateFormat.NEC_1
        }
}

fun IFingerprintTemplateFormat.fromModuleApiToDomain(): FingerprintTemplateFormat =
    when (this) {
        IFingerprintTemplateFormat.ISO_19794_2 -> FingerprintTemplateFormat.ISO_19794_2
        IFingerprintTemplateFormat.NEC_1 -> FingerprintTemplateFormat.NEC_1
    }
