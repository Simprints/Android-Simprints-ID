package com.simprints.fingerprint.activities.matching.request

import android.os.Parcelable
import com.simprints.fingerprint.data.domain.person.Person
import kotlinx.android.parcel.Parcelize

@Parcelize
class MatchingTaskVerifyRequest(override val language: String,
                                override val probe: Person,
                                val queryForVerifyPool: QueryForVerifyPool,
                                val verifyGuid: String): MatchingTaskRequest {

    @Parcelize
    data class QueryForVerifyPool(val projectId: String): Parcelable
}
