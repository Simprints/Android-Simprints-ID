package com.simprints.id.data.db.person

interface PersonRepositoryUpSyncHelper {
    suspend fun executeUpload()
}
