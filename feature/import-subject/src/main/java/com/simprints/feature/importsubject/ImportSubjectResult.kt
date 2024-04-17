package com.simprints.feature.importsubject

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class ImportSubjectResult(
    val isSuccess: Boolean,
) : Serializable
