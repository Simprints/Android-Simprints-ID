package com.simprints.id.data.db.remote.models

import android.os.Parcel
import android.os.Parcelable
import com.simprints.id.tools.json.SkipSerialisation
import com.simprints.libcommon.Fingerprint
import com.simprints.libcommon.Utils
import com.simprints.libsimprints.FingerIdentifier

data class fb_Fingerprint(@SkipSerialisation
                          val fingerId: FingerIdentifier = FingerIdentifier.LEFT_THUMB,
                          val template: String = "",
                          val quality: Int = 0) : Parcelable {

    constructor(parcel: Parcel) : this(
        TODO("fingerId"),
        parcel.readString(),
        parcel.readInt()) {
    }

    constructor (fingerprint: Fingerprint) : this (
        fingerId = fingerprint.fingerId,
        template = Utils.byteArrayToBase64(fingerprint.templateBytes),
        quality = fingerprint.qualityScore)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(template)
        parcel.writeInt(quality)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<fb_Fingerprint> {
        override fun createFromParcel(parcel: Parcel): fb_Fingerprint {
            return fb_Fingerprint(parcel)
        }

        override fun newArray(size: Int): Array<fb_Fingerprint?> {
            return arrayOfNulls(size)
        }
    }
}
