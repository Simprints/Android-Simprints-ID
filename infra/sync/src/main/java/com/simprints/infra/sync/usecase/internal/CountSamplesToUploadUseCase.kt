package com.simprints.infra.sync.usecase.internal

import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.images.ImageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Observes sample count to upload, while tracking the project ID changes.
 *
 * Note: CountSamplesToUploadUseCase bridges
 * the older term of "image" in its dependencies
 * with
 * the newer term "sample" that the use case exposes.
 */
internal class CountSamplesToUploadUseCase @Inject constructor(
    private val configRepository: ConfigRepository,
    private val imageRepository: ImageRepository,
) {
    internal operator fun invoke(): Flow<Int> = configRepository
        .observeProjectConfiguration()
        .map { it.projectId }
        .flatMapLatest(imageRepository::observeNumberOfImagesToUpload) // images are samples in renewed terms
        .distinctUntilChanged()
}
