package com.simprints.id.data.consent.longconsent

import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.consent.longconsent.LongConsentFetchResult.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import timber.log.Timber

class LongConsentRepositoryImpl(
    private val longConsentLocalDataSource: LongConsentLocalDataSource,
    private val longConsentRemoteDataSource: LongConsentRemoteDataSource,
    private val crashReportManager: CrashReportManager
) : LongConsentRepository {

    companion object {
        const val DEFAULT_SIZE = 1024
    }

    override fun getLongConsentResultForLanguage(language: String): Flow<LongConsentFetchResult> = flow {
        try {
            val localConsent = longConsentLocalDataSource.getLongConsentText(language)
            if (localConsent.isNotEmpty()) {
                emit(Succeed(language, localConsent))
            } else {
                downloadLongConsentFromFirebaseStorage(this, language)
            }
        } catch (t: Throwable) {
            crashReportManager.logExceptionOrSafeException(t)
            Timber.d(t)
            emit(Failed(language, t))
        }
    }

    private suspend fun downloadLongConsentFromFirebaseStorage(
        flowCollector: FlowCollector<LongConsentFetchResult>,
        language: String
    ) {
        try {
            val stream = longConsentRemoteDataSource.downloadLongConsent(language)
            val file = longConsentLocalDataSource.createFileForLanguage(language)

            stream.inputStream.use { input ->
                file.outputStream().use { output ->
                    var bytesCopied: Long = 0
                    val buffer = ByteArray(DEFAULT_SIZE)
                    var bytesToWrite = input.read(buffer)
                    while (bytesToWrite >= 0) {
                        output.write(buffer, 0, bytesToWrite)
                        bytesCopied += bytesToWrite
                        flowCollector.emit(
                            Progress(language, bytesCopied / stream.total.toFloat())
                        )
                        bytesToWrite = input.read(buffer)
                    }
                }
            }

            flowCollector.emit(Succeed(language, file.readText()))

        } catch (t: Throwable) {
            crashReportManager.logExceptionOrSafeException(t)
            Timber.d(t)
            flowCollector.emit(Failed(language, t))
        }
    }

    override fun deleteLongConsents() {
        longConsentLocalDataSource.deleteLongConsents()
    }
}
