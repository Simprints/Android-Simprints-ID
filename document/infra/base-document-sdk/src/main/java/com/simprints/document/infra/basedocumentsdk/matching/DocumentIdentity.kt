package com.simprints.document.infra.basedocumentsdk.matching

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DocumentIdentity(
    val subjectId: String,
    val documents: List<DocumentSample>,
) : Parcelable
