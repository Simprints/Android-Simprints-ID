package com.simprints.clientapi.simprintsrequests

import android.os.Parcel
import android.os.Parcelable


data class VerifyRequest(val projectId: String,
                         val moduleId: String,
                         val userId: String,
                         val metadata: String,
                         val verifyGuid: String) : SimprintsIdRequest {

    override val requestName: String = REQUEST_NAME

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "")

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(projectId)
        parcel.writeString(moduleId)
        parcel.writeString(userId)
        parcel.writeString(metadata)
        parcel.writeString(verifyGuid)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VerifyRequest> {
        private const val REQUEST_NAME = "verifyRequest"

        override fun createFromParcel(parcel: Parcel): VerifyRequest {
            return VerifyRequest(parcel)
        }

        override fun newArray(size: Int): Array<VerifyRequest?> {
            return arrayOfNulls(size)
        }
    }


}
