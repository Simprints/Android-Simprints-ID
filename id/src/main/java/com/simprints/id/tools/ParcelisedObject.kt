package com.simprints.id.tools

import android.os.Parcel
import android.os.Parcelable

internal class ParcelisedObject<T: Parcelable> {

    private val parcel = Parcel.obtain()

    constructor(parcelable: T) {
        val flags = 0
        parcelable.writeToParcel(parcel, flags)
    }

    constructor(bytes: ByteArray) {
        with(parcel) {
            val offset = 0
            unmarshall(bytes, offset, bytes.size)
            setDataPosition(0)
        }
    }

    fun toBytes(): ByteArray = parcel.marshall()

    fun getParcel(): Parcel = parcel.apply {
        setDataPosition(0)
    }

    fun recycle() {
        parcel.recycle()
    }

}
