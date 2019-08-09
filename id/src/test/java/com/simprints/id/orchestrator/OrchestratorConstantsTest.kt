package com.simprints.id.orchestrator

import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.app.requests.AppIdentifyRequest
import com.simprints.id.domain.moduleapi.app.requests.AppVerifyRequest
import com.simprints.moduleapi.app.responses.IAppEnrolResponse
import com.simprints.moduleapi.app.responses.IAppResponseType
import com.simprints.moduleapi.face.responses.IFaceEnrolResponse
import kotlinx.android.parcel.Parcelize

internal const val SOME_SESSION = "some_session"
internal const val SOME_GUID = "some_guid"
internal const val SOME_METADATA = "some_metadata"
internal const val PACKAGE = "com.simprints.id"

internal val verifyAppRequest = AppVerifyRequest(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, SOME_METADATA, SOME_GUID)
internal val enrolAppRequest = AppEnrolRequest(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, SOME_METADATA)
internal val identifyAppRequest = AppIdentifyRequest(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, SOME_METADATA)

@Parcelize
internal class AppEnrolResponse(override val guid: String,
                                override val type: IAppResponseType = IAppResponseType.ENROL) : IAppEnrolResponse

@Parcelize
internal class FaceEnrolResponse(override val guid: String) : IFaceEnrolResponse
