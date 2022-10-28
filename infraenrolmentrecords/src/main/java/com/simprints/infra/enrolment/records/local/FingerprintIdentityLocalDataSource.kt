package com.simprints.infra.enrolment.records.local

import com.simprints.infra.enrolment.records.domain.models.FingerprintIdentity
import com.simprints.infra.enrolment.records.exceptions.InvalidQueryToLoadRecordsException
import kotlinx.coroutines.flow.Flow
import java.io.Serializable

internal interface FingerprintIdentityLocalDataSource {

    @Throws(InvalidQueryToLoadRecordsException::class)
    suspend fun loadFingerprintIdentities(query: Serializable): Flow<FingerprintIdentity>
}
