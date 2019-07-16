package com.simprints.fingerprint.tasks.saveperson

import com.simprints.fingerprint.orchestrator.task.TaskResult
import kotlinx.android.parcel.Parcelize

@Parcelize
class SavePersonTaskResult(val success: Boolean) : TaskResult
