package com.simprints.moduleapi.fingerprint


interface IFingerprintIdentifyRequest : IFingerprintRequest {
    val matchGroup: IMatchGroup
    val returnIdCount: Int
}
