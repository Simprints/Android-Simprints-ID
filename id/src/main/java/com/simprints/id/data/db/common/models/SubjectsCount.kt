package com.simprints.id.data.db.common.models

import androidx.annotation.Keep

@Keep
data class SubjectsCount(val created: Int,
                         val deleted: Int = 0,
                         val updated: Int = 0)

fun SubjectsCount.totalCount() = created + deleted + updated
