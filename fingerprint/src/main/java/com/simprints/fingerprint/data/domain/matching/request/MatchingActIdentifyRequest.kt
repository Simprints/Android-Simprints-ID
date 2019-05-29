package com.simprints.fingerprint.data.domain.matching.request

import com.simprints.fingerprint.data.domain.person.Person
import kotlinx.android.parcel.Parcelize
import com.simprints.fingerprint.data.domain.matching.MatchGroup

@Parcelize
class MatchingActIdentifyRequest(override val language: String,
                                 override val probe: Person,
                                 val matchGroup: MatchGroup,
                                 val returnIdCount: Int): MatchingActRequest
