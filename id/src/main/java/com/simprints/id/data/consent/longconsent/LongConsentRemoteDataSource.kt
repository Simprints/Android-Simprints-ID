package com.simprints.id.data.consent.longconsent

import java.io.InputStream

interface LongConsentRemoteDataSource {

    data class Stream(val inputStream: InputStream, val total: Long)

    suspend fun downloadLongConsent(language: String): Stream
}
