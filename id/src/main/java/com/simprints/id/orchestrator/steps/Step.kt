package com.simprints.id.orchestrator.steps

import android.content.Intent
import com.simprints.id.orchestrator.steps.Step.Status.COMPLETED

data class Step(val request: Request,
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


    data class Request(val requestCode: Int,
                       val intent: Intent)

    interface Result
}
