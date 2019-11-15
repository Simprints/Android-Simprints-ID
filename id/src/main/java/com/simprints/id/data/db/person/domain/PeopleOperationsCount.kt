package com.simprints.id.data.db.person.domain

import androidx.annotation.Keep
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope

@Keep
class PeopleOperationsCount(val subSyncScope: SubSyncScope,
                            val lastKnownPatientId: String?,
                            val lastKnownPatientUpdatedAt: Long?)
