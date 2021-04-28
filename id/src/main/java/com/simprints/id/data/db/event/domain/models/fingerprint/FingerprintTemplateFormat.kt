package com.simprints.id.data.db.event.domain.models.fingerprint

import androidx.annotation.Keep
import com.simprints.moduleapi.fingerprint.IFingerprintTemplateFormat

@Keep
enum class FingerprintTemplateFormat {
    ISO_19794_2,
    NEC;

    fun fromDomainToModuleApi(): IFingerprintTemplateFormat =
        when (this) {
            ISO_19794_2 -> IFingerprintTemplateFormat.ISO_19794_2
            NEC -> IFingerprintTemplateFormat.NEC
        }
}

fun IFingerprintTemplateFormat.fromModuleApiToDomain(): FingerprintTemplateFormat =
    when (this) {
        IFingerprintTemplateFormat.ISO_19794_2 -> FingerprintTemplateFormat.ISO_19794_2
        IFingerprintTemplateFormat.NEC -> FingerprintTemplateFormat.NEC
    }
