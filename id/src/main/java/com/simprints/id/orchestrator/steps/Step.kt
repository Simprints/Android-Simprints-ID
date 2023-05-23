package com.simprints.id.orchestrator.steps

import android.os.Parcel
import android.os.Parcelable
import com.simprints.id.orchestrator.steps.Step.Status.COMPLETED
import java.util.UUID

data class Step(
    val id: String = UUID.randomUUID().toString(),
    val requestCode: Int,
    val activityName: String,
    val bundleKey: String,
    val request: Parcelable,
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
            writeParcelable(request, flags)
            writeString(status.name)
            writeParcelable(result, flags)
        }
    }

    override fun describeContents() = 0

    // Useful when debugging
    override fun toString(): String {
        return "$request: $status"
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
            val request = source.readParcelable<Request>(Request::class.java.classLoader)!!
            val status = Status.valueOf(source.readString()!!)
            val result = source.readParcelable<Result>(Result::class.java.classLoader)

            return Step(id, requestCode, activityName, bundleKey, request, result, status)
        }

        override fun newArray(size: Int): Array<Step?> = arrayOfNulls(size)
    }

    enum class Status {
        NOT_STARTED, ONGOING, COMPLETED
    }

    interface Request : Parcelable

    interface Result : Parcelable
}
