package com.simprints.id.data.consent.longconsent

import com.simprints.core.tools.extentions.getEstimatedOutage
import com.simprints.core.tools.extentions.isBackendMaitenanceException
import com.simprints.id.data.consent.longconsent.LongConsentFetchResult.Failed
import com.simprints.id.data.consent.longconsent.LongConsentFetchResult.FailedBecauseBackendMaintenance
import com.simprints.id.data.consent.longconsent.LongConsentFetchResult.InProgress
import com.simprints.id.data.consent.longconsent.LongConsentFetchResult.Succeed
import com.simprints.id.data.consent.longconsent.local.LongConsentLocalDataSource
import com.simprints.id.data.consent.longconsent.remote.LongConsentRemoteDataSource
import com.simprints.logging.Simber
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow

class LongConsentRepositoryImpl(
    private val longConsentLocalDataSource: LongConsentLocalDataSource,
    private val longConsentRemoteDataSource: LongConsentRemoteDataSource,
) : LongConsentRepository {

    override fun getLongConsentResultForLanguage(language: String): Flow<LongConsentFetchResult> = flow {
        try {
            val localConsent = longConsentLocalDataSource.getLongConsentText(language)
            if (localConsent.isNotEmpty()) {
                emit(Succeed(language, localConsent))
            } else {
                downloadLongConsentFromFirebaseStorage(this, language)
            }
        } catch (t: Throwable) {
            Simber.e(t)
            if (t.isBackendMaitenanceException()) {
                emit(FailedBecauseBackendMaintenance(language, t, t.getEstimatedOutage()))
            } else {
                emit(Failed(language, t))
            }
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
            Simber.e(t)
            flowCollector.emit(
                if (t.isBackendMaitenanceException()) {
                    FailedBecauseBackendMaintenance(language, t, t.getEstimatedOutage())
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
