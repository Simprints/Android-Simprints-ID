package com.simprints.id.orchestrator

import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.domain.moduleapi.app.requests.AppIdentifyRequest
import com.simprints.id.domain.moduleapi.app.requests.AppVerifyRequest
import com.simprints.moduleapi.face.responses.IFaceCaptureResponse
import com.simprints.moduleapi.face.responses.IFaceMatchResponse
import com.simprints.moduleapi.face.responses.IFaceMatchResult
import com.simprints.moduleapi.face.responses.IFaceResponseType
import com.simprints.moduleapi.face.responses.entities.IFaceCaptureResult
import kotlinx.android.parcel.Parcelize

internal const val SOME_GUID = "some_guid"
internal const val SOME_METADATA = "some_metadata"

internal val verifyAppRequest = AppVerifyRequest(
    DEFAULT_PROJECT_ID,
    DEFAULT_USER_ID,
    DEFAULT_MODULE_ID,
    SOME_METADATA,
    SOME_GUID
)

internal val enrolAppRequest = AppEnrolRequest(
    DEFAULT_PROJECT_ID,
    DEFAULT_USER_ID,
    DEFAULT_MODULE_ID,
    SOME_METADATA
)

internal val identifyAppRequest = AppIdentifyRequest(
    DEFAULT_PROJECT_ID,
    DEFAULT_USER_ID,
    DEFAULT_MODULE_ID,
    SOME_METADATA
)

@Parcelize
internal class IFaceCaptureResponseImpl(
    override val capturingResult: List<IFaceCaptureResult>,
    override val type: IFaceResponseType = IFaceResponseType.CAPTURE
) : IFaceCaptureResponse

@Parcelize
internal class IFaceMatchResponseImpl(
    override val result: List<IFaceMatchResult>,
    override val type: IFaceResponseType = IFaceResponseType.MATCH
) : IFaceMatchResponse
