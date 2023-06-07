package com.simprints.fingerprint.data.domain.matching

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * This class represents the result of a fingerprint match request
 *
 * @property guid  the
 * @property confidence  the floating point number representing the score of how confident the match is.
 */
@Parcelize
data class MatchResult(
    val guid: String,
    val confidence: Float) : Parcelable
