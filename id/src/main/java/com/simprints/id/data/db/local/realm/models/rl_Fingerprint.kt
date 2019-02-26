package com.simprints.id.data.db.local.realm.models

import com.simprints.id.domain.fingerprint.Fingerprint
import io.realm.RealmObject
import com.simprints.id.domain.fingerprint.Fingerprint as LibFingerprint

open class rl_Fingerprint (
    var fingerId: Int = 0,
    var template: ByteArray? = null,
    var qualityScore: Int = 0
): RealmObject()

fun rl_Fingerprint.toDomainFingerprint(): Fingerprint =
    Fingerprint(
        fingerId = fingerId,
        qualityScore = qualityScore,
        template = template
    )

fun Fingerprint.toRealmFingerprint(): rl_Fingerprint =
    rl_Fingerprint(
        fingerId = fingerId,
        qualityScore = qualityScore,
        template = template
    )
