package com.simprints.infra.images.remote.signedurl.usecase

import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.samples.SampleUpSyncRequestEvent
import com.simprints.infra.events.event.domain.models.scope.EventScope
import com.simprints.infra.images.local.ImageLocalDataSource
import com.simprints.infra.images.remote.signedurl.SampleUploadData
import com.simprints.infra.images.remote.signedurl.api.SampleUploadApiInterface
import com.simprints.infra.images.remote.signedurl.api.SampleUploadRequestBody
import com.simprints.infra.logging.LoggingConstants
import com.simprints.infra.logging.Simber
import java.util.UUID
import javax.inject.Inject

internal class UploadSampleWithTrackingUseCase @Inject constructor(
    private val timeHelper: TimeHelper,
    private val authStore: AuthStore,
    private val localDataSource: ImageLocalDataSource,
    private val eventRepository: EventRepository,
) {
    suspend operator fun invoke(
        urlRequestScope: EventScope,
        url: String,
        sample: SampleUploadData,
    ): Boolean {
        val requestId = UUID.randomUUID().toString()
        val requestStartTime = timeHelper.now()

        val errorType = uploadSample(requestId, url, sample)
        eventRepository.addOrUpdateEvent(
            scope = urlRequestScope,
            event = SampleUpSyncRequestEvent(
                createdAt = requestStartTime,
                endedAt = timeHelper.now(),
                requestId = requestId,
                sampleId = sample.sampleId,
                size = sample.size,
                errorType = errorType,
            ),
        )
        return errorType.isNullOrBlank()
    }

    private suspend fun uploadSample(
        requestId: String,
        url: String,
        sampleData: SampleUploadData,
    ): String? {
        val fileStream = localDataSource.decryptImage(sampleData.imageRef)
        if (fileStream == null) {
            return "Image decryption failed"
        }

        return fileStream.use { stream ->
            try {
                val client = authStore.buildClient(SampleUploadApiInterface::class)
                val response = client.executeCall { api ->
                    api.uploadFile(
                        uploadUrl = url,
                        requestId = requestId,
                        md5 = sampleData.md5,
                        requestBody = SampleUploadRequestBody(stream, sampleData.size),
                    )
                }
                if (response.isSuccessful) {
                    null
                } else {
                    response.errorBody()?.string().also {
                        Simber.i("Failed to upload image: $it", tag = LoggingConstants.CrashReportTag.SYNC)
                    }
                }
            } catch (e: Exception) {
                Simber.e("Failed to upload image", e, tag = LoggingConstants.CrashReportTag.SYNC)
                e.javaClass.simpleName
            }
        }
    }
}
