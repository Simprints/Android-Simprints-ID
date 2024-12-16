package com.simprints.infra.authlogic.integrity.exceptions

import com.google.android.play.core.integrity.model.IntegrityErrorCode

/**
 * An exception indicating that the Google Play Store app is not installed or have outdated.
 *
 * @property errorCode is one of the integrity api error codes listed in [IntegrityErrorCode]
 */
class MissingOrOutdatedGooglePlayStoreApp(
    val errorCode: Int,
) : RuntimeException("Integrity API error $errorCode")
