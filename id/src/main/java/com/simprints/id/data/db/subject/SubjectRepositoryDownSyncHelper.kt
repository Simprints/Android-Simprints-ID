package com.simprints.id.data.db.subject

import com.simprints.core.tools.EncodingUtils
import com.simprints.id.data.db.subjects_sync.down.domain.EventQuery
import com.simprints.id.data.db.subjects_sync.down.domain.SubjectsDownSyncOperation
import com.simprints.id.data.db.subjects_sync.down.domain.SubjectsDownSyncProgress
import com.simprints.id.data.db.subject.domain.FaceSample
import com.simprints.id.data.db.subject.domain.FingerprintSample
import com.simprints.id.data.db.subject.domain.Subject
import com.simprints.id.data.db.subject.domain.subjectevents.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel

interface SubjectRepositoryDownSyncHelper {
    suspend fun performDownSyncWithProgress(scope: CoroutineScope,
                                            downSyncOperation: SubjectsDownSyncOperation,
                                            eventQuery: EventQuery): ReceiveChannel<SubjectsDownSyncProgress>

    companion object {
        const val BATCH_SIZE_FOR_DOWNLOADING = 200

        fun buildPersonFromCreationPayload(payload: EnrolmentRecordCreationPayload) = with(payload) {
            Subject(
                subjectId = subjectId,
                projectId = projectId,
                attendantId = attendantId,
                moduleId = moduleId,
                toSync = false,
                fingerprintSamples = biometricReferences.filterIsInstance(FingerprintReference::class.java)
                    .firstOrNull()?.templates?.map { buildFingerprintSample(it) } ?: emptyList(),
                faceSamples = biometricReferences.filterIsInstance(FaceReference::class.java)
                    .firstOrNull()?.templates?.map { buildFaceSample(it) } ?: emptyList()
            )
        }

        private fun buildFingerprintSample(template: FingerprintTemplate): FingerprintSample {
            return FingerprintSample(
                template.finger.fromEventToPerson(),
                convertBase64ToBytes(template.template),
                template.quality
            )
        }

        private fun buildFaceSample(template: FaceTemplate) =
            FaceSample(convertBase64ToBytes(template.template))

        private fun convertBase64ToBytes(template: String) = EncodingUtils.base64ToBytes(template)
    }
}
