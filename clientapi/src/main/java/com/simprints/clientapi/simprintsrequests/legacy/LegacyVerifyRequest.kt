package com.simprints.clientapi.simprintsrequests.legacy

import android.os.Parcel
import android.os.Parcelable


data class LegacyVerifyRequest(val legacyApiKey: String,
                               val moduleId: String,
                               val userId: String,
                               val metadata: String,
                               val verifyGuid: String) : LegacySimprintsIdRequest {

    override val requestName: String = REQUEST_NAME

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "")

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(legacyApiKey)
        parcel.writeString(moduleId)
        parcel.writeString(userId)
        parcel.writeString(metadata)
        parcel.writeString(verifyGuid)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LegacyVerifyRequest> {
        private const val REQUEST_NAME = "legacyVerifyRequest"

        override fun createFromParcel(parcel: Parcel): LegacyVerifyRequest {
            return LegacyVerifyRequest(parcel)
        }

        override fun newArray(size: Int): Array<LegacyVerifyRequest?> {
            return arrayOfNulls(size)
        }
    }

}
