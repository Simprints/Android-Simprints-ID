package com.simprints.feature.importsubject.usecase

import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.enrolment.records.store.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.store.domain.models.Subject
import com.simprints.infra.enrolment.records.store.domain.models.SubjectAction
import com.simprints.face.infra.basebiosdk.detection.Face
import com.simprints.infra.logging.Simber
import java.util.Date
import javax.inject.Inject

class SaveSubjectUseCase @Inject constructor(
    private val timeHelper: TimeHelper,
    private val enrolmentLocalSource: EnrolmentRecordRepository,
) {

    suspend operator fun invoke(
        projectId: String,
        subjectId: String,
        face: Face,
    ) {
        val now = Date(timeHelper.now().ms)

        val subject = Subject(
            subjectId = subjectId,
            projectId = projectId,
            createdAt = now,
            updatedAt = now,
            moduleId = TokenizableString.Raw("module1"),
            attendantId = TokenizableString.Raw("userId"),
            faceSamples = listOf(
                FaceSample(
                    template = face.template,
                    format = face.format
                )
            ),
        )
        Simber.tag("POC").d("Subject: $subject")

        enrolmentLocalSource.performActions(listOf(SubjectAction.Creation(subject)))
    }
}
