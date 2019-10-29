package com.simprints.fingerprint.controllers.core.repository

import com.simprints.core.tools.extentions.singleWithSuspend
import com.simprints.fingerprint.data.domain.fingerprint.Fingerprint
import com.simprints.fingerprint.data.domain.fingerprint.FingerprintRecord
import com.simprints.id.data.db.person.local.FingerprintRecordLocalDataSource
import io.reactivex.Single
import kotlinx.coroutines.flow.toList
import java.io.Serializable

class FingerprintDbManagerImpl(private val coreFingerprintRecordLocalDataSource: FingerprintRecordLocalDataSource) : FingerprintDbManager {

    override fun loadPeople(query: Serializable): Single<List<FingerprintRecord>> =
        singleWithSuspend {
            coreFingerprintRecordLocalDataSource
                .loadFingerprintRecords(query)
                .toList()
                .groupBy { it.personId } // STOPSHIP : Loading inefficiency as fingerprint records are kept individually rather than unified under the personId
                .entries
                .map { (personId, fingerprints) ->
                    FingerprintRecord(personId, fingerprints.map { Fingerprint.fromCoreToDomain(it) })
                }
        }
}
