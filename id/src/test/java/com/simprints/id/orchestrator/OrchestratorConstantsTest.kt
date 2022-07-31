package com.simprints.id.orchestrator

import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_METADATA
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID1
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.requests.AppRequest.AppRequestFlow.*
import com.simprints.moduleapi.face.responses.IFaceCaptureResponse
import com.simprints.moduleapi.face.responses.IFaceMatchResponse
import com.simprints.moduleapi.face.responses.IFaceMatchResult
import com.simprints.moduleapi.face.responses.IFaceResponseType
import com.simprints.moduleapi.face.responses.entities.IFaceCaptureResult
import kotlinx.parcelize.Parcelize

internal val verifyAppRequest = AppVerifyRequest(
    DEFAULT_PROJECT_ID,
    DEFAULT_USER_ID,
    DEFAULT_MODULE_ID,
    DEFAULT_METADATA,
    GUID1
)

internal val enrolAppRequest = AppEnrolRequest(
    DEFAULT_PROJECT_ID,
    DEFAULT_USER_ID,
    DEFAULT_MODULE_ID,
    DEFAULT_METADATA
)

internal val identifyAppRequest = AppIdentifyRequest(
    DEFAULT_PROJECT_ID,
    DEFAULT_USER_ID,
    DEFAULT_MODULE_ID,
    DEFAULT_METADATA
)

internal val followUpRequest = AppRequest.AppRequestFollowUp.AppEnrolLastBiometricsRequest(
    DEFAULT_PROJECT_ID,
    DEFAULT_USER_ID,
    DEFAULT_MODULE_ID,
    DEFAULT_METADATA,
    GUID1,
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
