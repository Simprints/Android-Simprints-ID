package com.simprints.id.data.consent

import io.reactivex.Completable
import io.reactivex.Flowable

interface LongConsentManager {

    fun downloadAllLongConsents(languages: Array<String>): Completable

    fun downloadLongConsentWithProgress(language: String): Flowable<Int>

    fun checkIfLongConsentExists(language: String): Boolean

    fun getLongConsentText(language: String): String
}
