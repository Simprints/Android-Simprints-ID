package com.simprints.infra.authlogic.integrity.exceptions

import com.google.android.play.core.integrity.model.IntegrityErrorCode

/**
 * An exception indicating Integrity service is down. and user should try again after a while.
 *
 * @property errorCode is one of the integrity api error codes listed in [IntegrityErrorCode]
 */
class IntegrityServiceTemporaryDown(
    val errorCode: Int,
) : RuntimeException("Integrity API error $errorCode")
