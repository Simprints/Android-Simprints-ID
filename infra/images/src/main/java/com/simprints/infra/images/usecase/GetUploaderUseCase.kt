package com.simprints.infra.images.usecase

import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.experimental
import com.simprints.infra.images.remote.SampleUploader
import com.simprints.infra.images.remote.firestore.FirestoreSampleUploader
import com.simprints.infra.images.remote.signedurl.SignedUrlSampleUploader
import javax.inject.Inject

internal class GetUploaderUseCase @Inject constructor(
    private val configRepository: ConfigRepository,
    private val firestoreUploader: FirestoreSampleUploader,
    private val signedUrlUploader: SignedUrlSampleUploader,
) {
    suspend operator fun invoke(): SampleUploader = configRepository
        .getProjectConfiguration()
        .experimental()
        .sampleUploadWithSignedUrlEnabled
        .let { if (it) signedUrlUploader else firestoreUploader }
}
