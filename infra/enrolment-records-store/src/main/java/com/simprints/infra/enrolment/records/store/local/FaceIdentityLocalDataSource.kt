package com.simprints.infra.enrolment.records.store.local

import com.simprints.infra.enrolment.records.store.domain.models.FaceIdentity
import com.simprints.infra.enrolment.records.store.exceptions.InvalidQueryToLoadRecordsException
import kotlinx.coroutines.flow.Flow
import java.io.Serializable

interface FaceIdentityLocalDataSource {

    @Throws(InvalidQueryToLoadRecordsException::class)
    suspend fun loadFaceIdentities(query: Serializable): Flow<FaceIdentity>
}
