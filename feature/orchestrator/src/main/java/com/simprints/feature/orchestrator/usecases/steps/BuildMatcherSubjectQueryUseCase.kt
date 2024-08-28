package com.simprints.feature.orchestrator.usecases.steps

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.infra.config.store.models.IdentificationConfiguration
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.enrolment.records.store.domain.models.SubjectQuery
import com.simprints.infra.orchestration.data.ActionRequest
import javax.inject.Inject

@ExcludedFromGeneratedTestCoverageReports("Mapping code")
internal class BuildMatcherSubjectQueryUseCase @Inject constructor() {

    operator fun invoke(
        projectConfiguration: ProjectConfiguration,
        actionRequest: ActionRequest,
    ) = when (projectConfiguration.identification.poolType) {
        IdentificationConfiguration.PoolType.PROJECT -> SubjectQuery(
            projectId = actionRequest.projectId,
            metadata = actionRequest.metadata,
        )

        IdentificationConfiguration.PoolType.USER -> SubjectQuery(
            projectId = actionRequest.projectId,
            attendantId = actionRequest.userId.value,
            metadata = actionRequest.metadata,
        )

        IdentificationConfiguration.PoolType.MODULE -> SubjectQuery(
            projectId = actionRequest.projectId,
            moduleId = (actionRequest as ActionRequest.FlowAction).moduleId.value,
            metadata = actionRequest.metadata,
        )
    }
}
