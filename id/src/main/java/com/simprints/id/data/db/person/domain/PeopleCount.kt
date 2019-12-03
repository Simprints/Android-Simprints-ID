package com.simprints.id.data.db.person.domain

import androidx.annotation.Keep
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope

@Keep
data class PeopleCount(val subSyncScope: SubSyncScope,
                       val created: Int,
                       val deleted: Int = 0,
                       val updated: Int = 0)

fun PeopleCount.totalCount() = created + deleted + updated
