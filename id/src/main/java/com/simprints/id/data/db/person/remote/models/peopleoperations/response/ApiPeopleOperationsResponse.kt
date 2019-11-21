package com.simprints.id.data.db.person.remote.models.peopleoperations.response

import androidx.annotation.Keep

@Keep
data class ApiPeopleOperationsResponse(
    val groups: List<ApiPeopleOperationGroupResponse>
)
