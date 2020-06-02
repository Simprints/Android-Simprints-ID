package com.simprints.clientapi.domain.requests

import com.simprints.moduleapi.app.requests.IAppRequest


interface BaseRequest {

    val projectId: String
    val userId: String

    val unknownExtras: Map<String, Any?>

    fun convertToAppRequest(): IAppRequest

}



