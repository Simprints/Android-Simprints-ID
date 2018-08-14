package com.simprints.id.data.consent

import io.reactivex.Flowable

interface LongConsentManager {

    val languages: Array<String>

    fun downloadLongConsent(language: String): Flowable<Int>

    fun checkIfLongConsentExists(language: String): Boolean

    fun getLongConsentText(language: String): String

}
