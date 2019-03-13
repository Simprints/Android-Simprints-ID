package com.simprints.moduleapi.fingerprint

import com.simprints.moduleapi.app.requests.IAppRequest


interface IFingerprintVerifyRequest : IFingerprintRequest {

    val verifyGuid: String

}
