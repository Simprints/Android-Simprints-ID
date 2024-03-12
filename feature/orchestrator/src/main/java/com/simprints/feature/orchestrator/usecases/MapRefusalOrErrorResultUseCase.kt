package com.simprints.feature.orchestrator.usecases

import com.simprints.core.domain.response.AppErrorReason
import com.simprints.feature.alert.AlertResult
import com.simprints.feature.exitform.ExitFormResult
import com.simprints.feature.fetchsubject.FetchSubjectResult
import com.simprints.feature.orchestrator.AlertConfigurationMapper
import com.simprints.feature.setup.SetupResult
import com.simprints.fingerprint.connect.FingerprintConnectResult
import com.simprints.infra.orchestration.data.responses.AppErrorResponse
import com.simprints.infra.orchestration.data.responses.AppRefusalResponse
import com.simprints.infra.orchestration.data.responses.AppResponse
import java.io.Serializable
import javax.inject.Inject

internal class MapRefusalOrErrorResultUseCase @Inject constructor() {

    operator fun invoke(result: Serializable): AppResponse? = when (result) {
        is ExitFormResult -> AppRefusalResponse.fromResult(result)
        is FetchSubjectResult -> result.takeUnless { it.found }?.let {
            AppErrorResponse(
                if (it.wasOnline) AppErrorReason.GUID_NOT_FOUND_ONLINE
                else AppErrorReason.GUID_NOT_FOUND_OFFLINE
            )
        }

        is SetupResult -> result.takeUnless { it.isSuccess }
            ?.let { AppErrorResponse(AppErrorReason.UNEXPECTED_ERROR) }

        is FingerprintConnectResult -> result.takeUnless { it.isSuccess }
            ?.let { AppErrorResponse(AppErrorReason.UNEXPECTED_ERROR) }

        is AlertResult -> AppErrorResponse(AlertConfigurationMapper.reasonFromPayload(result.payload))

        else -> null
    }
}
