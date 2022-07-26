package com.simprints.id.data.consent.longconsent

import com.simprints.id.data.consent.longconsent.LongConsentFetchResult.*
import com.simprints.id.data.consent.longconsent.local.LongConsentLocalDataSource
import com.simprints.id.data.consent.longconsent.remote.LongConsentRemoteDataSource
import com.simprints.infra.logging.Simber
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.network.exceptions.isCausedFromBadNetworkConnection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow

class LongConsentRepositoryImpl(
    private val longConsentLocalDataSource: LongConsentLocalDataSource,
    private val longConsentRemoteDataSource: LongConsentRemoteDataSource,
) : LongConsentRepository {

    override fun getLongConsentResultForLanguage(language: String): Flow<LongConsentFetchResult> =
        flow {
            try {
                val localConsent = longConsentLocalDataSource.getLongConsentText(language)
                if (localConsent.isNotEmpty()) {
                    emit(Succeed(language, localConsent))
                } else {
                    downloadLongConsentFromFirebaseStorage(this, language)
                }
            } catch (t: Throwable) {
                Simber.e(t)
                emit(Failed(language, t))
            }
        }

    private suspend fun downloadLongConsentFromFirebaseStorage(
        flowCollector: FlowCollector<LongConsentFetchResult>,
        language: String
    ) {
        try {

            flowCollector.emit(InProgress(language))

            val fileBytes = longConsentRemoteDataSource.downloadLongConsent(language)
            val file = longConsentLocalDataSource.createFileForLanguage(language)
            file.outputStream().use { output ->
                output.write(fileBytes.bytes)
            }

            flowCollector.emit(Succeed(language, file.readText()))
        } catch (t: Throwable) {
            when {
                t is BackendMaintenanceException -> Simber.i(t)
                t.isCausedFromBadNetworkConnection() -> Simber.i(t)
                else -> Simber.e(t)
            }

            flowCollector.emit(
                if (t is BackendMaintenanceException) {
                    FailedBecauseBackendMaintenance(language, t, t.estimatedOutage)
                } else {
                    Failed(language, t)
                }
            )
        }
    }

    override fun deleteLongConsents() {
        longConsentLocalDataSource.deleteLongConsents()
    }
}
