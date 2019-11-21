package com.simprints.id.data.db.person.remote.models.peopleoperations.response

data class ApiPeopleOperationCounts(
    val create: Int,
    val delete: Int,
    val update: Int
)

fun ApiPeopleOperationCounts.sumUp() = create + update + delete
