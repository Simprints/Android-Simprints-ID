package com.simprints.id.data.db.image.repository

interface ImageRepository {
    suspend fun uploadStoredImagesAndDelete(): Boolean
}
