package com.simprints.fingerprint.tasks.saveperson

import com.simprints.fingerprint.data.domain.person.Person
import com.simprints.fingerprint.orchestrator.task.TaskRequest
import kotlinx.android.parcel.Parcelize

@Parcelize
class SavePersonTaskRequest(val person: Person) : TaskRequest
