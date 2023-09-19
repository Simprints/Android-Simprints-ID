package com.simprints.id.orchestrator.steps

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import com.simprints.id.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.id.domain.moduleapi.fingerprint.requests.fromDomainToModuleApi
import com.simprints.id.orchestrator.steps.Step.Status.COMPLETED
import com.simprints.id.orchestrator.steps.core.requests.CoreRequest
import java.util.UUID

/**
 * Step
 *
 * @property id
 * @property requestCode
 * @property activityName
 * @property bundleKey
 * @property payloadType
 * @property payload   A Step payload can carry either a bundle of navigation args or a request to be handled
 * @property result
 * @property status
 * @constructor Create empty Step
 */
data class Step(
    val id: String = UUID.randomUUID().toString(),
    val requestCode: Int,
    val activityName: String,
    val bundleKey: String,
    val payloadType: PayloadType,
    val payload: Parcelable,
    private var result: Result? = null,
    private var status: Status
) : Parcelable {

    fun getResult() = result

    fun setResult(result: Result?) {
        if (result != null)
            setStatus(COMPLETED)
        this.result = result
    }

    fun getStatus(): Status {
        updateStatusBasedOnResult()
        return status
    }

    fun setStatus(status: Status) {
        this.status = status
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.run {
            writeString(id)
            writeInt(requestCode)
            writeString(activityName)
            writeString(bundleKey)
            writeString(payloadType.name)
            writeParcelable(payload, flags)
            writeString(status.name)
            writeParcelable(result, flags)
        }
    }

    override fun describeContents() = 0

    // Useful when debugging
    override fun toString(): String {
        return "$payload: $status"
    }

    private fun updateStatusBasedOnResult() {
        if (result != null)
            setStatus(COMPLETED)
    }

    companion object CREATOR : Parcelable.Creator<Step> {
        override fun createFromParcel(source: Parcel): Step {
            val id = source.readString()!!
            val requestCode = source.readInt()
            val activityName = source.readString()!!
            val bundleKey = source.readString()!!
            val payloadType = PayloadType.valueOf(source.readString()!!)
            val request = when (payloadType) {
                PayloadType.BUNDLE -> source.readParcelable<Bundle>(Bundle::class.java.classLoader)!!
                PayloadType.REQUEST -> source.readParcelable<Request>(Request::class.java.classLoader)!!
            }
            val status = Status.valueOf(source.readString()!!)
            val result = source.readParcelable<Result>(Result::class.java.classLoader)

            return Step(
                id,
                requestCode,
                activityName,
                bundleKey,
                payloadType,
                request,
                result,
                status
            )
        }

        override fun newArray(size: Int): Array<Step?> = arrayOfNulls(size)
    }

    enum class PayloadType {
        BUNDLE, REQUEST
    }

    enum class Status {
        NOT_STARTED, ONGOING, COMPLETED
    }

    interface Request : Parcelable

    interface Result : Parcelable
}

fun Step.Request.fromDomainToModuleApi(): Parcelable =
    when (this) {
        is FingerprintRequest -> fromDomainToModuleApi()
        is CoreRequest -> this

        else -> throw Throwable("Invalid Request $this")
    }
