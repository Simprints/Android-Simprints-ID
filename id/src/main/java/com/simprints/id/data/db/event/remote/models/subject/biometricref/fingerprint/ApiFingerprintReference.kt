package com.simprints.id.data.db.event.remote.models.subject.biometricref.fingerprint

import androidx.annotation.Keep
import com.simprints.id.data.db.event.remote.models.subject.biometricref.ApiBiometricReference
import com.simprints.id.data.db.event.remote.models.subject.biometricref.ApiBiometricReferenceType
import com.simprints.id.data.db.event.remote.models.subject.biometricref.ApiBiometricReferenceType.FingerprintReference
import java.util.*

@Keep
data class ApiFingerprintReference(
    override val id: String = UUID.randomUUID().toString(),
    val templates: List<ApiFingerprintTemplate>,
    val format: ApiFingerprintTemplateFormat,
    val metadata: HashMap<String, String>? = null) : ApiBiometricReference {
    override val type: ApiBiometricReferenceType = FingerprintReference
}
