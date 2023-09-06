package com.simprints.clientapi.domain.requests

import com.simprints.core.domain.tokenization.TokenizedString
import com.simprints.moduleapi.app.requests.IAppRequest


interface BaseRequest {

    val projectId: String
    val userId: TokenizedString

    val unknownExtras: Map<String, Any?>

    fun convertToAppRequest(): IAppRequest

}



