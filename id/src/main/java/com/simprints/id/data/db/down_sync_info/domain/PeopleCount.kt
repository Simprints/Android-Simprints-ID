package com.simprints.id.data.db.syncscope.domain

import androidx.annotation.Keep

@Keep
data class PeopleCount(val created: Int,
                       val deleted: Int = 0,
                       val updated: Int = 0)

fun PeopleCount.totalCount() = created + deleted + updated
