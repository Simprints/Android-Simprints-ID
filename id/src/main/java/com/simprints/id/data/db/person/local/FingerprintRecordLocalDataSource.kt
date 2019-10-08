package com.simprints.id.data.db.person.local

import com.simprints.id.data.db.person.domain.FingerprintRecord
import com.simprints.id.exceptions.unexpected.InvalidQueryToLoadRecordsException
import kotlinx.coroutines.flow.Flow
import java.io.Serializable

interface FingerprintRecordLocalDataSource {

    @Throws(InvalidQueryToLoadRecordsException::class)
    suspend fun loadFingerprintRecords(query: Serializable): Flow<FingerprintRecord>
}
