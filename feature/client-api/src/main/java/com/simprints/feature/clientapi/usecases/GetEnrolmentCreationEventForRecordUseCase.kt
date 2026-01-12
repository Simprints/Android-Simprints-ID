package com.simprints.feature.clientapi.usecases

import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.canCoSyncAllData
import com.simprints.infra.config.store.models.canCoSyncBiometricData
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecord
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordQuery
import com.simprints.infra.events.event.cosync.CoSyncEnrolmentRecordEvents
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.infra.logging.Simber
import javax.inject.Inject

internal class GetEnrolmentCreationEventForRecordUseCase @Inject constructor(
    private val configRepository: ConfigRepository,
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
    private val encoder: EncodingUtils,
    private val jsonHelper: JsonHelper,
) {
    suspend operator fun invoke(
        projectId: String,
        subjectId: String,
    ): String? {
        val config = configRepository.getProjectConfiguration()

        if (!config.canCoSyncAllData() && !config.canCoSyncBiometricData()) {
            return null
        }

        val recordCreationEvent = enrolmentRecordRepository
            .load(EnrolmentRecordQuery(projectId = projectId, subjectId = subjectId))
            .firstOrNull()
            ?.fromSubjectToEnrolmentCreationEvent()

        if (recordCreationEvent == null) {
            Simber.e(
                "Couldn't find enrolment for subjectActions",
                IllegalStateException("No enrolment record found for subjectId: $subjectId"),
            )
            return null
        }

        return jsonHelper.json.encodeToString(CoSyncEnrolmentRecordEvents(listOf(recordCreationEvent)))
    }

    private fun EnrolmentRecord.fromSubjectToEnrolmentCreationEvent() = EnrolmentRecordCreationEvent(
        subjectId = subjectId,
        projectId = projectId,
        moduleId = moduleId,
        attendantId = attendantId,
        biometricReferences = EnrolmentRecordCreationEvent.buildBiometricReferences(references, encoder),
        externalCredentials = externalCredentials,
    )
}
