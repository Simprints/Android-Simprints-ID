package com.simprints.fingerprint.controllers.core.repository

import com.simprints.fingerprint.data.domain.fingerprint.FingerIdentifier
import com.simprints.fingerprint.data.domain.fingerprint.Fingerprint
import com.simprints.fingerprint.data.domain.fingerprint.FingerprintIdentity
import com.simprints.id.data.db.subject.domain.FingerprintSample
import com.simprints.id.data.db.subject.local.FingerprintIdentityLocalDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.Serializable
import com.simprints.id.data.db.subject.domain.FingerIdentifier as FingerIdentifierCore

class FingerprintDbManagerImpl(private val coreFingerprintIdentityLocalDataSource: FingerprintIdentityLocalDataSource) : FingerprintDbManager {

    override suspend fun loadPeople(query: Serializable): Flow<FingerprintIdentity> =
            coreFingerprintIdentityLocalDataSource
                .loadFingerprintIdentities(query)
                .map {
                    FingerprintIdentity(it.patientId, it.fingerprints.map { fingerprint -> fingerprint.fromCoreToDomain() })
                }
}

fun FingerprintSample.fromCoreToDomain() =
    Fingerprint(fingerIdentifier.fromCoreToDomain(), template)

fun FingerIdentifierCore.fromCoreToDomain(): FingerIdentifier =
    when (this) {
        FingerIdentifierCore.RIGHT_5TH_FINGER -> FingerIdentifier.RIGHT_5TH_FINGER
        FingerIdentifierCore.RIGHT_4TH_FINGER -> FingerIdentifier.RIGHT_4TH_FINGER
        FingerIdentifierCore.RIGHT_3RD_FINGER -> FingerIdentifier.RIGHT_3RD_FINGER
        FingerIdentifierCore.RIGHT_INDEX_FINGER -> FingerIdentifier.RIGHT_INDEX_FINGER
        FingerIdentifierCore.RIGHT_THUMB -> FingerIdentifier.RIGHT_THUMB
        FingerIdentifierCore.LEFT_THUMB -> FingerIdentifier.LEFT_THUMB
        FingerIdentifierCore.LEFT_INDEX_FINGER -> FingerIdentifier.LEFT_INDEX_FINGER
        FingerIdentifierCore.LEFT_3RD_FINGER -> FingerIdentifier.LEFT_3RD_FINGER
        FingerIdentifierCore.LEFT_4TH_FINGER -> FingerIdentifier.LEFT_4TH_FINGER
        FingerIdentifierCore.LEFT_5TH_FINGER -> FingerIdentifier.LEFT_5TH_FINGER
    }
