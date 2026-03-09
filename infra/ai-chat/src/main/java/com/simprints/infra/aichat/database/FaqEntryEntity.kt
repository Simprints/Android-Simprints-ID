package com.simprints.infra.aichat.database

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "FaqEntry")
@Keep
internal data class FaqEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val question: String,
    val answer: String,
    val keywords: String,
    val category: String,
)
