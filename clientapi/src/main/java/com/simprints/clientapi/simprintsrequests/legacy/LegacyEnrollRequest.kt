package com.simprints.clientapi.simprintsrequests.legacy

import android.os.Parcel
import android.os.Parcelable


data class LegacyEnrollRequest(val legacyApiKey: String,
                               val moduleId: String,
                               val userId: String,
                               val metadata: String) : LegacySimprintsIdRequest {

    override val requestName: String = REQUEST_NAME

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(legacyApiKey)
        parcel.writeString(moduleId)
        parcel.writeString(userId)
        parcel.writeString(metadata)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LegacyEnrollRequest> {
        private const val REQUEST_NAME = "legacyEnrollmentRequest"

        override fun createFromParcel(parcel: Parcel): LegacyEnrollRequest {
            return LegacyEnrollRequest(parcel)
        }

        override fun newArray(size: Int): Array<LegacyEnrollRequest?> {
            return arrayOfNulls(size)
        }
    }
}
