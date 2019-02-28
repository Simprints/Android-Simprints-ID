package com.simprints.id.domain.requests

import android.os.Parcelable

interface AppBaseRequest {
    val projectId: String
}

interface AppRequestParamAction {
    val userId: String
    val moduleId: String
    val metadata: String
}

interface AppRequest : AppBaseRequest, AppRequestParamAction, Parcelable {
    companion object {
        const val BUNDLE_KEY = "ApiRequest"

        fun action(appRequest: AppRequest) =
            when (appRequest) {
                is AppEnrolRequest -> AppRequestAction.ENROL
                is AppVerifyRequest -> AppRequestAction.VERIFY
                is AppIdentifyRequest -> AppRequestAction.IDENTIFY
                else -> throw IllegalArgumentException("Invalid appRequest")
            }
    }
}
