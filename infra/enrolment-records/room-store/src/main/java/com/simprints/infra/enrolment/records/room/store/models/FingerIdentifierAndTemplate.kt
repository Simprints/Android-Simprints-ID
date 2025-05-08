package com.simprints.infra.enrolment.records.room.store.models

import androidx.room.ColumnInfo

@Suppress("ArrayInDataClass")
data class FingerIdentifierAndTemplate(
    @ColumnInfo("fingerIdentifier") val fingerIdentifier: Int,
    @ColumnInfo("template") val template: ByteArray,
)
