package com.simprints.id.data.db.event.remote.models.subject.biometricref.face

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.face.FaceTemplateFormat
import com.simprints.id.data.db.event.remote.models.subject.biometricref.ApiBiometricReference
import com.simprints.id.data.db.event.remote.models.subject.biometricref.ApiBiometricReferenceType
import com.simprints.id.data.db.event.remote.models.subject.biometricref.ApiBiometricReferenceType.FaceReference
import com.simprints.id.data.db.event.domain.models.face.FaceTemplateFormat.RANK_ONE_1_23
import java.util.*

@Keep
data class ApiFaceReference(
    override val id: String = UUID.randomUUID().toString(),
    val templates: List<ApiFaceTemplate>,
    val format: FaceTemplateFormat = RANK_ONE_1_23,
    val metadata: HashMap<String, String>? = null) : ApiBiometricReference {
    override val type: ApiBiometricReferenceType = FaceReference
}
