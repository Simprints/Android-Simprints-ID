package com.simprints.id.data.consent.longconsent

import java.io.File

interface LongConsentLocalDataSource {

    fun isLongConsentPresentInLocal(language: String): Boolean

    fun createFileForLanguage(language: String): File

    fun deleteLongConsents()

    fun getLongConsentText(language: String): String
}
