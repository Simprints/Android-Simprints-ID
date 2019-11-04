package com.simprints.fingerprint.controllers.core.repository

import com.simprints.core.tools.extentions.singleWithSuspend
import com.simprints.fingerprint.data.domain.fingerprint.Fingerprint
import com.simprints.fingerprint.data.domain.fingerprint.FingerprintIdentity
import com.simprints.id.data.db.person.local.FingerprintIdentityLocalDataSource
import io.reactivex.Single
import kotlinx.coroutines.flow.toList
import java.io.Serializable

class FingerprintDbManagerImpl(private val coreFingerprintIdentityLocalDataSource: FingerprintIdentityLocalDataSource) : FingerprintDbManager {

    override fun loadPeople(query: Serializable): Single<List<FingerprintIdentity>> =
        singleWithSuspend {
            coreFingerprintIdentityLocalDataSource
                .loadFingerprintIdentities(query)
                .toList()
                .map {
                    FingerprintIdentity(it.id, it.fingerprints.map { fingerprint -> Fingerprint.fromCoreToDomain(fingerprint) })
                }
        }
}
