package com.simprints.id.data.db.event.remote.models.subject.biometricref.fingerprint

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.fingerprint.FingerprintTemplateFormat
import com.simprints.id.data.db.event.remote.models.subject.biometricref.ApiBiometricReference
import com.simprints.id.data.db.event.remote.models.subject.biometricref.ApiBiometricReferenceType
import com.simprints.id.data.db.event.remote.models.subject.biometricref.ApiBiometricReferenceType.FingerprintReference
import com.simprints.id.data.db.event.domain.models.fingerprint.FingerprintTemplateFormat.ISO_19794_2
import java.util.*

@Keep
data class ApiFingerprintReference(
    override val id: String = UUID.randomUUID().toString(),
    val templates: List<ApiFingerprintTemplate>,
    val format: FingerprintTemplateFormat = ISO_19794_2,
    val metadata: HashMap<String, String>? = null) : ApiBiometricReference {
    override val type: ApiBiometricReferenceType = FingerprintReference
}
