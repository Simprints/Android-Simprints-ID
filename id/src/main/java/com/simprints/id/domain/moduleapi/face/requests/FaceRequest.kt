package com.simprints.id.domain.moduleapi.face.requests

import android.os.Parcelable
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.domain.moduleapi.face.requests.FaceRequestType.*
import com.simprints.id.orchestrator.steps.Step.Request

abstract class FaceRequest: Parcelable, Request {
    abstract val type: FaceRequestType

    override fun toJson(): String = JsonHelper.toJson(this)
}

enum class FaceRequestType {
    CAPTURE,
    MATCH,

    VERIFY, //TBRemoved soon
    IDENTIFY //TBRemoved soon
}

fun FaceRequest.fromDomainToModuleApi() =
    when (this.type) {
        CAPTURE -> (this as FaceCaptureRequest).fromDomainToModuleApi()
        MATCH -> TODO()
        VERIFY -> (this as FaceVerifyRequest).fromDomainToModuleApi() //TBRemoved soon
        IDENTIFY -> (this as FaceIdentifyRequest).fromDomainToModuleApi() //TBRemoved soon
    }
