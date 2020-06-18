package com.simprints.id.data.db.subject.local

import com.simprints.id.data.db.subject.domain.FaceIdentity
import com.simprints.id.exceptions.unexpected.InvalidQueryToLoadRecordsException
import kotlinx.coroutines.flow.Flow
import java.io.Serializable

interface FaceIdentityLocalDataSource {

    @Throws(InvalidQueryToLoadRecordsException::class)
    suspend fun loadFaceIdentities(query: Serializable): Flow<FaceIdentity>
}
