package com.simprints.id.data.consent.longconsent

import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.consent.longconsent.LongConsentFetchResult.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class LongConsentRepositoryImpl(
    private val longConsentLocalDataSource: LongConsentLocalDataSource,
    private val longConsentRemoteDataSource: LongConsentRemoteDataSource,
    private val crashReportManager: CrashReportManager,
    private val dispatcherProvider: DispatcherProvider
) : LongConsentRepository {

    companion object {
        const val DEFAULT_SIZE = 1024
    }

    override fun getLongConsentForLanguages(languages: Array<String>): Flow<Map<String, LongConsentFetchResult>> =
        flow {

        }

    override fun getLongConsentForLanguage(language: String): Flow<LongConsentFetchResult> = flow {
        try {
            val localConsent = longConsentLocalDataSource.getLongConsentText(language)
            if (localConsent.isNotEmpty()) {
                emit(Succeed(language, localConsent))
            } else {
                downloadLongConsentFromFirebaseStorage(this, language)
            }
        } catch (t: Throwable) {
            crashReportManager.logExceptionOrSafeException(t)
            emit(Failed(language, t))
        }
    }

    override suspend fun fetchLongConsent(language: String): String? = null

    override suspend fun downloadLongConsent(languages: Array<String>): ReceiveChannel<Map<String, LongConsentFetchResult>> =
        withContext(dispatcherProvider.io()) {
            return@withContext produce<Map<String, LongConsentFetchResult>> {
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
            flowCollector.emit(Failed(language, t))
        }
    }

    override fun deleteLongConsents() {
        longConsentLocalDataSource.deleteLongConsents()
    }
}
