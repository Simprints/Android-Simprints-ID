package com.simprints.clientapi.simprintsrequests.requests

import android.os.Parcelable
import com.simprints.moduleinterfaces.app.confirmations.IAppConfirmation
import com.simprints.moduleinterfaces.app.requests.IAppRequest


interface ClientApiBaseRequest : Parcelable {

    val projectId: String

}

interface ClientApiAppRequest : ClientApiBaseRequest, Parcelable {

    val userId: String
    val moduleId: String
    val metadata: String

    fun convertToAppRequest(): IAppRequest

}

interface ClientApiAppConfirmation : ClientApiBaseRequest, Parcelable {

    val sessionId: String
    val selectedGuid: String

    fun convertToAppRequest(): IAppConfirmation

}

