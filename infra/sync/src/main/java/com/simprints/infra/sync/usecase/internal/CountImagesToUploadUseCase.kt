package com.simprints.infra.sync.usecase.internal

import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.images.ImageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CountImagesToUploadUseCase @Inject constructor(
    private val configRepository: ConfigRepository,
    private val imageRepository: ImageRepository,
) {

    internal operator fun invoke(): Flow<Int> =
        configRepository.observeProjectConfiguration()
            .map { it.projectId }
            .flatMapLatest(imageRepository::observeNumberOfImagesToUpload)
            .distinctUntilChanged()

}
