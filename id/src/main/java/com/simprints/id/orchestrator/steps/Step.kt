package com.simprints.id.orchestrator.steps

import android.os.Parcel
import android.os.Parcelable
import com.simprints.id.domain.moduleapi.face.requests.FaceRequest
import com.simprints.id.domain.moduleapi.face.requests.fromDomainToModuleApi
import com.simprints.id.domain.moduleapi.fingerprint.DomainToModuleApiFingerprintRequest.fromDomainToModuleApiFingerprintRequest
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.id.orchestrator.steps.Step.Status.COMPLETED

data class Step(
    val requestCode: Int,
    val activityName: String,
    val bundleKey: String,
    val request: Request,
    var status: Status
) : Parcelable {

    var result: Result? = null
        set(value) {
            field = value
            if (field != null) {
                status = COMPLETED
            }
        }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.run {
            writeInt(requestCode)
            writeString(activityName)
            writeString(bundleKey)
            writeParcelable(request, 0)
            writeSerializable(status)
            writeParcelable(result, 0)
        }
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<Step> {
        override fun createFromParcel(source: Parcel): Step {
            val requestCode = source.readInt()
            val activityName = source.readString()!!
            val bundleKey = source.readString()!!
            val request = source.readParcelable<Request>(Request::class.java.classLoader)!!
            val status = source.readSerializable() as Status
            val result = source.readParcelable<Result>(Result::class.java.classLoader)

            return Step(requestCode, activityName, bundleKey, request, status).also {
                it.result = result
            }
        }

        override fun newArray(size: Int): Array<Step?> = arrayOfNulls(size)
    }

    enum class Status {
        NOT_STARTED, ONGOING, COMPLETED
    }

    interface Request : Parcelable

    interface Result : Parcelable
}

fun Step.Request.fromDomainToModuleApi(): Parcelable =
    when (this) {
        is FingerprintRequest -> fromDomainToModuleApiFingerprintRequest(this)
        is FaceRequest -> fromDomainToModuleApi()
        else -> throw Throwable("Invalid Request")
    }
