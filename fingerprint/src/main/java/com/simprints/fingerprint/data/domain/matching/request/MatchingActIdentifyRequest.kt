package com.simprints.fingerprint.data.domain.matching.request

import android.os.Parcelable
import com.simprints.fingerprint.data.domain.person.Person
import kotlinx.android.parcel.Parcelize

@Parcelize
class MatchingActIdentifyRequest(override val language: String,
                                 override val probe: Person,
                                 val queryForIdentifyPool: QueryForIdentifyPool,
                                 val returnIdCount: Int): MatchingActRequest {

    @Parcelize
    data class QueryForIdentifyPool(val projectId: String,
                                    val userId: String? = null,
                                    val moduleId: String? = null): Parcelable
}
