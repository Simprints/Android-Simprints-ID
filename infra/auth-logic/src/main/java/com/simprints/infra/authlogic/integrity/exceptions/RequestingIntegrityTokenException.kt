package com.simprints.infra.authlogic.integrity.exceptions

import com.google.android.play.core.integrity.model.IntegrityErrorCode

/**
 * An exception indicating something went wrong while requesting the Integrity token.
 *
 * @property errorCode is one of the integrity api error codes listed in [IntegrityErrorCode]
 */
class RequestingIntegrityTokenException(
    val errorCode: Int,
) : RuntimeException("Integrity API error $errorCode")
