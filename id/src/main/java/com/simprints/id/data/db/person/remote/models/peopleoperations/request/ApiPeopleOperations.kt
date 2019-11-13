package com.simprints.id.data.db.person.remote.models.peopleoperations.request

import androidx.annotation.Keep

@Keep
data class ApiPeopleOperations(val groups: List<ApiPeopleOperationGroup>)
