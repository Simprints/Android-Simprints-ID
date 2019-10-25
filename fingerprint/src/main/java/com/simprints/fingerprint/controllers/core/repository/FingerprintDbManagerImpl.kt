package com.simprints.fingerprint.controllers.core.repository

import com.simprints.core.tools.extentions.singleWithSuspend
import com.simprints.id.data.db.person.domain.FingerprintRecord
import com.simprints.id.data.db.person.local.FingerprintRecordLocalDataSource
import io.reactivex.Single
import kotlinx.coroutines.flow.toList
import java.io.Serializable

class FingerprintDbManagerImpl(private val coreFingerprintRecordLocalDataSource: FingerprintRecordLocalDataSource) : FingerprintDbManager {

    //StopShip: Transform return to fingerprintRecord domain classes

    override fun loadPeople(query: Serializable): Single<List<FingerprintRecord>> =
        singleWithSuspend {
            coreFingerprintRecordLocalDataSource.loadFingerprintRecords(query).toList()
        }
}
