package com.simprints.id.data.db.common.models

import androidx.annotation.Keep

@Keep
data class PeopleCount(val created: Int,
                       val deleted: Int = 0,
                       val updated: Int = 0)

fun PeopleCount.totalCount() = created + deleted + updated

// Cloud returns always the last state only, so if a patient is created, updated and then deleted,
// only the deletion is in the down stream. So # of patients from the down sync stream is equivalent to the creations.
fun PeopleCount.fromDownSync() = created
