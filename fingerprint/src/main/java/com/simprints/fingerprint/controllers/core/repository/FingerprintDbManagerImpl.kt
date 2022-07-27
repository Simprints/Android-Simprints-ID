package com.simprints.fingerprint.controllers.core.repository

import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.fingerprint.data.domain.fingerprint.Fingerprint
import com.simprints.fingerprint.data.domain.fingerprint.FingerprintIdentity
import com.simprints.fingerprint.data.domain.fingerprint.fromModuleApiToDomain
import com.simprints.id.data.db.subject.local.FingerprintIdentityLocalDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.Serializable

class FingerprintDbManagerImpl(private val coreFingerprintIdentityLocalDataSource: FingerprintIdentityLocalDataSource) : FingerprintDbManager {

    override suspend fun loadPeople(query: Serializable): Flow<FingerprintIdentity> =
            coreFingerprintIdentityLocalDataSource
                .loadFingerprintIdentities(query)
                .map {
                    FingerprintIdentity(it.patientId, it.fingerprints.map { fingerprint -> fingerprint.fromCoreToDomain() })
                }
}

fun FingerprintSample.fromCoreToDomain() =
    Fingerprint(fingerIdentifier.fromModuleApiToDomain(), template)
