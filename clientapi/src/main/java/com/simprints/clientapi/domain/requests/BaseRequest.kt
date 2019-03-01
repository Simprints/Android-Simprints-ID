package com.simprints.clientapi.domain.requests

import com.simprints.clientapi.domain.ClientBase
import com.simprints.moduleinterfaces.app.requests.IAppRequest


interface BaseRequest : ClientBase {

    val userId: String
    val moduleId: String
    val metadata: String

    fun convertToAppRequest(): IAppRequest

}



