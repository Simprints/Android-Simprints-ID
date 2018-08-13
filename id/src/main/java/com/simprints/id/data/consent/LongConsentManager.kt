package com.simprints.id.data.consent

import android.net.Uri
import io.reactivex.Flowable

interface LongConsentManager {

    val languages: Array<String>

    fun downloadLongConsent(language: String): Flowable<Int>

    fun checkIfLongConsentExists(language: String): Boolean

    fun getLongConsentUri(language: String): Uri

}
