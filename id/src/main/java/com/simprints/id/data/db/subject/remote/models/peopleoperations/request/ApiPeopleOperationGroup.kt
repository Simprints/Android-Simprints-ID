package com.simprints.id.data.db.subject.remote.models.peopleoperations.request

import androidx.annotation.Keep

@Keep
data class ApiPeopleOperationGroup(val lastKnownPatient: ApiLastKnownPatient?,
                                   val whereLabels: List<ApiPeopleOperationWhereLabel>)
