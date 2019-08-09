package com.simprints.id.orchestrator.steps

import android.os.Parcelable
import com.simprints.id.orchestrator.steps.Step.Status.COMPLETED

data class Step(val requestCode: Int,
                val activityName: String,
                val bundleKey: String,
                val request: Request,
                var status: Status) {

    var result: Result? = null
        set(value) {
            field = value
            if (field != null) {
                status = COMPLETED
            }
        }

    enum class Status {
        NOT_STARTED, ONGOING, COMPLETED
    }

    interface Request : Parcelable
    interface Result
}
