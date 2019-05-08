package com.simprints.fingerprint.data.domain.matching.request

import com.simprints.fingerprint.data.domain.person.Person
import kotlinx.android.parcel.Parcelize

@Parcelize
class MatchingActVerifyRequest(override val language: String,
                               override val probe: Person,
                               val projectId: String,
                               val verifyGuid: String): MatchingActRequest
