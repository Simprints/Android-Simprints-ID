package com.simprints.id.data.db.subject.domain

import android.os.Parcelable
import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.eventsystem.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.id.tools.extensions.toMode
import com.simprints.infra.config.domain.models.GeneralConfiguration
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Subject(
    val subjectId: String,
    val projectId: String,
    val attendantId: String,
    val moduleId: String,
    val createdAt: Date? = null,
    val updatedAt: Date? = null,
    var fingerprintSamples: List<FingerprintSample> = emptyList(),
    var faceSamples: List<FaceSample> = emptyList(),

    @Deprecated("See SubjectToEventDbMigrationManagerImpl doc")
    val toSync: Boolean = false
) : Parcelable

fun Subject.fromSubjectToEnrolmentCreationEvent(
    now: Long,
    modalities: List<GeneralConfiguration.Modality>,
    encoder: EncodingUtils
): EnrolmentRecordCreationEvent {
    return EnrolmentRecordCreationEvent(
        now,
        subjectId,
        projectId,
        moduleId,
        attendantId,
        modalities.map { it.toMode() },
        EnrolmentRecordCreationEvent.buildBiometricReferences(
            fingerprintSamples,
            faceSamples,
            encoder
        )
    )
}
