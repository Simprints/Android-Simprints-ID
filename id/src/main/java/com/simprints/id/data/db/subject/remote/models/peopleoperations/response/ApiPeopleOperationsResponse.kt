package com.simprints.id.data.db.subject.remote.models.peopleoperations.response

import androidx.annotation.Keep

@Keep
data class ApiPeopleOperationsResponse(
    val groups: List<ApiPeopleOperationGroupResponse>
)
