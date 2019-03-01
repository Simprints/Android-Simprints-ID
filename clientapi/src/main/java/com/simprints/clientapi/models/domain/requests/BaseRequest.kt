package com.simprints.clientapi.models.domain.requests

import android.os.Parcelable
import com.simprints.clientapi.models.domain.ClientBase
import com.simprints.moduleinterfaces.app.requests.IAppRequest


interface BaseRequest : ClientBase, Parcelable {

    val userId: String
    val moduleId: String
    val metadata: String

    fun convertToAppRequest(): IAppRequest

}



