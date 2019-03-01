package com.simprints.id.domain.requests

import android.os.Parcelable

interface BaseRequest {
    val projectId: String
}

interface RequestParamAction {
    val userId: String
    val moduleId: String
    val metadata: String
}

interface Request : BaseRequest, RequestParamAction, Parcelable {
    companion object {
        const val BUNDLE_KEY = "ApiRequest"

        fun action(appRequest: Request) =
            when (appRequest) {
                is EnrolRequest -> RequestAction.ENROL
                is VerifyRequest -> RequestAction.VERIFY
                is IdentifyRequest -> RequestAction.IDENTIFY
                else -> throw IllegalArgumentException("Invalid appRequest")
            }
    }
}
