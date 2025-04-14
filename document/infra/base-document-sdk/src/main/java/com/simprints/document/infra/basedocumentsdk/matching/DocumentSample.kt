package com.simprints.document.infra.basedocumentsdk.matching

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DocumentSample(
    val documentId: String,
    val template: ByteArray,
) : Parcelable
