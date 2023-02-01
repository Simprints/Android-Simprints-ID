package com.simprints.infra.login.exceptions

import com.google.android.play.core.integrity.model.IntegrityErrorCode

/**
 * An exception indicating something went wrong while requesting the Integrity token.
 *
 * @property errorCode is one of the integrity api error codes listed in [IntegrityErrorCode]
 * @property cause is the original exception
 */
//Todo Guiding the user on how to handle this errors will be implemented in this task https://simprints.atlassian.net/browse/CORE-2153
class RequestingIntegrityTokenException(
    val errorCode: Int,
    override val cause: Throwable
) : RuntimeException("Integrity API error $errorCode")

