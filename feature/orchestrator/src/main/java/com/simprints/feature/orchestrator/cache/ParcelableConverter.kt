package com.simprints.feature.orchestrator.cache

import android.os.Parcel
import android.os.Parcelable
import javax.inject.Inject

internal class ParcelableConverter @Inject constructor() {

    fun marshall(parcelable: Parcelable): ByteArray {
        val parcel = Parcel.obtain()
        parcelable.writeToParcel(parcel, 0)
        val bytes = parcel.marshall()
        parcel.recycle()
        return bytes
    }

    fun <T : Parcelable> unmarshall(bytes: ByteArray, creator: Parcelable.Creator<T>): T {
        val parcel = unmarshall(bytes)
        return creator.createFromParcel(parcel)
    }

    private fun unmarshall(bytes: ByteArray): Parcel {
        val parcel = Parcel.obtain()
        parcel.unmarshall(bytes, 0, bytes.size)
        parcel.setDataPosition(0)
        return parcel
    }
}
