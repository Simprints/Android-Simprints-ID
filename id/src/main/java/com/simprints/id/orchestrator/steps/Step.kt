package com.simprints.id.orchestrator.steps

import android.os.Parcelable
import com.simprints.id.domain.moduleapi.face.requests.FaceRequest
import com.simprints.id.domain.moduleapi.face.requests.fromDomainToModuleApi
import com.simprints.id.domain.moduleapi.fingerprint.DomainToModuleApiFingerprintRequest.fromDomainToModuleApiFingerprintRequest
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.id.orchestrator.steps.Step.Status.COMPLETED
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Step(val requestCode: Int,
                val activityName: String,
                val bundleKey: String,
                val request: Request,
                var status: Status) : Parcelable {

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
    interface Result : Parcelable
}

fun Step.Request.fromDomainToModuleApi(): Parcelable =
    when {
        this is FingerprintRequest -> fromDomainToModuleApiFingerprintRequest(this)
        this is FaceRequest -> fromDomainToModuleApi()
        else -> throw Throwable("Invalid Request")
    }
