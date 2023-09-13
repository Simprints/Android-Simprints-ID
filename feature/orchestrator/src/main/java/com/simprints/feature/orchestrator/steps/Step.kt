package com.simprints.feature.orchestrator.steps

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.IdRes


data class Step(
    @IdRes val navigationActionId: Int,
    @IdRes val destinationId: Int,
    val payload: Bundle,
    val resultType: Class<out Parcelable>,
    var status: StepStatus = StepStatus.NOT_STARTED,
    var result: Parcelable? = null,
) : Parcelable {

    // TODO Figure out why using @Parcelize is causing ShadowParcel$UnreliableBehaviorError in pipeline tests

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readBundle(Bundle::class.java.classLoader) ?: Bundle.EMPTY,
        parcel.readSerializable() as Class<out Parcelable>,
        StepStatus.valueOf(parcel.readString().orEmpty()),
        parcel.readParcelable(Parcelable::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(navigationActionId)
        parcel.writeInt(destinationId)
        parcel.writeBundle(payload)
        parcel.writeSerializable(resultType)
        parcel.writeString(status.name)
        parcel.writeParcelable(result, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Step> {
        override fun createFromParcel(parcel: Parcel): Step {
            return Step(parcel)
        }

        override fun newArray(size: Int): Array<Step?> {
            return arrayOfNulls(size)
        }
    }
}

enum class StepStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
}
