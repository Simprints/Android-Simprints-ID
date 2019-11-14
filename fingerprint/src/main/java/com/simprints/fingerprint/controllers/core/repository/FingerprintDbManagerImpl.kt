package com.simprints.fingerprint.controllers.core.repository

import com.simprints.core.tools.extentions.singleWithSuspend
import com.simprints.fingerprint.data.domain.fingerprint.FingerIdentifier
import com.simprints.fingerprint.data.domain.fingerprint.Fingerprint
import com.simprints.fingerprint.data.domain.fingerprint.FingerprintIdentity
import com.simprints.id.data.db.person.domain.FingerprintSample
import com.simprints.id.data.db.person.local.FingerprintIdentityLocalDataSource
import com.simprints.id.data.db.person.domain.FingerIdentifier as FingerIdentifierCore
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
                    FingerprintIdentity(it.patientId, it.fingerprints.map { fingerprint -> fingerprint.fromCoreToDomain() })
                }
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
