package com.simprints.moduleapi.fingerprint.responses

@Deprecated("To be replaced by IFingerprintCaptureResponse")
interface IFingerprintEnrolResponse : IFingerprintResponse {

    val guid: String

}
