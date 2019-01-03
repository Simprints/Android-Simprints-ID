package com.simprints.clientapi.simprintsrequests

import android.os.Parcel
import android.os.Parcelable


data class EnrollRequest(val projectId: String,
                         val moduleId: String,
                         val userId: String,
                         val metadata: String) : SimprintsIdRequest {

    override val requestName: String = REQUEST_NAME

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(projectId)
        parcel.writeString(moduleId)
        parcel.writeString(userId)
        parcel.writeString(metadata)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<EnrollRequest> {
        private const val REQUEST_NAME = "enrollmentRequest"

        override fun createFromParcel(parcel: Parcel): EnrollRequest {
            return EnrollRequest(parcel)
        }

        override fun newArray(size: Int): Array<EnrollRequest?> {
            return arrayOfNulls(size)
        }
    }
}
