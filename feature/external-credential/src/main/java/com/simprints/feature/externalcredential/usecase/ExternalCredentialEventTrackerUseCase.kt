package com.simprints.feature.externalcredential.usecase

import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.externalcredential.screens.scanocr.usecase.CalculateLevenshteinDistanceUseCase
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredential
import com.simprints.feature.externalcredential.screens.search.model.toExternalCredential
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.events.event.domain.models.ExternalCredentialCaptureEvent
import com.simprints.infra.events.event.domain.models.ExternalCredentialCaptureValueEvent
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.infra.logging.Simber
import javax.inject.Inject

internal class ExternalCredentialEventTrackerUseCase @Inject constructor(
    private val timeHelper: TimeHelper,
    private val authStore: AuthStore,
    private val configManager: ConfigManager,
    private val tokenizationProcessor: TokenizationProcessor,
    private val eventRepository: SessionEventRepository,
    private val calculateDistance: CalculateLevenshteinDistanceUseCase,
) {
    suspend fun saveCaptureEvents(
        startTime: Timestamp,
        subjectId: String,
        scannedCredential: ScannedCredential,
    ) {
        Simber.d("Saving External Credential Events for $scannedCredential")
        val credential = scannedCredential.toExternalCredential(subjectId)
        eventRepository.addOrUpdateEvent(
            ExternalCredentialCaptureValueEvent(
                createdAt = timeHelper.now(),
                payloadId = scannedCredential.credentialScanId,
                credential = credential,
            ),
        )

        eventRepository.addOrUpdateEvent(
            ExternalCredentialCaptureEvent(
                startTime = startTime,
                endTime = timeHelper.now(),
                payloadId = scannedCredential.credentialScanId,
                autoCaptureStartTime = scannedCredential.scanStartTime,
                autoCaptureEndTime = scannedCredential.scanEndTime,
                ocrErrorCount = calculateOcrErrorCount(scannedCredential),
                capturedTextLength = getActualCapturedCredentialLength(scannedCredential),
                credentialTextLength = getExpectedCredentialValueLength(credential),
                selectionId = "", // TODO - add ExternalCredentialSelectionEvent eventId here
            ),
        )
    }

    private fun getActualCapturedCredentialLength(scannedCredential: ScannedCredential): Int = scannedCredential.scannedValue.value.length

    private fun getExpectedCredentialValueLength(credential: ExternalCredential): Int = when (credential.type) {
        ExternalCredentialType.NHISCard -> NHIS_CARD_ID_LENGTH
        ExternalCredentialType.GhanaIdCard -> GHANA_ID_CARD_ID_LENGTH
        ExternalCredentialType.QRCode -> QR_CODE_LENGTH
    }

    private suspend fun calculateOcrErrorCount(scannedCredential: ScannedCredential): Int {
        val project = configManager.getProject(authStore.signedInProjectId)
        val actualCredentialRaw = tokenizationProcessor.decrypt(
            scannedCredential.credential,
            TokenKeyType.ExternalCredential,
            project,
        )
        return calculateDistance(
            scannedCredential.scannedValue.value,
            actualCredentialRaw.value,
        )
    }

    companion object Companion {
        private const val NHIS_CARD_ID_LENGTH = 8
        private const val GHANA_ID_CARD_ID_LENGTH = 15
        private const val QR_CODE_LENGTH = 6
    }
}
