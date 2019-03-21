package com.simprints.moduleapi.face.requests

import com.simprints.moduleapi.fingerprint.requests.IMatchGroup


interface IFaceIdentifyRequest : IFaceRequest {
    val matchGroup: IMatchGroup
    val returnIdCount: Int
}
