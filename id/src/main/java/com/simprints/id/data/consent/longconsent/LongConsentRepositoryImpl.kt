package com.simprints.id.data.consent.longconsent

import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.consent.longconsent.LongConsentFetchResult.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.withContext
import timber.log.Timber

class LongConsentRepositoryImpl(
    private val longConsentLocalDataSource: LongConsentLocalDataSource,
    private val longConsentRemoteDataSource: LongConsentRemoteDataSource,
    private val crashReportManager: CrashReportManager
) : LongConsentRepository {


    companion object {
        const val DEFAULT_SIZE = 1024
    }

    override suspend fun fetchLongConsent(language: String): String? =
        longConsentLocalDataSource.getLongConsentText(language)

    override suspend fun downloadLongConsent(languages: Array<String>): ReceiveChannel<Map<String, LongConsentFetchResult>> =
        withContext(Dispatchers.IO) {
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
//        try {
//            val stream = longConsentRemoteDataSource.downloadLongConsent(language)
//            val bufferedStream = stream.inputStream.buffered(DEFAULT_SIZE)
//            val file = longConsentLocalDataSource.createFileForLanguage(language)
//            var count = 0
//            Timber.d("$language: Ready to download ${stream.total} bytes")
//
//            while (stream.) {
//                val bytes = bufferedStream.readBytes()
//                file.writeBytes(bytes)
//                updateStateAndEmit(
//                    downloadStateChannel,
//                    downloadState,
//                    Progress(language, (bytes.size / totalToDownload).toFloat())
//                )
//                count += bytes.size
//                Timber.d("$language: Stored ${bytes.size} / $totalToDownload")
//            }
//        } catch (t: Throwable) {
//            updateStateAndEmit(downloadStateChannel, downloadState, Failed(language, t))
//        }
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
