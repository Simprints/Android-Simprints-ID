package com.simprints.id.domain.moduleapi.app.requests

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
    val type: AppRequestType

    companion object {
        const val BUNDLE_KEY = "ApiRequest"
    }
}

enum class AppRequestType {
    ENROL,
    IDENTIFY,
    VERIFY
}
