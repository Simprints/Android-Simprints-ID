package com.simprints.feature.orchestrator.usecases

import android.os.Parcelable
import com.simprints.face.configuration.FaceConfigurationResult
import com.simprints.feature.exitform.ExitFormResult
import com.simprints.feature.fetchsubject.FetchSubjectResult
import com.simprints.feature.orchestrator.model.responses.AppErrorResponse
import com.simprints.feature.orchestrator.model.responses.AppRefusalResponse
import com.simprints.feature.setup.SetupResult
import com.simprints.moduleapi.app.responses.IAppErrorReason
import com.simprints.moduleapi.app.responses.IAppResponse
import javax.inject.Inject

class MapRefusalOrErrorResultUseCase @Inject constructor() {

    operator fun invoke(result: Parcelable): IAppResponse? = when (result) {
        null -> null
        is ExitFormResult -> AppRefusalResponse.fromResult(result)
        is FetchSubjectResult -> result.takeUnless { it.found }?.let {
            AppErrorResponse(
                if (it.wasOnline) IAppErrorReason.GUID_NOT_FOUND_ONLINE
                else IAppErrorReason.GUID_NOT_FOUND_OFFLINE
            )
        }

        is SetupResult -> result.takeUnless { it.permissionGranted }
            ?.let { AppErrorResponse(IAppErrorReason.LOGIN_NOT_COMPLETE) }

        is FaceConfigurationResult -> result.takeUnless { it.isSuccess }
            ?.let { AppErrorResponse(it.error ?: IAppErrorReason.UNEXPECTED_ERROR) }

        else -> null
    }
}
