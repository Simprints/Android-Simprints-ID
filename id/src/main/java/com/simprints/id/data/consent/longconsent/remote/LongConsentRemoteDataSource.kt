package com.simprints.id.data.consent.longconsent.remote


interface LongConsentRemoteDataSource {

    class File(val bytes: ByteArray)

    suspend fun downloadLongConsent(language: String): File
}
