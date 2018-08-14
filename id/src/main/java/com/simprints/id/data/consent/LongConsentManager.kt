package com.simprints.id.data.consent

import io.reactivex.Flowable
import java.io.File

interface LongConsentManager {

    val languages: Array<String>

    fun downloadLongConsent(language: String): Flowable<Int>

    fun checkIfLongConsentExists(language: String): Boolean

    fun getLongConsentUri(language: String): File

}
