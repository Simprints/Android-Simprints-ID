package com.simprints.feature.clientapi.usecases

import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.feature.clientapi.models.CoSyncEnrolmentRecordEvents
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.canCoSyncAllData
import com.simprints.infra.config.domain.models.canCoSyncBiometricData
import com.simprints.infra.enrolment.records.EnrolmentRecordManager
import com.simprints.infra.enrolment.records.domain.models.Subject
import com.simprints.infra.enrolment.records.domain.models.SubjectQuery
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordCreationEvent
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

internal class GetEnrolmentCreationEventForSubjectUseCase @Inject constructor(
    private val configManager: ConfigManager,
    private val enrolmentRecordManager: EnrolmentRecordManager,
    private val encoder: EncodingUtils,
    private val jsonHelper: JsonHelper,
) {
    suspend operator fun invoke(projectId: String, subjectId: String): String? {
        val config = configManager.getProjectConfiguration()

        if (!config.canCoSyncAllData() && !config.canCoSyncBiometricData()) {
            return null
        }

        val recordCreationEvent = enrolmentRecordManager
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
