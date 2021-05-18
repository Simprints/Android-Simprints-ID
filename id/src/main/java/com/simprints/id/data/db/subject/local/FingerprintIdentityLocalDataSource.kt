package com.simprints.eventsystem.subject.local

import com.simprints.eventsystem.subject.domain.FingerprintIdentity
import com.simprints.id.exceptions.unexpected.InvalidQueryToLoadRecordsException
import kotlinx.coroutines.flow.Flow
import java.io.Serializable

interface FingerprintIdentityLocalDataSource {

    @Throws(InvalidQueryToLoadRecordsException::class)
    suspend fun loadFingerprintIdentities(query: Serializable): Flow<FingerprintIdentity>
}
