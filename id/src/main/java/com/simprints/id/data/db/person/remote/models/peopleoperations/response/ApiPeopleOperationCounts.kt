package com.simprints.id.data.db.person.remote.models.peopleoperations.response

import androidx.annotation.Keep

@Keep
data class ApiPeopleOperationCounts(
    val create: Int,
    val delete: Int,
    val update: Int
)
