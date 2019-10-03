package com.simprints.id.data.db.person.local

import com.simprints.id.data.db.person.domain.FingerprintRecord
import com.simprints.id.exceptions.unexpected.InvalidQueryToLoadRecords
import kotlinx.coroutines.flow.Flow
import java.io.Serializable

interface FingerprintRecordLocalDataSource {

    @Throws(InvalidQueryToLoadRecords::class)
    suspend fun loadFingerprintRecords(query: Serializable): Flow<FingerprintRecord>
}
