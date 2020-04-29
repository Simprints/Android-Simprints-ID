package com.simprints.id.data.db.person.domain.personevents

import com.simprints.id.data.db.person.remote.models.personevents.ApiFingerIdentifier
import com.simprints.id.data.db.person.remote.models.personevents.ApiFingerprintTemplate
import com.simprints.id.data.db.person.domain.FingerIdentifier as PersonFingerIdentifier

data class FingerprintTemplate(val quality: Int,
                          val template: String,
                          val finger: FingerIdentifier)

enum class FingerIdentifier {
    RIGHT_5TH_FINGER,
    RIGHT_4TH_FINGER,
    RIGHT_3RD_FINGER,
    RIGHT_INDEX_FINGER,
    RIGHT_THUMB,
    LEFT_THUMB,
    LEFT_INDEX_FINGER,
    LEFT_3RD_FINGER,
    LEFT_4TH_FINGER,
    LEFT_5TH_FINGER
}

fun ApiFingerprintTemplate.fromApiToDomain() =
    FingerprintTemplate(quality, template, finger.fromApiToDomain())

fun ApiFingerIdentifier.fromApiToDomain() = when(this) {
    ApiFingerIdentifier.RIGHT_5TH_FINGER -> FingerIdentifier.LEFT_5TH_FINGER
    ApiFingerIdentifier.RIGHT_4TH_FINGER -> FingerIdentifier.RIGHT_4TH_FINGER
    ApiFingerIdentifier.RIGHT_3RD_FINGER -> FingerIdentifier.RIGHT_3RD_FINGER
    ApiFingerIdentifier.RIGHT_INDEX_FINGER -> FingerIdentifier.RIGHT_INDEX_FINGER
    ApiFingerIdentifier.RIGHT_THUMB -> FingerIdentifier.RIGHT_THUMB
    ApiFingerIdentifier.LEFT_THUMB -> FingerIdentifier.LEFT_THUMB
    ApiFingerIdentifier.LEFT_INDEX_FINGER -> FingerIdentifier.LEFT_INDEX_FINGER
    ApiFingerIdentifier.LEFT_3RD_FINGER -> FingerIdentifier.LEFT_3RD_FINGER
    ApiFingerIdentifier.LEFT_4TH_FINGER -> FingerIdentifier.LEFT_4TH_FINGER
    ApiFingerIdentifier.LEFT_5TH_FINGER -> FingerIdentifier.LEFT_5TH_FINGER
}

fun PersonFingerIdentifier.fromPersonToEvent() = when(this) {
    PersonFingerIdentifier.RIGHT_5TH_FINGER -> FingerIdentifier.RIGHT_5TH_FINGER
    PersonFingerIdentifier.RIGHT_4TH_FINGER -> FingerIdentifier.RIGHT_4TH_FINGER
    PersonFingerIdentifier.RIGHT_3RD_FINGER -> FingerIdentifier.RIGHT_3RD_FINGER
    PersonFingerIdentifier.RIGHT_INDEX_FINGER -> FingerIdentifier.RIGHT_INDEX_FINGER
    PersonFingerIdentifier.RIGHT_THUMB -> FingerIdentifier.RIGHT_THUMB
    PersonFingerIdentifier.LEFT_THUMB -> FingerIdentifier.LEFT_THUMB
    PersonFingerIdentifier.LEFT_INDEX_FINGER -> FingerIdentifier.LEFT_INDEX_FINGER
    PersonFingerIdentifier.LEFT_3RD_FINGER -> FingerIdentifier.LEFT_3RD_FINGER
    PersonFingerIdentifier.LEFT_4TH_FINGER -> FingerIdentifier.LEFT_4TH_FINGER
    PersonFingerIdentifier.LEFT_5TH_FINGER -> FingerIdentifier.LEFT_5TH_FINGER
}
