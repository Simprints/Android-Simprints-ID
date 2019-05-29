package com.simprints.moduleapi.fingerprint.requests


interface IFingerprintIdentifyRequest : IFingerprintRequest {
    val matchGroup: IMatchGroup
    val returnIdCount: Int
}
