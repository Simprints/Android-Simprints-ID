package com.simprints.moduleapi.fingerprint.responses

import com.simprints.moduleapi.fingerprint.responses.entities.IFingerprintMatchResult

interface IFingerprintMatchResponse: IFingerprintResponse {
    val result: List<IFingerprintMatchResult>
}
