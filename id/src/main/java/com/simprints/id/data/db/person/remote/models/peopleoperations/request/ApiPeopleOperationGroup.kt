package com.simprints.id.data.db.person.remote.models.peopleoperations.request

import androidx.annotation.Keep

@Keep
data class ApiPeopleOperationGroup(val lastKnownPatient: ApiLastKnownPatient,
                                   val whereLabels: List<ApiPeopleOperationWhereLabel>)
