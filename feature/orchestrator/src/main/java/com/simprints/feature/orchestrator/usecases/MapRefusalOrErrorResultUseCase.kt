package com.simprints.feature.orchestrator.usecases

import com.simprints.core.domain.response.AppErrorReason
import com.simprints.feature.alert.AlertResult
import com.simprints.feature.exitform.ExitFormResult
import com.simprints.feature.fetchsubject.FetchSubjectResult
import com.simprints.feature.selectagegroup.SelectSubjectAgeGroupResult
import com.simprints.feature.setup.SetupResult
import com.simprints.feature.validatepool.ValidateSubjectPoolResult
import com.simprints.fingerprint.connect.FingerprintConnectResult
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.allowedAgeRanges
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.infra.orchestration.data.responses.AppErrorResponse
import com.simprints.infra.orchestration.data.responses.AppIdentifyResponse
import com.simprints.infra.orchestration.data.responses.AppRefusalResponse
import com.simprints.infra.orchestration.data.responses.AppResponse
import java.io.Serializable
import javax.inject.Inject

internal class MapRefusalOrErrorResultUseCase @Inject constructor(
    private val eventRepository: SessionEventRepository,
) {
    suspend operator fun invoke(
        result: Serializable,
        projectConfiguration: ProjectConfiguration,
    ): AppResponse? = when (result) {
        is ExitFormResult -> AppRefusalResponse.fromResult(result)

        is FetchSubjectResult -> result.takeUnless { it.found }?.let {
            AppErrorResponse(
                if (it.wasOnline) {
                    AppErrorReason.GUID_NOT_FOUND_ONLINE
                } else {
                    AppErrorReason.GUID_NOT_FOUND_OFFLINE
                },
            )
        }

        is SetupResult ->
            result
                .takeUnless { it.isSuccess }
                ?.let { AppErrorResponse(AppErrorReason.UNEXPECTED_ERROR) }

        is FingerprintConnectResult ->
            result
                .takeUnless { it.isSuccess }
                ?.let { AppErrorResponse(AppErrorReason.UNEXPECTED_ERROR) }

        is AlertResult -> AppErrorResponse(result.appErrorReason ?: AppErrorReason.UNEXPECTED_ERROR)

        is ValidateSubjectPoolResult ->
            result
                .takeUnless { it.isValid }
                ?.let { AppIdentifyResponse(emptyList(), eventRepository.getCurrentSessionScope().id) }

        is SelectSubjectAgeGroupResult ->
            result
                .takeUnless {
                    projectConfiguration.allowedAgeRanges().any { it.contains(result.ageGroup) }
                }?.let { AppErrorResponse(AppErrorReason.AGE_GROUP_NOT_SUPPORTED) }

        else -> null
    }
}
