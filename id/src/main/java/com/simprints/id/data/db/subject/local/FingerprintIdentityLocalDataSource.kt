package com.simprints.id.data.db.subject.local

import com.simprints.id.data.db.subject.domain.FingerprintIdentity
import com.simprints.id.exceptions.unexpected.InvalidQueryToLoadRecordsException
import kotlinx.coroutines.flow.Flow
import java.io.Serializable

interface FingerprintIdentityLocalDataSource {

    @Throws(InvalidQueryToLoadRecordsException::class)
    suspend fun loadFingerprintIdentities(query: Serializable): Flow<FingerprintIdentity>
}
