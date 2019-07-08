package com.simprints.fingerprint.activities.matching.request

import android.os.Parcelable
import com.simprints.fingerprint.data.domain.person.Person
import kotlinx.android.parcel.Parcelize

@Parcelize
class MatchingTaskIdentifyRequest(override val language: String,
                                  override val probe: Person,
                                  val queryForIdentifyPool: QueryForIdentifyPool,
                                  val returnIdCount: Int): MatchingTaskRequest {

    @Parcelize
    data class QueryForIdentifyPool(val projectId: String,
                                    val userId: String? = null,
                                    val moduleId: String? = null): Parcelable
}
