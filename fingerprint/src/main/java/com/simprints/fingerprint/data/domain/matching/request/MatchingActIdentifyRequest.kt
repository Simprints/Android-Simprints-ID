package com.simprints.fingerprint.data.domain.matching.request

import com.simprints.fingerprint.data.domain.matching.MatchGroup
import com.simprints.id.domain.fingerprint.Person
import kotlinx.android.parcel.Parcelize

@Parcelize
class MatchingActIdentifyRequest(override val language: String,
                                 override val probe: Person,
                                 val matchGroup: MatchGroup,
                                 val returnIdCount: Int): MatchingActRequest
