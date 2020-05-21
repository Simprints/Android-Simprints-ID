package com.simprints.id.data.db.person.local

import com.simprints.id.data.db.person.domain.FaceRecord
import com.simprints.id.exceptions.unexpected.InvalidQueryToLoadRecordsException
import kotlinx.coroutines.flow.Flow
import java.io.Serializable

interface FaceRecordLocalDataSource {

    @Throws(InvalidQueryToLoadRecordsException::class)
    suspend fun loadFaceRecords(query: Serializable): Flow<FaceRecord>
}
