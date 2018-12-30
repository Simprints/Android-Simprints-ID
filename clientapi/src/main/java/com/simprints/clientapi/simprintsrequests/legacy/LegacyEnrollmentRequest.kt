package com.simprints.clientapi.simprintsrequests.legacy

import android.os.Parcel
import android.os.Parcelable
import com.simprints.clientapi.simprintsrequests.EnrollmentRequest
import com.simprints.clientapi.simprintsrequests.SimprintsIdRequest


data class LegacyEnrollmentRequest(val legacyApiKey: String,
                                   val moduleId: String,
                                   val userId: String,
                                   val metadata: String) : SimprintsIdRequest {

    override val requestName: String = REQUEST_NAME

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "")

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(legacyApiKey)
        parcel.writeString(moduleId)
        parcel.writeString(userId)
        parcel.writeString(metadata)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<EnrollmentRequest> {
        private const val REQUEST_NAME = "legacyEnrollmentRequest"

        override fun createFromParcel(parcel: Parcel): EnrollmentRequest {
            return EnrollmentRequest(parcel)
        }

        override fun newArray(size: Int): Array<EnrollmentRequest?> {
            return arrayOfNulls(size)
        }
    }
}
