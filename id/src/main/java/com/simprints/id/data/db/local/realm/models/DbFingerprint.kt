package com.simprints.id.data.db.local.realm.models

import com.simprints.id.FingerIdentifier
import com.simprints.id.domain.fingerprint.Fingerprint
import io.realm.RealmObject

open class DbFingerprint (
    var fingerId: Int = 0,
    var template: ByteArray? = null,
    var qualityScore: Int = 0
): RealmObject()

fun DbFingerprint.toDomainFingerprint(): Fingerprint =
    Fingerprint(
        fingerId = FingerIdentifier.values()[fingerId],
        isoTemplateBytes = template ?: throw IllegalArgumentException("Unexpected null fingerprint template")
    )

fun Fingerprint.toRealmFingerprint(): DbFingerprint =
    DbFingerprint(
        fingerId = fingerId.ordinal,
        qualityScore = qualityScore,
        template = templateBytes
    )
