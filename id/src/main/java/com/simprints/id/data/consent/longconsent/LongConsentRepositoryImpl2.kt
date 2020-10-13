package com.simprints.id.data.consent.longconsent

import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.consent.longconsent.LongConsentFetchResult.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import timber.log.Timber

class LongConsentRepositoryImpl2(
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

    }

    override suspend fun fetchLongConsent(language: String): String? =
        longConsentLocalDataSource.getLongConsentText(language)

    override suspend fun downloadLongConsent(languages: Array<String>): ReceiveChannel<Map<String, LongConsentFetchResult>> =
        withContext(dispatcherProvider.io()) {
            return@withContext produce<Map<String, LongConsentFetchResult>> {
                val downloadState = createDownloadState(languages)
                try {
                    languages.forEach { language ->
                        val localConsent = longConsentLocalDataSource.getLongConsentText(language)
                        if (localConsent.isNotEmpty()) {
                            updateStateAndEmit(this.channel, downloadState, Succeed(language, localConsent))
                        } else {
                            downloadLongConsentFromFirebaseStorage(this.channel, downloadState, language)
                        }
                    }
                } catch (t: Throwable) {
                    crashReportManager.logExceptionOrSafeException(t)
                }
            }
        }

    private fun createDownloadState(languages: Array<String>) =
        languages.map { it to Progress(it, 0F) as LongConsentFetchResult }.toMap().toMutableMap()


    private suspend fun downloadLongConsentFromFirebaseStorage(
        downloadStateChannel: SendChannel<Map<String, LongConsentFetchResult>>,
        downloadState: MutableMap<String, LongConsentFetchResult>,
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
                    updateStateAndEmit(
                        downloadStateChannel,
                        downloadState,
                        Progress(language, (bytesCopied / stream.total).toFloat())
                    )
                    Timber.d("$language: Stored $bytesCopied / ${stream.total}")
                    bytesToWrite = it.read(buffer)
                }
            }
        } catch (t: Throwable) {
            updateStateAndEmit(downloadStateChannel, downloadState, Failed(language, t))
        }
    }

    private suspend fun updateStateAndEmit(
        downloadStateChannel: SendChannel<Map<String, LongConsentFetchResult>>,
        downloadState: MutableMap<String, LongConsentFetchResult>,
        newResult: LongConsentFetchResult
    ) {
        try {
            if (downloadState[newResult.language] != newResult) {
                downloadState[newResult.language] = newResult
                sendState(downloadStateChannel, downloadState)
            }
        } catch (t: Throwable) {
            Timber.d(t)
        }
    }

    private suspend fun sendState(
        downloadStateChannel: SendChannel<Map<String, LongConsentFetchResult>>,
        downloadState: MutableMap<String, LongConsentFetchResult>
    ) {
        if (!downloadStateChannel.isClosedForSend) {
            downloadStateChannel.send(downloadState)
        }

        if (!downloadState.values.any { it is Progress }) {
            downloadStateChannel.close()
        }
    }

    override fun deleteLongConsents() {
        longConsentLocalDataSource.deleteLongConsents()
    }
}
