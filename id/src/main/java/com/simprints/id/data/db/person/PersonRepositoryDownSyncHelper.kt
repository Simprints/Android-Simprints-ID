package com.simprints.id.data.db.person

import com.simprints.core.tools.EncodingUtils
import com.simprints.id.data.db.people_sync.down.domain.EventQuery
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperation
import com.simprints.id.data.db.person.domain.FaceSample
import com.simprints.id.data.db.person.domain.FingerprintSample
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.data.db.person.domain.personevents.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel

interface PersonRepositoryDownSyncHelper {
    suspend fun performDownSyncWithProgress(scope: CoroutineScope,
                                            downSyncOperation: PeopleDownSyncOperation,
                                            eventQuery: EventQuery): ReceiveChannel<Int>

    companion object {
        const val BATCH_SIZE_FOR_DOWNLOADING = 200

        fun buildPersonFromCreationPayload(payload: EnrolmentRecordCreationPayload) = with(payload) {
            Person(
                patientId = subjectId,
                projectId = projectId,
                userId = attendantId,
                moduleId = moduleId,
                toSync = false,
                fingerprintSamples = biometricReferences.filterIsInstance(FingerprintReference::class.java)
                    .firstOrNull()?.templates?.map { buildFingerprintSample(it) } ?: emptyList(),
                faceSamples = biometricReferences.filterIsInstance(FaceReference::class.java)
                    .firstOrNull()?.templates?.map { buildFaceSample(it) } ?: emptyList()
            )
        }

        private fun buildFingerprintSample(template: FingerprintTemplate) =
            FingerprintSample(
                template.finger.fromEventToPerson(),
                EncodingUtils.base64ToBytes(template.template),
                template.quality
            )

        private fun buildFaceSample(template: FaceTemplate) =
            FaceSample(EncodingUtils.base64ToBytes(template.template))
    }
}
