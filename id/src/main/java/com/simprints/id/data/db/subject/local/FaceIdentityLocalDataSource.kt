package com.simprints.eventsystem.subject.local

import com.simprints.eventsystem.subject.domain.FaceIdentity
import com.simprints.id.exceptions.unexpected.InvalidQueryToLoadRecordsException
import kotlinx.coroutines.flow.Flow
import java.io.Serializable

interface FaceIdentityLocalDataSource {

    @Throws(InvalidQueryToLoadRecordsException::class)
    suspend fun loadFaceIdentities(query: Serializable): Flow<FaceIdentity>
}
