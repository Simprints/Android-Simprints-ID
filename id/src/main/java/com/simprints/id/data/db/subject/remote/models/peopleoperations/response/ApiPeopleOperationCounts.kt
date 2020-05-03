package com.simprints.id.data.db.subject.remote.models.peopleoperations.response

import androidx.annotation.Keep

@Keep
data class ApiPeopleOperationCounts(
    val create: Int,
    val delete: Int,
    val update: Int
)
