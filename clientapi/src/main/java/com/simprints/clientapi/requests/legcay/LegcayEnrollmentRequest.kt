package com.simprints.clientapi.requests.legcay

import android.os.Parcel
import android.os.Parcelable
import com.simprints.clientapi.requests.SimprintsIdRequest

data class LegcayEnrollmentRequest(val apiKey: String,
                                   val moduleId: String,
                                   val userId: String,
                                   val callingPackage: String,
                                   val metadata: String) : Parcelable, SimprintsIdRequest {

    override val requestName: String = REQUEST_NAME

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "")

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(apiKey)
        parcel.writeString(moduleId)
        parcel.writeString(userId)
        parcel.writeString(callingPackage)
        parcel.writeString(metadata)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LegcayEnrollmentRequest> {
        private const val REQUEST_NAME = "legacyEnrollmentRequest"

        override fun createFromParcel(parcel: Parcel): LegcayEnrollmentRequest {
            return LegcayEnrollmentRequest(parcel)
        }

        override fun newArray(size: Int): Array<LegcayEnrollmentRequest?> {
            return arrayOfNulls(size)
        }
    }
}
