package com.simprints.id.data.db.person.local

import com.simprints.id.data.db.person.domain.FingerprintRecord
import kotlinx.coroutines.flow.Flow
import java.io.Serializable

interface FingerprintRecordLocalDataSource {

    suspend fun loadFingerprintRecords(query: Serializable): Flow<FingerprintRecord>
}
