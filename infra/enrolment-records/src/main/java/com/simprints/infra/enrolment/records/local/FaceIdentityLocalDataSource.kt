package com.simprints.infra.enrolment.records.local

import com.simprints.infra.enrolment.records.domain.models.FaceIdentity
import com.simprints.infra.enrolment.records.exceptions.InvalidQueryToLoadRecordsException
import kotlinx.coroutines.flow.Flow
import java.io.Serializable

internal interface FaceIdentityLocalDataSource {

    @Throws(InvalidQueryToLoadRecordsException::class)
    suspend fun loadFaceIdentities(query: Serializable): Flow<FaceIdentity>
}
