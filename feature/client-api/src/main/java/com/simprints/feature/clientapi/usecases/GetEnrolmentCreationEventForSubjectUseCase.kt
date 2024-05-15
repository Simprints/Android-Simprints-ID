package com.simprints.feature.clientapi.usecases

import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.canCoSyncAllData
import com.simprints.infra.config.store.models.canCoSyncBiometricData
import com.simprints.infra.enrolment.records.store.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.store.domain.models.Subject
import com.simprints.infra.enrolment.records.store.domain.models.SubjectQuery
import com.simprints.infra.events.event.cosync.CoSyncEnrolmentRecordEvents
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordCreationEvent
import javax.inject.Inject

internal class GetEnrolmentCreationEventForSubjectUseCase @Inject constructor(
    private val configRepository: ConfigRepository,
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
    private val encoder: EncodingUtils,
    private val jsonHelper: JsonHelper,
) {
    suspend operator fun invoke(projectId: String, subjectId: String): String? {
        val config = configRepository.getProjectConfiguration()

        if (!config.canCoSyncAllData() && !config.canCoSyncBiometricData()) {
            return null
        }

        val recordCreationEvent = enrolmentRecordRepository
            .load(SubjectQuery(projectId = projectId, subjectId = subjectId))
            .firstOrNull()
            ?.fromSubjectToEnrolmentCreationEvent()
            ?: return null

        return jsonHelper.toJson(CoSyncEnrolmentRecordEvents(listOf(recordCreationEvent)))
    }

    private fun Subject.fromSubjectToEnrolmentCreationEvent() = EnrolmentRecordCreationEvent(
        subjectId,
        projectId,
        moduleId,
        attendantId,
        EnrolmentRecordCreationEvent.buildBiometricReferences(fingerprintSamples, faceSamples, encoder)
    )
}
