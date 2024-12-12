package com.simprints.infra.images.metadata

import com.simprints.infra.images.metadata.database.DbImageMetadata
import com.simprints.infra.images.metadata.database.ImageMetadataDao
import com.simprints.infra.images.model.Path
import javax.inject.Inject

internal class ImageMetadataStore @Inject constructor(
    private val imageMetadataDao: ImageMetadataDao,
) {
    suspend fun storeMetadata(
        imageKey: Path,
        metadata: Map<String, String>,
    ) = metadata
        .takeIf { it.isNotEmpty() }
        ?.map { (k, v) -> DbImageMetadata(imageId = extractKey(imageKey), key = k, value = v) }
        ?.let { imageMetadataDao.save(it) }

    suspend fun getMetadata(imageKey: Path): Map<String, String> = imageMetadataDao
        .get(extractKey(imageKey))
        .let { metadata -> metadata.associate { it.key to it.value } }

    suspend fun deleteMetadata(imageKey: Path) = imageMetadataDao.delete(extractKey(imageKey))

    suspend fun deleteAllMetadata() = imageMetadataDao.deleteAll()

    // Using only the file name as the key to avoid confusion with the path root
    private fun extractKey(key: Path) = key.parts.last()
}
