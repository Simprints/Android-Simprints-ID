package com.simprints.id.data.db.local.realm.models

import com.simprints.id.domain.fingerprint.IdFingerprint
import io.realm.RealmObject
import com.simprints.id.domain.fingerprint.IdFingerprint as LibFingerprint

open class rl_Fingerprint (
    var fingerId: Int = 0,
    var template: ByteArray? = null,
    var qualityScore: Int = 0
): RealmObject()

fun rl_Fingerprint.toDomainFingerprint(): IdFingerprint =
    IdFingerprint(
        fingerId = fingerId,
        qualityScore = qualityScore,
        template = template
    )

fun IdFingerprint.toRealmFingerprint(): rl_Fingerprint =
    rl_Fingerprint(
        fingerId = fingerId,
        qualityScore = qualityScore,
        template = template
    )
