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
import timber.log.Timber

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

    override fun getLongConsentForLanguage(language: String): Flow<LongConsentFetchResult> =
        flow<LongConsentFetchResult> {
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
            Timber.d("$language: Ready to download ${stream.total} bytes")

            stream.inputStream.use {
                var bytesCopied: Long = 0
                val buffer = ByteArray(DEFAULT_SIZE)
                var bytesToWrite = it.read(buffer)
                while (bytesToWrite >= 0) {
                    file.writeBytes(buffer)
                    bytesCopied += bytesToWrite
                    flowCollector.emit(
                        Progress(language, (bytesCopied / stream.total).toFloat())
                    )
                    Timber.d("$language: Stored $bytesCopied / ${stream.total}")
                    bytesToWrite = it.read(buffer)
                }
            }
        } catch (t: Throwable) {
            flowCollector.emit(Failed(language, t))
        }
    }

    override fun deleteLongConsents() {
        longConsentLocalDataSource.deleteLongConsents()
    }
}
